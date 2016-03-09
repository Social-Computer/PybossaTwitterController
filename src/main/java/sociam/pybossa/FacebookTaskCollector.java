package sociam.pybossa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.PagableList;
import facebook4j.Post;
import facebook4j.Reading;
import facebook4j.ResponseList;
import sociam.pybossa.config.Config;
import sociam.pybossa.util.FacebookAccount;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class FacebookTaskCollector {

	final static Logger logger = Logger.getLogger(FacebookTaskCollector.class);
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat(
			"yyyy-mm-dd'T'hh:mm:ss.SSSSSS");
	final static String SOURCE = "Facebook";

	static Facebook facebook;

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");
		facebook = FacebookAccount.setFacebookAccount(1);
		logger.info("TaskCollector will be repeated every "
				+ Config.TaskCollectorTrigger + " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.TaskCollectorTrigger
						+ " ms");
				Thread.sleep(Integer.valueOf(Config.TaskCollectorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}

	}

	public static void run() {
		try {

			logger.debug("Getting time line from facebook");
			ArrayList<Post> postsfromFacebook = getLatestPosts(facebook);
			if (postsfromFacebook != null) {
				logger.debug("There are "
						+ postsfromFacebook.size()
						+ " facebook posts with at least one message to be processed");
				for (Post post : postsfromFacebook) {
					String postMessage = post.getMessage();
					Pattern pattern = Pattern.compile("(#t[0-9]+)");
					Matcher matcher = pattern.matcher(postMessage);
					String taskID = "";
					if (matcher.find()) {
						logger.debug("Found a taskID in the orginal post");
						taskID = matcher.group(1).replaceAll("#t", "");
						Integer intTaskID = Integer.valueOf(taskID);
						logger.debug("Retriving Task id from Collection: "
								+ Config.taskCollection);
						Document doc = getTaskFromMongoDB(intTaskID);
						if (doc != null) {
							int project_id = doc.getInteger("project_id");
							PagableList<Comment> comments = post.getComments();
							for (Comment comment : comments) {
								String taskResponse = comment.getMessage();
								String contributorName = comment.getFrom()
										.getName();
								if (insertTaskRun(taskResponse, intTaskID,
										project_id, contributorName, SOURCE)) {
									logger.debug("Task run was completely processed");
								} else {
									logger.error("Failed to process the task run");
								}
							}
						} else {
							logger.error("Couldn't find task with ID " + taskID);
						}
					} else {
						logger.error("couldn't find an associated task id with the one in the post "
								+ postMessage);
					}
				}
			} else {
				logger.error("facebook is not returning any posts!");
			}

		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

	public static Boolean insertTaskRun(String text, int task_id,
			int project_id, String contributor_name, String source) {

		Document taskRun = getTaskRunsFromMongoDB(task_id, contributor_name);
		if (taskRun != null) {
			logger.error("You are only allowed one contribution for each task.");
			logger.error("task_id= " + task_id + " screen_name: "
					+ contributor_name);
			return false;
		}

		JSONObject jsonData = BuildJsonTaskRunContent(text, task_id, project_id);
		if (insertTaskRunIntoMongoDB(jsonData, contributor_name, source)) {
			logger.debug("Task run was successfully inserted into MongoDB");
			// Project has to be reqested before inserting a task run
			logger.debug("Requesting the project ID from PyBossa before inserting it");
			String postURL = Config.PyBossahost + Config.taskRunDir
					+ Config.api_key;
			JSONObject postResponse = insertTaskRunIntoPyBossa(postURL,
					jsonData);
			if (postResponse != null) {
				logger.debug("Task run was successfully inserted into PyBossa");
				return true;
			} else {
				return false;
			}

		} else {
			logger.error("Task run was not inserted into MongoDB!");
			return false;
		}

	}

	public static Boolean insertTaskRunIntoMongoDB(JSONObject jsonData,
			String contributor_name, String source) {

		try {
			Date date = new Date();
			String insertedAt = MongoDBformatter.format(date);
			Integer project_id = jsonData.getInt("project_id");
			Integer task_id = jsonData.getInt("task_id");
			String task_run_text = jsonData.getString("info");
			logger.debug("Inserting a task run into MongoDB");
			if (pushTaskRunToMongoDB(insertedAt, project_id, task_id,
					task_run_text, contributor_name, source)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}

	}

	// maybe it's not needed to check id_str becasue we check it first!
	// so only do an insert?
	public static boolean pushTaskRunToMongoDB(String publishedAt,
			Integer project_id, Integer task_id, String task_text,
			String contributor_name, String source) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			if (publishedAt != null && project_id != null && task_text != null
					&& contributor_name != null && source != null) {

				database.getCollection(Config.taskRunCollection).insertOne(
						new Document().append("publishedAt", publishedAt)
								.append("project_id", project_id)
								.append("task_id", task_id)
								.append("task_text", task_text)
								.append("contributor_name", contributor_name)
								.append("source", source));
				logger.debug("One task run is inserted into MongoDB");
				mongoClient.close();
				return true;

			} else {
				mongoClient.close();
				return false;
			}

		} catch (Exception e) {
			logger.error("Error with inserting the task run " + " publishedAt "
					+ publishedAt + " project_id " + project_id + " isPushed "
					+ task_id + " task_id " + task_text + "\n", e);
			mongoClient.close();
			return false;
		}

	}

	public static JSONObject insertTaskRunIntoPyBossa(String url,
			JSONObject jsonData) {
		JSONObject jsonResult = null;
		logger.debug("Json to be inserted into PyBossa:  " + jsonData);
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString(), "utf-8");
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));
			String output;
			logger.debug("Output from Server ...."
					+ response.getStatusLine().getStatusCode() + "\n");
			StringBuffer responseText = new StringBuffer();
			while ((output = br.readLine()) != null) {

				responseText.append(output);
			}
			jsonResult = new JSONObject(responseText);
			logger.debug("Post Response " + responseText);
			if (response.getStatusLine().getStatusCode() == 200
					|| response.getStatusLine().getStatusCode() == 204) {
				return jsonResult;
			} else {
				logger.error("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
				logger.error("Message " + response.getStatusLine());
				logger.error(response.toString());
				return null;
			}

		} catch (Exception ex) {
			logger.error(ex);
			return null;
		}

	}

	/**
	 * It returns a json string for task creation from a given task details
	 * 
	 * @param text
	 *            the task content
	 * @param n_answers
	 * @param quorum
	 * @param calibration
	 * @param project_id
	 *            The project ID
	 * @param priority_0
	 * @return Json string
	 */
	public static JSONObject BuildJsonTaskRunContent(String answer,
			int task_id, int project_id) {

		JSONObject app2 = new JSONObject();
		app2.put("project_id", project_id);

		app2.put("info", answer);
		app2.put("task_id", task_id);
		Random rn = new Random();
		int randomNum = rn.nextInt((250 - 1) + 1) + 1;
		String ip = "80.44.192." + randomNum;
		app2.put("user_ip", ip);
		logger.debug("Generated ip " + ip);
		return app2;
	}

	public static Document getTaskFromMongoDB(int pybossa_task_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.taskCollection).find(
					new Document("pybossa_task_id", pybossa_task_id));
			Document document = iterable.first();
			mongoClient.close();
			return document;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static Document getTaskRunsFromMongoDB(int task_id,
			String contributor_name) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.taskRunCollection).find(
					new Document("task_id", task_id).append("contributor_name",
							contributor_name));
			Document document = iterable.first();
			mongoClient.close();
			return document;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static Post getPostByID(String post_id, Facebook facebook) {

		try {
			facebook = FacebookAccount.setFacebookAccount(1);
			Post onePost = facebook.getPost(post_id,
					new Reading().fields("comments,message,name"));
			return onePost;
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}
	}

	public static ArrayList<Post> getLatestPosts(Facebook facebook) {

		ArrayList<Post> validposts = new ArrayList<Post>();
		try {
			ResponseList<Post> feeds = facebook.getFeed("964602923577144",
					new Reading().limit(100).fields("comments,message,name"));
			for (Post post : feeds) {
				if (post.getComments().size() > 0) {
					validposts.add(post);
				}
			}
			if (!validposts.isEmpty()) {
				return validposts;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}

	}

}
