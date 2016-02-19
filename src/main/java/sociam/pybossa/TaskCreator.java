package sociam.pybossa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class TaskCreator {

	final static Logger logger = Logger.getLogger(TaskCreator.class);

	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss.SSSSSS");

	static MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
	static MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);

	static MongoDatabase binsDatabase = mongoClient.getDatabase(Config.binsDatabaseName);

	static String url = Config.PyBossahost + Config.taskDir + Config.api_key;

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		logger.info("TaskCreator will be repeated every " + Config.TaskCreatorTrigger + " ms");
		try {
			while (true) {
				run();
				logger.info("Sleeping for " + Config.TaskCreatorTrigger + " ms");
				Thread.sleep(Integer.valueOf(Config.TaskCreatorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error " + e);
		}

	}

	public static void run() {
		try {
			// Check for started projects
			HashSet<JSONObject> projectsAsJsons = getReadyProjects();
			if (projectsAsJsons != null) {
				logger.info("There are " + projectsAsJsons.size()
						+ " projects that have tasks ready to be inserted into PyBossa, then to MongoDB");
				if (!projectsAsJsons.isEmpty()) {

					// Get project name and id for these started projects
					for (JSONObject jsonObject : projectsAsJsons) {
						JSONArray bin_id = jsonObject.getJSONArray("bin_ids");
						int project_id = jsonObject.getInt("project_id");
						int tasksPerProjectlimit = Integer.valueOf(Config.TasksPerProject);
						ArrayList<String> tasksTexts = getAllTasksTextsFromPyBossa(project_id);
						if (tasksTexts.size() > tasksPerProjectlimit) {
							if (updateProjectToInsertedInMongoDB(project_id)) {
								logger.debug("Project with id " + project_id + " has already got "
										+ tasksPerProjectlimit + " tasks");
								break;
							}
						}
						int tasksPerProjectCounter = 0;
						if (tasksPerProjectCounter > tasksPerProjectlimit) {
							logger.info("tasksPerProjectlimit was reached " + tasksPerProjectCounter);
							if (updateProjectToInsertedInMongoDB(project_id)) {
								logger.debug("changing to another project");
								break;
							}
						}
						// TODO: don't retrieve ones which have already been
						// pushed
						// to
						// crowd and not completed by crowd, from Ramine
						for (Object object : bin_id) {
							String binItem = (String) object;
							// for each started project, get their bins
							HashSet<Document> tweets = getTweetsFromBinInMongoDB(binItem);
							HashSet<String> originalBinText = new HashSet<>();
							logger.info("There are \"" + tweets.size() + "\" tweets for projectID " + project_id);
							for (Document tweet : tweets) {

								// for each bin, get the text/tweet
								String text = tweet.getString("text");
								if (!originalBinText.contains(text)) {
									originalBinText.add(text);
									String text_encoded = tweet.getString("text_encoded");

									ObjectId _id = tweet.getObjectId("_id");
									if (tasksTexts != null) {
										if (!tasksTexts.contains(text_encoded)) {

											// Build the PyBossa json for
											// insertion
											// of a
											// task
											JSONObject PyBossaTaskJsonToBeInserted = BuildJsonTaskContent(text, "30",
													"0", "0", project_id, "0.0");
											if (PyBossaTaskJsonToBeInserted != null) {
												// Insert the PyBossa json into
												// PyBossa
												JSONObject pybossaResponse = inserTaskIntoPyBossa(url,
														PyBossaTaskJsonToBeInserted);
												if (pybossaResponse != null) {
													JSONObject info = pybossaResponse.getJSONObject("info");
													String task_text = info.getString("text");
													tasksTexts.add(task_text);

													// Insert the resonse of
													// PyBossa
													// into
													// MongoDB
													if (insertTaskIntoMongoDB(pybossaResponse, false)) {
														logger.debug("task with pybossaResponse "
																+ pybossaResponse.toString());
														if (updateBinString(_id, task_text, binItem)) {
															logger.debug("Bin with _id " + _id + " was updated");
															tasksPerProjectCounter++;
														} else {
															logger.error("Bin with _id " + _id + "  was not updated ");
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
											logger.error("task " + text + " in Project " + project_id
													+ " is already in PyBossa!!");
										}
									}
								} else {
									logger.error("Tweet is already processed " + tweet.toString());
								}
							}
						}
					}
				} else {
					logger.debug("There are no ready projects' tasks to be inserted into PyBossa!");
				}
			}
		} catch (Exception e) {
			logger.error("Erro " + e);
		}
	}

	private static Boolean updateProjectToInsertedInMongoDB(int project_id) {
		try {
			UpdateResult result = database.getCollection(Config.projectCollection).updateOne(
					new Document("project_id", project_id),
					new Document().append("$set", new Document("project_status", "inserted")));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.projectCollection + " Collection was updated with project_status: inserted");
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			logger.error("Error " + e);
			return false;
		}
	}

	// For encoding issue that makes the text changed after inserting it into
	// PyBossa
	private static Boolean updateBinString(ObjectId _id, String text_encoded, String binItem) {
		try {
			UpdateResult result = binsDatabase.getCollection(binItem).updateOne(new Document("_id", _id),
					new Document().append("$set", new Document("text_encoded", text_encoded)));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			logger.error("Error " + e);
			return false;
		}
	}

	static HashSet<Document> tweetsjsons = new LinkedHashSet<Document>();

	private static HashSet<Document> getTweetsFromBinInMongoDB(String collectionName) {

		tweetsjsons = new LinkedHashSet<Document>();
		try {

			FindIterable<Document> iterable = binsDatabase.getCollection(collectionName).find();
			if (iterable.first() != null) {
				iterable.forEach(new Block<Document>() {
					@Override
					public void apply(final Document document) {
						tweetsjsons.add(document);
					}
				});
				return tweetsjsons;
			}
			return tweetsjsons;
		} catch (Exception e) {
			logger.error("Error " + e);
			return tweetsjsons;
		}
	}

	static HashSet<JSONObject> startedProjectsJsons = new LinkedHashSet<JSONObject>();

	private static HashSet<JSONObject> getReadyProjects() {
		startedProjectsJsons = new LinkedHashSet<JSONObject>();
		try {

			FindIterable<Document> iterable = database.getCollection(Config.projectCollection)
					.find(new Document("project_status", "ready"));
			if (iterable.first() != null) {
				iterable.forEach(new Block<Document>() {
					@Override
					public void apply(final Document document) {
						JSONObject app2 = new JSONObject(document);
						startedProjectsJsons.add(app2);
					}
				});
				return startedProjectsJsons;
			}
			return startedProjectsJsons;
		} catch (Exception e) {
			logger.error("Error " + e);
			return null;
		}
	}

	private static JSONObject inserTaskIntoPyBossa(String url, JSONObject jsonData) {
		JSONObject jsonResult = null;
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString());
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 204) {
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				String output;
				logger.debug("Output from Server ...." + response.getStatusLine().getStatusCode() + "\n");
				while ((output = br.readLine()) != null) {
					logger.debug(output);
					jsonResult = new JSONObject(output);
				}
				return jsonResult;
			} else {
				logger.error("PyBossa response failed : HTTP error code : " + response.getStatusLine().getStatusCode());
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
	private static JSONObject BuildJsonTaskContent(String text, String n_answers, String quorum, String calibration,
			int project_id, String priority_0) {
		try {
			JSONObject app = new JSONObject();
			app.put("text", text);
			JSONObject app2 = new JSONObject();
			app2.put("info", app);
			app2.put("n_answers", n_answers);
			app2.put("quorum", quorum);
			app2.put("calibration", calibration);
			app2.put("project_id", project_id);
			app2.put("priority_0", priority_0);
			return app2;
		} catch (Exception e) {
			logger.error("Error " + e);
			return null;
		}

	}

	private static Boolean insertTaskIntoMongoDB(JSONObject response, Boolean isPushedToTwitter) {

		try {
			Integer pybossa_task_id = response.getInt("id");
			// String created_String = response.getString("created");
			// Date publishedAt = PyBossaformatter.parse(created_String);
			Date date = new Date();
			String publishedAt = MongoDBformatter.format(date);
			String targettedFormat = MongoDBformatter.format(publishedAt);
			Integer project_id = response.getInt("project_id");
			JSONObject info = response.getJSONObject("info");
			String task_text = info.getString("text");
			logger.debug("Inserting a task into MongoDB");
			if (pushTaskToMongoDB(pybossa_task_id, targettedFormat, project_id, isPushedToTwitter, task_text)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error " + e);
			return false;
		}

	}

	private static boolean pushTaskToMongoDB(Integer pybossa_task_id, String publishedAt, Integer project_id,
			Boolean isPushedToTwitter, String task_text) {

		try {
			if (publishedAt != null && project_id != null && isPushedToTwitter != null && task_text != null) {

				FindIterable<Document> iterable = database.getCollection(Config.taskCollection)
						.find(new Document("project_id", project_id).append("task_text", task_text));
				if (iterable.first() == null) {
					database.getCollection(Config.taskCollection)
							.insertOne(new Document().append("pybossa_task_id", pybossa_task_id)
									.append("publishedAt", publishedAt).append("project_id", project_id)
									.append("isPushed", isPushedToTwitter).append("task_text", task_text));
					logger.debug("One task is inserted into MongoDB");

				} else {
					logger.error("task is already in the collection!!");
				}

			}
			return true;
		} catch (Exception e) {
			logger.error("Error with inserting the task " + "pybossa_task_id " + pybossa_task_id + "publishedAt "
					+ publishedAt + "project_id " + project_id + "isPushed " + isPushedToTwitter + "task_text "
					+ task_text + "\n" + e);
			return false;
		}

	}

	private static ArrayList<String> getAllTasksTextsFromPyBossa(int project_id) {

		String url = Config.PyBossahost + Config.taskDir + "?project_id=" + project_id;

		ArrayList<String> texts = new ArrayList<>();

		HttpURLConnection con;
		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			// int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			JSONArray jsonData = new JSONArray(response.toString());
			for (Object object : jsonData) {
				JSONObject json = new JSONObject(object.toString());
				JSONObject info = json.getJSONObject("info");
				String text = info.getString("text");
				texts.add(text);
			}

			return texts;
		} catch (IOException e) {
			logger.error("Error " + e);
			return null;
		}

	}

}
