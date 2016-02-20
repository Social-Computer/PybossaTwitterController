package sociam.pybossa.test1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import sociam.pybossa.config.Config;

public class TestProjectCreator {
	final static Logger logger = Logger.getLogger(TestProjectCreator.class);

	public static void main(String[] args) {
		BasicConfigurator.configure();

		JSONObject jsonData = BuildJsonPorject("test15", "test15", "test15", Config.project_validation_templatePath);
		String url = Config.PyBossahost + Config.projectDir + Config.api_key;
		createProject(url, jsonData);

	}

	/**
	 * This method creates a project on a given url that accepts json doc - in
	 * this case its the PyBossa url and credentials
	 * 
	 * @param url
	 *            the like for the host alongside credentials.
	 * @param jsonData
	 *            the json doc which should have the project parms.
	 * 
	 * @return Boolean true if its created, false otherwise.
	 **/
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
	 * This returns a json string from a given project's details
	 * 
	 * @param name
	 *            the name of the project
	 * @param shortName
	 *            a short name for the project
	 * @param description
	 *            a description for the project // This could be incrimental
	 *            later!
	 * @return Json string
	 */
	private static JSONObject BuildJsonPorject(String name, String shortName, String description, String templeteFile) {

		JSONObject app2 = new JSONObject();
		String templete = readFile(templeteFile);
		templete = templete.replaceAll("\\[project short name\\]", shortName);
		app2.put("task_presenter", templete);
		JSONObject app = new JSONObject();
		app.put("name", name);
		app.put("short_name", shortName);
		app.put("description", description);
		// app.put("created", true);
		app.put("allow_anonymous_contributors", true);
		// TODO: publishing through the api is not allowed - we leave it to be
		// done manually!
		// app.put("published", true);
		app.put("info", app2);

		return app;
	}

	static String readFile(String path) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			logger.error(e);
		}
		return new String(encoded, StandardCharsets.UTF_8);
	}
}
