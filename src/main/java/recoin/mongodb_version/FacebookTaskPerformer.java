package recoin.mongodb_version;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.FacebookMethods;
import sociam.pybossa.methods.GeneralMethods;
import sociam.pybossa.methods.MongodbMethods;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class FacebookTaskPerformer {

	final static Logger logger = Logger.getLogger(FacebookTaskPerformer.class);
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
			ArrayList<Document> tasksToBePushed = MongodbMethods.getIncompletedTasksFromMongoDB("facebook_task_status");
			if (tasksToBePushed != null) {
				logger.info("There are " + tasksToBePushed.size()
						+ " tasks that need to be pushed into facebook, then updating to MongoDB");

				// randomly pick a task
				// for (Document document : tasksToBePushed) {
				int seed = 300;
				Queue<Document> queue = TwitterTaskPerformer.stackQueue(tasksToBePushed, seed);
				for (Document document : queue) {
					String facebook_task_status = document.getString("facebook_task_status");
					String task_text = document.getString("task_text");
					if (facebook_task_status.equals("pushed")) {
						String facebook_lastPushAtString = document.getString("facebook_lastPushAt");
						Date facebook_lastPushAt = MongoDBformatter.parse(facebook_lastPushAtString);
						if (!GeneralMethods.rePush(facebook_lastPushAt)) {
							continue;
						} else {
							logger.debug("Repushing task " + task_text);
						}
					}
					ObjectId _id = document.getObjectId("_id");
					int task_id = document.getInteger("task_id");
					int project_id = document.getInteger("project_id");
					String media_url = document.getString("media_url");
					ArrayList<String> hashtags = MongodbMethods.getProjectHashTags(project_id);
					String taskTag = "#t" + task_id;
					String facebook_task_id = FacebookMethods.sendTaskToFacebook(task_text, media_url, taskTag,
							hashtags, 1);
					if (facebook_task_id != null) {
						if (MongodbMethods.updateTaskToPushedInMongoDB(_id, facebook_task_id, "pushed")) {
							logger.info("Task with text " + task_text + " has been sucessfully pushed to facebook");
							wasPushed = true;
						} else {
							logger.error(
									"Error with updating " + Config.taskCollection + " for the _id " + _id.toString());
						}
					} else {
						if (MongodbMethods.updateTaskToPushedInMongoDB(_id, "", "error")) {
							logger.debug(
									"pushing post to facebook has encountered an error, but has been updated into MongoDB "
											+ task_text);
						} else {
							logger.error("Couldn't update the task in MongoDB");
						}
					}
					// TODO: add lastPushAt when updating tasks

					if (wasPushed) {
						wasPushed = false;
						logger.debug("waiting for " + Config.TaskPerformerPushRate
								+ " ms before pushing another post to facebook");
						Thread.sleep(Integer.valueOf(Config.TaskPerformerPushRate));
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

}
