package recoin.mongodb_version;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.TwitterMethods;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class InstructionSetPorcessor {

	final static Logger logger = Logger.getLogger(InstructionSetPorcessor.class);

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");
		logger.info("InstructionSetPorcessor will be repeated every " + Config.TaskCollectorTrigger + " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.TaskCollectorTrigger + " ms");
				Thread.sleep(Integer.valueOf(Config.TaskCollectorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}
	}

	public static void run() {
		try {

			logger.debug("Getting Task runs");
			HashSet<Document> taskRunsDocuments = MongodbMethods.getUnPorcessedTaskRuns();
			if (taskRunsDocuments != null) {
				logger.info("There are " + taskRunsDocuments.size() + " taskRuns to be processed");
				if (!taskRunsDocuments.isEmpty()) {

					for (Document document : taskRunsDocuments) {
						ObjectId _id = document.getObjectId("_id");
						String task_run_text = document.getString("task_run_text");
						String instructionSet = determineInstructionSetType(task_run_text);
						Boolean wasProcessed = false;
						if (instructionSet != null) {
							switch (instructionSet) {
							case "PRIO":
								wasProcessed = process_Prio_instrctionSet(document);
								break;
							case "SHARE":
								wasProcessed = process_Share_instrctionSet(document);
								break;
							case "ENRICH":
								wasProcessed = process_Enrich_instrctionSet(document);
								break;
							case "TRANS":
								wasProcessed = process_Trans_instrctionSet(document);
								break;
							case "RESOLVE":
								wasProcessed = process_Resolve_instrctionSet(document);
								break;
							}

						} else {
							logger.debug("Task Run was not identifed with any known instruction set");
							logger.debug(document.toString());
						}
						if (wasProcessed) {
							Boolean updated = MongodbMethods.updatetaskRunsToBeProcessed(_id);
							if (updated) {
								logger.info("TaskRun was sucessfully updated to be processed");
							}
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

	public static Boolean process_Prio_instrctionSet(Document document) {

		String task_run_text = document.getString("task_run_text");
		int task_id = document.getInteger("task_id");
		Document doc = MongodbMethods.getTaskFromMongoDB(task_id);
		Integer OldPriority = doc.getInteger("priority");
		if (OldPriority == null) {
			OldPriority = 0;
		}
		Pattern patter = Pattern.compile("-?[0-9]+");
		Matcher matcher = patter.matcher(task_run_text);
		Integer priority_number = null;
		if (matcher.find()) {
			priority_number = Integer.valueOf(matcher.group(0));
		}

		if (priority_number == null) {
			return false;
		}

		Integer result = OldPriority + priority_number;
		Boolean updated = MongodbMethods.updatePriorityInTask(task_id, result);
		return updated;
	}

	public static Boolean process_Share_instrctionSet(Document document) {

		logger.debug("Sharing insrtruction set");
		String task_run_text = document.getString("task_run_text");
		String source = document.getString("source");
		int task_id = document.getInteger("task_id");
		if (source.equals("Twitter") || task_run_text.contains("http://twitter.com/")) {
			Document taskDoc = MongodbMethods.getTaskFromMongoDB(task_id);
			String taskTag = "#t" + task_id;
			int project_id = document.getInteger("project_id");
			ArrayList<String> hashtags = MongodbMethods.getProjectHashTags(project_id);
			String twitter_url = taskDoc.getString("twitter_url");
			HashSet<String> users = extractUserToBeSharedWith(task_run_text);
			if (!users.isEmpty()) {
				for (String string : users) {
					int response = TwitterMethods.sendTaskToTwitterWithUrl(taskTag, hashtags, 2, string, twitter_url);
					if (response == 1) {
						logger.debug("Task was shared with " + string);
						return true;
					}else{
						logger.error("Couldn't post tweet to Twitter");
					}
				}
			}else{
				logger.error("Couldn't identify users!");
			}
		}
		return false;
	}

	public static Boolean process_Enrich_instrctionSet(Document document) {

		return true;
	}

	public static Boolean process_Trans_instrctionSet(Document document) {

		return true;
	}

	public static Boolean process_Resolve_instrctionSet(Document document) {

		int task_id = document.getInteger("task_id");
		Boolean upadate = MongodbMethods.updateTaskToBeCompleted(task_id);
		return upadate;
	}

	public static String determineInstructionSetType(String task_run_text) {
		if (task_run_text.contains("PRIO")) {
			return "PRIO";
		} else if (task_run_text.contains("SHARE")) {
			return "SHARE";
		} else if (task_run_text.contains("ENRICH")) {
			return "ENRICH";
		} else if (task_run_text.contains("TRANS")) {
			return "TRANS";
		} else if (task_run_text.contains("RESOLVE")) {
			return "RESOLVE";
		} else {
			return null;
		}

	}

	public static HashSet<String> extractUserToBeSharedWith(String instrcutionSetText) {

		HashSet<String> users = new HashSet<String>();
		Pattern pattern = Pattern.compile("http://twitter.com/([A-Za-z0-9_]+)");
		Pattern pattern2 = Pattern.compile("@([A-Za-z0-9_]+)");
		Matcher matcher = pattern.matcher(instrcutionSetText);
		if (matcher.find()) {
			users.add("@" + matcher.group(1));
		}else{
			Matcher matcher2 = pattern2.matcher(instrcutionSetText);
			if (matcher2.find()) {
				users.add(matcher2.group());
			}
		}
		return users;

	}

}
