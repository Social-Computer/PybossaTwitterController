package sociam.pybossa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class TaskCreator {

	final static Logger logger = Logger.getLogger(TaskCreator.class);

	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss.SSSSSS");

	static MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
	static MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);

	static MongoDatabase binsDatabase = mongoClient.getDatabase(Config.binsDatabaseName);
	static String url = Config.PyBossahost + Config.taskDir + Config.api_key;

	public static void main(String[] args) {
		BasicConfigurator.configure();

		// Check for started projects
		HashSet<JSONObject> projectsAsJsons = getStartedProjects();
		logger.info("There are " + projectsAsJsons.size()
				+ " projects that have tasks ready to be inserted into PyBossa, then to MongoDB");
		if (!projectsAsJsons.isEmpty()) {

			// Get project name and id for these started projects
			for (JSONObject jsonObject : projectsAsJsons) {
				JSONArray bin_id = jsonObject.getJSONArray("bin_ids");
				int project_id = jsonObject.getInt("project_id");

				// TODO: don't retrieve ones which have already been pushed to
				// crowd and not completed by crowd, from Ramine
				for (Object object : bin_id) {
					String binItem = (String) object;
					// for each started project, get their bins
					HashSet<JSONObject> binsAsJsons = getBinsFromMongoDB(binItem);
					ArrayList<String> tasksTexts = getAllTasksTextsFromPyBossa(project_id);
					System.out.println("Size " + tasksTexts.size());
					for (JSONObject oneBin : binsAsJsons) {

						// for each bin, get the text/tweet
						String text = oneBin.getString("text");

						System.out.println(tasksTexts.get(0));
						String escaped = StringEscapeUtils.unescapeJson(tasksTexts.get(0));
						System.out.println(escaped);
						System.out.println(text);
						String escaped2 = StringEscapeUtils.unescapeJson(text);
						System.out.println(escaped2);
						if (!tasksTexts.contains(text)) {

							// Build the PyBossa json for insertion of a task
							JSONObject PyBossaTaskJsonToBeInserted = BuildJsonTaskContent(text, "30", "0", "0",
									project_id, "0.0");
							if (PyBossaTaskJsonToBeInserted != null) {
								// Insert the PyBossa json into PyBossa
								JSONObject pybossaResponse = inserTaskIntoPyBossa(url, PyBossaTaskJsonToBeInserted);

								if (pybossaResponse != null) {

									// Insert the resonse of PyBossa into
									// MongoDB
									insertTaskIntoMongoDB(pybossaResponse, false);
								} else {
									logger.error("pybossaResponse was null");
								}
							} else {
								logger.error("PyBossaTaskJsonToBeInserted was null");
							}
							break;
						} else {
							logger.error("task " + text + " in Project " + project_id + " is already in PyBossa!!");
						}
					}
				}
			}
		} else {
			logger.debug("There are no ready projects' tasks to be inserted into PyBossa!");
		}
	}

	private static HashSet<JSONObject> getBinsFromMongoDB(String collectionName) {
		HashSet<JSONObject> jsons = new LinkedHashSet<JSONObject>();
		FindIterable<Document> iterable = binsDatabase.getCollection(collectionName).find();
		if (iterable.first() != null) {
			iterable.forEach(new Block<Document>() {
				@Override
				public void apply(final Document document) {
					JSONObject app2 = new JSONObject(document);
					jsons.add(app2);
				}
			});
			return jsons;
		} else {

		}
		return jsons;
	}

	private static HashSet<JSONObject> getStartedProjects() {

		HashSet<JSONObject> jsons = new LinkedHashSet<JSONObject>();
		FindIterable<Document> iterable = database.getCollection(Config.projectCollection)
				.find(new Document("project_started", "true"));

		if (iterable.first() != null) {
			iterable.forEach(new Block<Document>() {
				@Override
				public void apply(final Document document) {
					JSONObject app2 = new JSONObject(document);
					jsons.add(app2);
				}
			});
			return jsons;
		} else {

		}
		return jsons;
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
				logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
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
			logger.error(e);
			return null;
		}

	}

	private static Boolean insertTaskIntoMongoDB(JSONObject response, Boolean isPushed) {

		try {
			Integer pybossa_task_id = response.getInt("id");
			String created_String = response.getString("created");
			Date publishedAt = PyBossaformatter.parse(created_String);
			String targettedFormat = MongoDBformatter.format(publishedAt);
			Integer project_id = response.getInt("project_id");
			JSONObject info = response.getJSONObject("info");
			String task_text = info.getString("text");
			logger.debug("Inserting a task into MongoDB");
			if (pushTaskToMongoDB(pybossa_task_id, targettedFormat, project_id, isPushed, task_text)) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			logger.error(e);
		}

		return true;
	}

	private static boolean pushTaskToMongoDB(Integer pybossa_task_id, String publishedAt, Integer project_id,
			Boolean isPushed, String task_text) {

		try {
			if (publishedAt != null && project_id != null && isPushed != null && task_text != null) {

				FindIterable<Document> iterable = database.getCollection(Config.taskCollection)
						.find(new Document("project_id", project_id).append("task_text", task_text));
				if (iterable.first() == null) {
					database.getCollection(Config.taskCollection)
							.insertOne(new Document().append("pybossa_task_id", pybossa_task_id)
									.append("publishedAt", publishedAt).append("project_id", project_id)
									.append("isPushed", isPushed).append("task_text", task_text));
					logger.info("One task is inserted");

				} else {
					logger.error("task is already in the collection!!");
				}

			}
			return true;
		} catch (Exception e) {
			logger.error("Error with inserting the task " + "pybossa_task_id " + pybossa_task_id + "publishedAt "
					+ publishedAt + "project_id " + project_id + "isPushed " + isPushed + "task_text " + task_text
					+ "\n" + e);
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
			int responseCode = con.getResponseCode();
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
				System.out.println("textis " + info.getString("text"));
				String text = info.getString("text");
				texts.add(text);
			}

			return texts;
		} catch (IOException e) {
			logger.error(e);
			return null;
		}

	}

}
