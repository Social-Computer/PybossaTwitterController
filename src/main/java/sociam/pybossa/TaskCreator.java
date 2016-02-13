package sociam.pybossa;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class TaskCreator {

	final static Logger logger = Logger.getLogger(TaskCreator.class);

	public static String host = "http://recoin.cloudapp.net:5000";
	public static String projectDir = "/api/task";
	public static String api_key = "?api_key=acc193bd-6930-486e-9a21-c80005cbbfd2";

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String jsonData = BuildJsonTaskContent("#Zika News: Stop The Zika Virus https://t.co/tYqAYlbPlc #PathogenPosse",
				"30", "0", "0", "24", "0.0");
		// String jsonData = "{\"info\": {\"text\": \"#Zika News: Stop The Zika
		// Virus https://t.co/tYqAYlbPlc #PathogenPosse\"}, \"n_answers\": 30,
		// \"quorum\": 0, \"calibration\": 0, \"project_id\": 11,
		// \"priority_0\": 0.0}";
		String url = host + projectDir + api_key;
		createProject(url, jsonData);

	}

	public static Boolean createProject(String url, String jsonData) {

		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData);
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
				}
				return true;
			} else {
				logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				return false;
			}
		} catch (Exception ex) {
			logger.error(ex);
			return false;
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
	@SuppressWarnings("unchecked")
	private static String BuildJsonTaskContent(String text, String n_answers, String quorum, String calibration,
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
		System.out.println(app2.toString());

		return app2.toJSONString();
	}

}
