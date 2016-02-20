package sociam.pybossa.test1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class TaskRun {

	final static Logger logger = Logger.getLogger(TaskRun.class);

	public static String host = "http://recoin.cloudapp.net:5000";
	public static String projectDir = "/api/taskrun";
	public static String api_key = "?api_key=acc193bd-6930-486e-9a21-c80005cbbfd2";

	public static void main(String[] args) {
		BasicConfigurator.configure();
		JSONObject jsonData = BuildJsonTaskContent("no", "119", "25");
		// String jsonData = "{\"info\": {\"text\": \"#Zika News: Stop The Zika
		// Virus https://t.co/tYqAYlbPlc #PathogenPosse\"}, \"n_answers\": 30,
		// \"quorum\": 0, \"calibration\": 0, \"project_id\": 11,
		// \"priority_0\": 0.0}";
		String url = host + projectDir;
		getReqest();

		createProject(url, jsonData);

	}

	public static void getReqest() {
		String url = "http://recoin.cloudapp.net:5000/api/project/25/newtask";

		HttpURLConnection con;
		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static JSONObject createProject(String url, JSONObject jsonData) {
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
	private static JSONObject BuildJsonTaskContent(String answer,
			String task_id, String project_id) {

		JSONObject app2 = new JSONObject();
		app2.put("project_id", 25);

		app2.put("info", answer);
		app2.put("task_id", 119);
		app2.put("user_ip", "80.44.145.144");
		return app2;
	}

}
