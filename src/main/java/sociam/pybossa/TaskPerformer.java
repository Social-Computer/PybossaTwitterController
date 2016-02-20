package sociam.pybossa;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;

import sociam.pybossa.config.Config;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import com.mongodb.Block;
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
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	static MongoClient mongoClient = new MongoClient(Config.mongoHost,
			Config.mongoPort);
	static MongoDatabase database = mongoClient
			.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		logger.info("TaskPerformer will be repeated every "
				+ Config.TaskCreatorTrigger + " ms");
		try {
			while (true) {
				run();
				logger.info("Sleeping for " + Config.TaskPerformerTrigger
						+ " ms");
				Thread.sleep(Integer.valueOf(Config.TaskPerformerTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error " + e);
		}
	}

	public static void run() {
		try {
			HashSet<Document> tasksToBePushed = getTasksFromMongoDB();
			if (tasksToBePushed != null) {
				logger.info("There are "
						+ tasksToBePushed.size()
						+ " tasks that need to be pushed into Twitter, then updating to MongoDB");

				for (Document document : tasksToBePushed) {
					Boolean isPushed = document.getBoolean("isPushed");
					String task_text = document.getString("task_text");
					if (isPushed) {
						Date lastPushAt = document.getDate("lastPushAt");
						if (!rePush(lastPushAt)) {
							break;
						} else {
							logger.debug("Repushing task " + task_text);
						}
					}

					ObjectId _id = document.getObjectId("_id");
					if (sendTaskToTwitter(task_text)) {
						if (updateTaskToPushedInMongoDB(_id)) {
							logger.info("Task with text " + task_text
									+ " has been sucessfully pushed to Twitter");
						} else {
							logger.error("Error with updating "
									+ Config.taskCollection + " for the _id "
									+ _id.toString());
						}
					} else {
						logger.error("Couldn't push task with text \""
								+ task_text + "\" to twitter");
					}
					// TODO: add lastPushAt when updating tasks

					logger.debug("waiting for " + Config.TaskPerformerPushRate
							+ " ms before pushing another tweet");
					Thread.sleep(Integer.valueOf(Config.TaskPerformerPushRate));
				}
			}
		} catch (Exception e) {
			logger.error("Error " + e);
		}
	}

	private static Boolean rePush(Date lastPushAt) {
		try {

			Calendar cal = Calendar.getInstance();
			Date currentDate = new Date();
			cal.setTime(lastPushAt);
			cal.add(Calendar.HOUR, Integer.valueOf(Config.RePushTaskToTwitter));
			Date convertedDate = cal.getTime();
			return currentDate.before(convertedDate);

		} catch (Exception e) {
			logger.error("Error " + e);
			return false;
		}
	}

	private static Boolean updateTaskToPushedInMongoDB(ObjectId _id) {
		try {
			Date date = new Date();
			String lastPushAt = MongoDBformatter.format(date);
			UpdateResult result = database.getCollection(
					Config.taskCollection).updateOne(
					new Document("_id", _id),
					new Document().append("$set",
							new Document("isPushed", true).append("lastPushAt",
									lastPushAt)));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.taskCollection
							+ " Collection was updated where _id= "
							+ _id.toString() + " to isPushed=true");
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			logger.error("Error " + e);
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
	private static Boolean sendTaskToTwitter(String taskContent) {
		Twitter twitter = TwitterFactory.getSingleton();
		Status status;

		try {
			String post = taskContent;
			if (post.length() < 140) {
				status = twitter.updateStatus(post);
				logger.debug("Successfully posting a task [" + status.getText()
						+ "]." + status.getId());
				return true;
			} else {
				logger.error("Post \"" + post
						+ "\" is longer than 140 characters");
				return false;
			}
		} catch (Exception e) {
			logger.error(e);
			return false;
		}

	}

	static HashSet<Document> NotPushedTasksjsons = new LinkedHashSet<Document>();

	public static HashSet<Document> getTasksFromMongoDB() {
		NotPushedTasksjsons = new LinkedHashSet<Document>();
		try {
			FindIterable<Document> iterable = database.getCollection(
					Config.taskCollection).find(new Document());
			if (iterable.first() != null) {
				iterable.forEach(new Block<Document>() {
					@Override
					public void apply(final Document document) {
						NotPushedTasksjsons.add(document);
					}
				});
				return NotPushedTasksjsons;
			} else {

			}
			return NotPushedTasksjsons;
		} catch (Exception e) {
			logger.error("Error " + e);
			return null;
		}
	}

}
