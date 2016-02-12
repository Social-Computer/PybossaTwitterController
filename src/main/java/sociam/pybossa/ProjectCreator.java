package sociam.pybossa;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

public class ProjectCreator {
	final static Logger logger = Logger.getLogger(ProjectCreator.class);

	public static String host = "http://recoin.cloudapp.net:5000";
	public static String projectDir = "/api/project";
	public static String api_key = "?api_key=acc193bd-6930-486e-9a21-c80005cbbfd2";

	public static void main(String[] args) {

		String jsonData = BuildJsonPorject("test2", "test2", "test2");
		String url = host + projectDir + api_key;
		createProject(url, jsonData);

	}

	/**
	 * This method creates a project on a given url that accepts json doc - in
	 * this case its the PyBossa url and credintials
	 * 
	 * @param url
	 *            the like for the host alongside credintials.
	 * @param jsonData
	 *            the json doc which should have the project parms.
	 * 
	 * @return Boolean true if its created, false otherwise.
	 **/
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

	public static String BuildJsonPorject(String name, String shortName, String description) {

		JSONObject app = new JSONObject();

		app.put("name", name);
		app.put("short_name", shortName);
		app.put("description", description);

		return app.toJSONString();
	}

}
