package sociam.pybossa;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.util.StringToImage;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class TaskPerformer {

	final static Logger logger = Logger.getLogger(TaskPerformer.class);
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static Boolean wasPushed = false;

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		logger.info("TaskPerformer will be repeated every " + Config.TaskCreatorTrigger + " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.TaskPerformerTrigger + " ms");
				Thread.sleep(Integer.valueOf(Config.TaskPerformerTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}
	}

	public static void run() {
		try {
			HashSet<Document> tasksToBePushed = getReadyTasksFromMongoDB();
			if (tasksToBePushed != null) {
				logger.info("There are " + tasksToBePushed.size()
						+ " tasks that need to be pushed into Twitter, then updating to MongoDB");

				for (Document document : tasksToBePushed) {
					String task_status = document.getString("task_status");
					String task_text = document.getString("task_text");
					if (task_status.equals("pushed")) {
						Date lastPushAt = document.getDate("lastPushAt");
						if (!rePush(lastPushAt)) {
							break;
						} else {
							logger.debug("Repushing task " + task_text);
						}
					}

					ObjectId _id = document.getObjectId("_id");
					int pybossa_task_id = document.getInteger("pybossa_task_id");
					int project_id = document.getInteger("project_id");

					JSONObject project = getProjectByID(project_id);
					ArrayList<String> hashtags = new ArrayList<>();
					if (project != null) {
						JSONArray bin_id = project.getJSONArray("bin_ids");
						for (Object object : bin_id) {
							String binItem = (String) object;
							hashtags.add("#" + binItem);
						}

					}

					String taskTag = "#t" + pybossa_task_id;

					int responseCode = sendTaskToTwitter(task_text, taskTag, hashtags, 2);
					if (responseCode == 1) {
						if (updateTaskToPushedInMongoDB(_id, "pushed")) {
							logger.info("Task with text " + task_text + " has been sucessfully pushed to Twitter");
							wasPushed = true;
						} else {
							logger.error(
									"Error with updating " + Config.taskCollection + " for the _id " + _id.toString());
						}
					} else if (responseCode == 0) {
						if (updateTaskToPushedInMongoDB(_id, "notValied")) {
							logger.debug("Tweeet is not valid because of length, but updated in Mongodb" + task_text);
						}
						logger.error("Couldn't update the task in MongoDB");
					} else if (responseCode == 2) {
						if (updateTaskToPushedInMongoDB(_id, "error")) {
							logger.debug("pushing tweet has encountered an error, but has been updated into MongoDB "
									+ task_text);
						} else {
							logger.error("Couldn't update the task in MongoDB");
						}
					}
					// TODO: add lastPushAt when updating tasks

					if (wasPushed) {
						wasPushed = false;
						logger.debug(
								"waiting for " + Config.TaskPerformerPushRate + " ms before pushing another tweet");
						Thread.sleep(Integer.valueOf(Config.TaskPerformerPushRate));
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

	public static Boolean rePush(Date lastPushAt) {
		try {

			Calendar cal = Calendar.getInstance();
			Date currentDate = new Date();
			cal.setTime(lastPushAt);
			cal.add(Calendar.HOUR, Integer.valueOf(Config.RePushTaskToTwitter));
			Date convertedDate = cal.getTime();
			return currentDate.before(convertedDate);

		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}
	}

	public static Boolean updateTaskToPushedInMongoDB(ObjectId _id, String task_status) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
		try {

			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);
			Date date = new Date();
			String lastPushAt = MongoDBformatter.format(date);
			UpdateResult result = database.getCollection(Config.taskCollection).updateOne(new Document("_id", _id),
					new Document().append("$set",
							new Document("task_status", task_status).append("lastPushAt", lastPushAt)));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.taskCollection + " Collection was updated where _id= " + _id.toString()
							+ " to task_status=" + task_status);
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	/**
	 * This method send a task by a twitter account
	 * 
	 * @param taskId
	 *            The id of the task to be hashed within the tweet
	 * @param taskContent
	 *            the content of the tweet to be published
	 */
	public static int sendTaskToTwitter(String taskContent, String taskTag, ArrayList<String> hashtags,
			int project_type) {
		try {
			Twitter twitter = TwitterAccount.setTwitterAccount(project_type);

			// defualt
			String question = "";
			if (project_type == 2) {
				question = Config.project_validation_question;
			}

			// combine hashtags and tasktag while maintaining the 140 length
			String post = question;
			for (String string : hashtags) {
				if (post.length() == 0) {
					post = string;
				} else {
					String tmpResult = post + " " + string + taskTag;
					if (tmpResult.length() >= 140) {
						break;
					}
					post = post + " " + string;
				}
			}
			post = post + "?";
			post = post + " " + taskTag;

			// convert taskContent and question into an image
			File image = StringToImage.convertStringToImage(taskContent);

			if (post.length() < 140) {
				// status = twitter.updateStatus(post);

				StatusUpdate status = new StatusUpdate(post);
				status.setMedia(image);
				twitter.updateStatus(status);

				logger.debug("Successfully posting a task '" + status.getStatus() + "'." + status.getPlaceId());
				return 1;
			} else {
				logger.error("Post \"" + post + "\" is longer than 140 characters. It has: " + (post.length()));
				return 0;
			}
		} catch (Exception e) {
			logger.error("Error", e);
			return 2;
		}
	}

	// static HashSet<Document> NotPushedTasksjsons = new
	// LinkedHashSet<Document>();

	public static HashSet<Document> getReadyTasksFromMongoDB() {
		HashSet<Document> NotPushedTasksjsons = new LinkedHashSet<Document>();
		MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
		try {

			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(Config.taskCollection)
					.find(new Document("task_status", "ready"));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					NotPushedTasksjsons.add(document);
				}
			}
			mongoClient.close();
			return NotPushedTasksjsons;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static JSONObject getProjectByID(int project_id) {
		logger.debug("getting project by project_id from " + Config.projectCollection + " collection");
		MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
		try {
			// JSONObject json = null;
			// DB database = mongoClient.getDB(Config.projectsDatabaseName);
			// JSONObject proj = new JSONObject();
			// proj.put("project_id", project_id);
			// Object o = com.mongodb.util.JSON.parse(proj.toString());
			// DBObject dbObj = (DBObject) o;
			//
			// DBCollection collections =
			// database.getCollection(Config.projectCollection);
			// DBCursor iterable = collections.find(dbObj);
			// if (iterable.hasNext()) {
			// json = new JSONObject(iterable.next().toString());
			//
			// }
			// mongoClient.close();
			// return json;

			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);
			JSONObject json = null;
			FindIterable<Document> iterable = database.getCollection(Config.projectCollection)
					.find(new Document("project_id", project_id));
			if (iterable.first() != null) {
				Document document = iterable.first();
				json = new JSONObject(document);
			}
			mongoClient.close();
			return json;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

}
