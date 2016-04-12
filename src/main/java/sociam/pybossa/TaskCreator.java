package sociam.pybossa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.PybossaMethods;

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
			HashSet<JSONObject> projectsAsJsons = MongodbMethods.getReadyProjects();
			if (projectsAsJsons != null) {
				logger.info("There are "
						+ projectsAsJsons.size()
						+ " projects that have tasks ready to be inserted into PyBossa, then to MongoDB");
				if (!projectsAsJsons.isEmpty()) {

					// Get project name and id for these started projects
					for (JSONObject jsonObject : projectsAsJsons) {
						String bin_id = jsonObject.getString("bin_id");
						int project_id = jsonObject.getInt("project_id");
						int tasksPerProjectlimit = Integer
								.valueOf(Config.TasksPerProject);
						ArrayList<String> tasksTexts = PybossaMethods
								.getAllTasksTextsFromPyBossa(project_id);
						if (tasksTexts != null) {
							if (tasksTexts.size() >= tasksPerProjectlimit) {
								logger.debug("Project with id " + project_id
										+ " has already got "
										+ tasksPerProjectlimit + " tasks");
								MongodbMethods.updateProjectToInsertedInMongoDB(project_id);
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
							MongodbMethods.updateProjectToInsertedInMongoDB(project_id);
							logger.debug("changing to another project");
							continue;

						}
						// TODO: don't retrieve ones which have already been
						// pushed
						// to
						// crowd and not completed by crowd, from Ramine
						HashSet<Document> tweets = MongodbMethods
								.getTweetsFromBinInMongoDB(bin_id);
						HashSet<String> originalBinText = new HashSet<>();
						logger.info("There are \"" + tweets.size()
								+ "\" tweets for projectID " + project_id);
						for (Document tweet : tweets) {

							logger.debug("Processing a new task");
							// for each bin, get the text/tweet
							String text = tweet.getString("text");
							logger.debug("tweet text " + text);
							if (!originalBinText.contains(text)) {
								originalBinText.add(text);
								String text_encoded = tweet
										.getString("text_encoded");

								ObjectId _id = tweet.getObjectId("_id");
								if (tasksTexts != null) {
									if (!tasksTexts.contains(text_encoded)) {

										// Build the PyBossa json for
										// insertion
										// of a
										// task
										String media_url = "";
										media_url = tweet
												.getString("media_url");

										JSONObject PyBossaTaskJsonToBeInserted = PybossaMethods
												.BuildJsonTaskContent(text,
														"5", "0", "0",
														project_id, "0.0",
														media_url);
										if (PyBossaTaskJsonToBeInserted != null) {
											// Insert the PyBossa json into
											// PyBossa
											JSONObject pybossaResponse = PybossaMethods
													.inserTaskIntoPyBossa(url,
															PyBossaTaskJsonToBeInserted);
											if (pybossaResponse != null) {
												JSONObject info = pybossaResponse
														.getJSONObject("info");
												String task_text = info
														.getString("text");
												tasksTexts.add(task_text);

												// Insert the resonse of
												// PyBossa
												// into
												// MongoDB
												if (MongodbMethods
														.insertTaskIntoMongoDB(
																pybossaResponse,
																"ready",
																"validate")) {
													logger.debug("task with pybossaResponse "
															+ pybossaResponse
																	.toString());
													if (MongodbMethods
															.updateBinString(
																	_id,
																	task_text,
																	bin_id)) {
														logger.debug("Bin with _id "
																+ _id
																+ " was updated");
														tasksPerProjectCounter++;
													} else {
														logger.error("Bin with _id "
																+ _id
																+ "  was not updated ");
													}
												} else {
													logger.error("Task was not inserted Into MongoDB");
												}
											} else {
												logger.error("pybossaResponse was null");
											}
										} else {
											logger.error("PyBossaTaskJsonToBeInserted was null");
										}
									} else {
										logger.error("task " + text
												+ " in Project " + project_id
												+ " is already in PyBossa!!");
									}
								}
							} else {
								logger.error("Tweet is already processed "
										+ tweet.toString());
							}
						}

					}
				} else {
					logger.debug("There are no ready projects' tasks to be inserted into PyBossa!");
				}
			}
		} catch (

		Exception e)

		{
			logger.error("Error ", e);
		}

	}

}
