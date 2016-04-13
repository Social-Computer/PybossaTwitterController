package recoin.mongodb_version;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.GeneralMethods;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.TwitterMethods;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class TwitterTaskPerformer {

	final static Logger logger = Logger.getLogger(TwitterTaskPerformer.class);
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	static Boolean wasPushed = false;

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		logger.info("TaskPerformer will be repeated every "
				+ Config.TaskCreatorTrigger + " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.TaskPerformerTrigger
						+ " ms");
				Thread.sleep(Integer.valueOf(Config.TaskPerformerTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}
	}

	public static void run() {
		try {
			ArrayList<Document> tasksToBePushed = MongodbMethods
					.getIncompletedTasksFromMongoDB("twitter_task_status");
			if (tasksToBePushed != null) {
				logger.info("There are "
						+ tasksToBePushed.size()
						+ " tasks that need to be pushed into Twitter, then updating to MongoDB");

				// randomly pick a task
				// for (Document document : tasksToBePushed) {
				HashSet<Integer> taskIDs = new HashSet<Integer>();
				int seed = 200;
				while (taskIDs.size() < tasksToBePushed.size()) {
					Random random = new Random(seed);
					seed++;
					Integer genertatedTaskID = random.nextInt(tasksToBePushed
							.size());
					if (taskIDs.contains(genertatedTaskID)) {
						continue;
					} else {
						taskIDs.add(genertatedTaskID);
					}
					Document document = tasksToBePushed.get(genertatedTaskID);
					String twitter_task_status = document
							.getString("twitter_task_status");
					String task_text = document.getString("task_text");
					if (twitter_task_status.equals("pushed")) {
						String twitter_lastPushAtString = document.getString("twitter_lastPushAt");
						Date twitter_lastPushAt = MongoDBformatter.parse(twitter_lastPushAtString);
						if (!GeneralMethods.rePush(twitter_lastPushAt)) {
							continue;
						} else {
							logger.debug("Repushing task " + task_text);
						}
					}
					ObjectId _id = document.getObjectId("_id");
					Integer task_id = document.getInteger("task_id");
					int project_id = document.getInteger("project_id");
					String media_url = document.getString("media_url");
					ArrayList<String> hashtags = MongodbMethods
							.getProjectHashTags(project_id);
					String taskTag = "#t" + task_id;
					int responseCode = TwitterMethods.sendTaskToTwitter(
							task_text, media_url, taskTag, hashtags, 2);
					if (responseCode == 1) {
						if (MongodbMethods.updateTaskToPushedInMongoDB(_id, "pushed")) {
							logger.info("Task with text " + task_text
									+ " has been sucessfully pushed to Twitter");
							wasPushed = true;
						} else {
							logger.error("Error with updating "
									+ Config.taskCollection + " for the _id "
									+ _id.toString());
						}
					} else if (responseCode == 0) {
						if (MongodbMethods.updateTaskToPushedInMongoDB(_id,
								"notValied")) {
							logger.debug("Tweeet is not valid because of length, but updated in Mongodb"
									+ task_text);
						}
						logger.error("Couldn't update the task in MongoDB");
					} else if (responseCode == 2) {
						if (MongodbMethods.updateTaskToPushedInMongoDB(_id, "error")) {
							logger.debug("pushing tweet has encountered an error, but has been updated into MongoDB "
									+ task_text);
						} else {
							logger.error("Couldn't update the task in MongoDB");
						}
					}
					// TODO: add lastPushAt when updating tasks

					if (wasPushed) {
						wasPushed = false;
						logger.debug("waiting for "
								+ Config.TaskPerformerPushRate
								+ " ms before pushing another tweet");
						Thread.sleep(Integer
								.valueOf(Config.TaskPerformerPushRate));
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

}
