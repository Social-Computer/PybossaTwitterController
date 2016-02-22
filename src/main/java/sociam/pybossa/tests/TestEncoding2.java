package sociam.pybossa.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import sociam.pybossa.ProjectCreator;
import sociam.pybossa.config.Config;

public class TestEncoding2 {
	final static Logger logger = Logger.getLogger(TestEncoding2.class);
	static String url = Config.PyBossahost + Config.taskDir + Config.api_key;
	public static void main(String[] args) {
		BasicConfigurator.configure();
		JSONObject PyBossaTaskJsonToBeInserted = BuildJsonTaskContent(
				"смартфоном из линейки", "30", "0", "0", 6378, "0.0");
		JSONObject info = PyBossaTaskJsonToBeInserted.getJSONObject("info");
		System.out.println("text " + info.getString("text"));
		String uni = " \\u0441\\u043c\\u0430\\u0440\\u0442\\u0444\\u043e\\u043d\\u043e\\u043c \\u0438\\u0437 \\u043b\\u0438\\u043d\\u0435\\u0439\\u043a\\u0438 90";
		System.out.println("unicioded " + StringEscapeUtils.unescapeJava(uni));
		inserTaskIntoPyBossa(url, PyBossaTaskJsonToBeInserted);
	}

	private static JSONObject inserTaskIntoPyBossa(String url, JSONObject jsonData) {
		JSONObject jsonResult = null;
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString(), "utf-8");
			
			String theString = convertStreamToString(params.getContent());
			System.out.println(theString);
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json;charset=utf-8");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 204) {
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent()), "UTF-8"));
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
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
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
}
