package sociam.pybossa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class TaskCreator {

	final static Logger logger = Logger.getLogger(TaskCreator.class);

	public static String host = "http://recoin.cloudapp.net:5000";
	public static String projectDir = "/api/task";
	public static String api_key = "?api_key=acc193bd-6930-486e-9a21-c80005cbbfd2";

	static final String mongoHost = "localhost";
	static final int port = 27017;
	static final String databaseName = "RECOIN_bins";
	static final String collectionName = "tasks";
	final static MongoClient mongoClient = new MongoClient(mongoHost, port);
	final static MongoDatabase database = mongoClient.getDatabase(databaseName);

	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat(
			"yyyy-mm-dd'T'hh:mm:ss.SSSSSS");

	public static void main(String[] args) {
		BasicConfigurator.configure();
		JSONObject jsonData = BuildJsonTaskContent(
				"#Zika NewsTests5555: what is this https://t.co/tYqAYlbPlc #PathogenPosse",
				"30", "0", "0", "26", "0.0");
		// String jsonData = "{\"info\": {\"text\": \"#Zika News: Stop The Zika
		// Virus https://t.co/tYqAYlbPlc #PathogenPosse\"}, \"n_answers\": 30,
		// \"quorum\": 0, \"calibration\": 0, \"project_id\": 11,
		// \"priority_0\": 0.0}";
		String url = host + projectDir + api_key;
		JSONObject response = createTask(url, jsonData);
		System.out.println(response.toString());
		System.out.println(insertTaskIntoPyBossa(response, false));
	}

	public static JSONObject createTask(String url, JSONObject jsonData) {
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
			if (response.getStatusLine().getStatusCode() == 200
					|| response.getStatusLine().getStatusCode() == 204) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(response.getEntity().getContent())));
				String output;
				logger.debug("Output from Server ...."
						+ response.getStatusLine().getStatusCode() + "\n");
				while ((output = br.readLine()) != null) {
					logger.debug(output);
					jsonResult = new JSONObject(output);
				}
				return jsonResult;
			} else {
				logger.error("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
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
	private static JSONObject BuildJsonTaskContent(String text,
			String n_answers, String quorum, String calibration,
			String project_id, String priority_0) {

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
	}

	private static Boolean insertTaskIntoPyBossa(JSONObject response, Boolean isPushed) {

		try {
			Integer pybossa_task_id = response.getInt("id");
			String created_String = response.getString("created");
			Date publishedAt = PyBossaformatter.parse(created_String);
			String targettedFormat = MongoDBformatter.format(publishedAt);
			Integer project_id = response.getInt("project_id");
			JSONObject info = response.getJSONObject("info");
			String task_text = info.getString("text"); 
			logger.debug("Inserting a task into MongoDB");
			if (pushTask(pybossa_task_id, targettedFormat, project_id,
					isPushed, task_text)) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			logger.error(e);
		}

		return true;
	}

	private static boolean pushTask(Integer pybossa_task_id,
			String publishedAt, Integer project_id, Boolean isPushed,
			String task_text) {

		try {
			if (publishedAt != null && project_id != null && isPushed != null
					&& task_text != null) {

				FindIterable<Document> iterable = database.getCollection(
						collectionName).find(
						new Document("project_id", project_id).append(
								"task_text", task_text));
				if (iterable.first() == null) {
					database.getCollection(collectionName).insertOne(
							new Document()
									.append("pybossa_task_id", pybossa_task_id)
									.append("publishedAt", publishedAt)
									.append("project_id", project_id)
									.append("isPushed", isPushed)
									.append("task_text", task_text));
					logger.info("One task is inserted");

				} else {
					logger.error("task is already in the collection!!");
				}

			}
			return true;
		} catch (Exception e) {
			logger.error("Error with inserting the task " + "pybossa_task_id "
					+ pybossa_task_id + "publishedAt " + publishedAt
					+ "project_id " + project_id + "isPushed " + isPushed
					+ "task_text " + task_text + "\n" + e);
			return false;
		}

	}

}
