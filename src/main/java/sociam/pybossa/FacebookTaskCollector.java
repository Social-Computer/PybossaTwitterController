package sociam.pybossa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.json.JSONObject;

import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.PagableList;
import facebook4j.Post;
import sociam.pybossa.config.Config;
import sociam.pybossa.methods.FacebookMethods;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.PybossaMethods;
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
			ArrayList<Post> postsfromFacebook = FacebookMethods
					.getLatestPosts(facebook);
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
						Document doc = MongodbMethods
								.getTaskFromMongoDB(intTaskID);
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
			logger.debug("Adding task_id field to collection " + Config.taskRunCollection);
			MongodbMethods.updateTaskRunsByAddingCounters();

		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

	public static Boolean insertTaskRun(String text, int task_id,
			int project_id, String contributor_name, String source) {

		Document taskRun = MongodbMethods.getTaskRunsFromMongoDB(task_id,
				contributor_name, text);
		if (taskRun != null) {
			logger.error("You are only allowed one contribution for each task.");
			logger.error("task_id= " + task_id + " screen_name: "
					+ contributor_name);
			return false;
		}

		JSONObject jsonData = PybossaMethods.BuildJsonTaskRunContent(text,
				task_id, project_id);
		if (MongodbMethods.insertTaskRunIntoMongoDB(jsonData, contributor_name,
				source)) {
			logger.debug("Task run was successfully inserted into MongoDB");
			// Project has to be reqested before inserting a task run
			logger.debug("Requesting the project ID from PyBossa before inserting it");
			String postURL = Config.PyBossahost + Config.taskRunDir
					+ Config.api_key;
			JSONObject postResponse = PybossaMethods.insertTaskRunIntoPyBossa(
					postURL, jsonData);
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

}
