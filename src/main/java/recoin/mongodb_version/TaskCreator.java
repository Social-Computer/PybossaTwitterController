package recoin.mongodb_version;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class TaskCreator {

	final static Logger logger = Logger.getLogger(TaskCreator.class);

	public final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat(
			"yyyy-mm-dd'T'hh:mm:ss.SSSSSS");

	static String url = Config.PyBossahost + Config.taskDir + Config.api_key;

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		logger.info("TaskCreator will be repeated every "
				+ Config.TaskCreatorTrigger + " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.TaskCreatorTrigger + " ms");
				Thread.sleep(Integer.valueOf(Config.TaskCreatorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}

	}

	public static void run() {
		try {
			// Check for started projects
			HashSet<Document> projectsAsJsons = getnotCompletedProjects();
			if (projectsAsJsons != null) {
				logger.info("There are "
						+ projectsAsJsons.size()
						+ " projects that have tasks ready to be inserted into PyBossa, then to MongoDB");
				if (!projectsAsJsons.isEmpty()) {

					// Get project name and id for these started projects
					for (Document jsonObject : projectsAsJsons) {
						String bin_id = jsonObject.getString("bin_id");
						int project_id = jsonObject.getInteger("project_id");
						int tasksPerProjectlimit = Integer
								.valueOf(Config.TasksPerProject);
						ArrayList<String> tasksTexts = getAllTasksTextsFromMongodb(project_id);
						if (tasksTexts != null) {
							if (tasksTexts.size() >= tasksPerProjectlimit) {
								logger.debug("Project with id " + project_id
										+ " has already got "
										+ tasksPerProjectlimit + " tasks");
								MongodbMethods
										.updateProjectToInsertedInMongoDB(project_id);
								logger.debug("changing to another project");
								continue;
							}
						} else {
							continue;
						}
						int tasksPerProjectCounter = tasksTexts.size();
						if (tasksPerProjectCounter >= tasksPerProjectlimit) {
							logger.info("tasksPerProjectlimit was reached "
									+ tasksPerProjectCounter);
							MongodbMethods
									.updateProjectToInsertedInMongoDB(project_id);
							logger.debug("changing to another project");
							continue;

						}
						// TODO: don't retrieve ones which have already been
						// pushed
						// to
						// crowd and not completed by crowd, from Ramine
						HashSet<Document> bins = MongodbMethods
								.getTweetsFromBinInMongoDB(bin_id);
						HashSet<String> originalBinText = new HashSet<>();
						logger.info("There are \"" + bins.size()
								+ "\" tweets for projectID " + project_id);
						for (Document bin : bins) {

							logger.debug("Processing a new task");
							// for each bin, get the text/tweet
							String text = bin.getString("text");
							logger.debug("tweet text " + text);
							if (!originalBinText.contains(text)) {
								originalBinText.add(text);

								ObjectId _id = bin.getObjectId("_id");
								String bin_id_String = _id.toString();
								if (tasksTexts != null) {
									if (!tasksTexts.contains(text)) {

										// Build the PyBossa json for
										// insertion
										// of a
										// task
										String media_url = "";
										media_url = bin.getString("media_url");

										tasksTexts.add(text);

										// Insert the resonse of
										// PyBossa
										// into
										// MongoDB
										if (MongodbMethods
												.insertTaskIntoMongoDB(
														project_id,
														bin_id_String, text,
														media_url, "ready",
														"validate")) {
											tasksPerProjectCounter++;
										} else {
											logger.error("Task was not inserted Into MongoDB");
										}

									} else {
										logger.error("task " + text
												+ " in Project " + project_id
												+ " is already in PyBossa!!");
									}
								}
							} else {
								logger.error("Tweet is already processed "
										+ bin.toString());
							}
						}
					}
				} else {
					logger.debug("There are no ready projects' tasks to be inserted into PyBossa!");
				}
			}

			// add counter for tasks as task_id
			logger.debug("Adding task_id field to collection "
					+ Config.taskCollection);
			MongodbMethods.updateTasksByAddingCounters();

		} catch (

		Exception e)

		{
			logger.error("Error ", e);
		}

	}

	public static HashSet<Document> getnotCompletedProjects() {
		logger.debug("getting projects from collection "
				+ Config.projectCollection);
		HashSet<Document> startedProjectsJsons = new HashSet<Document>();
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.projectCollection).find(
					new Document("project_status", "ready"));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					startedProjectsJsons.add(document);
				}
			}
			mongoClient.close();
			return startedProjectsJsons;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static ArrayList<String> getAllTasksTextsFromMongodb(int project_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		ArrayList<String> tasks = new ArrayList<String>();
		try {
			MongoDatabase binsDatabase = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = binsDatabase.getCollection(
					Config.taskCollection).find(
					new Document("project_id", project_id));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					// JSONObject app2 = new JSONObject(document);
					String task_text = document.getString("text");
					if (task_text != null) {
						tasks.add(task_text);
					}
				}
			}
			mongoClient.close();
			return tasks;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return tasks;
		}
	}
}
