package sociam.pybossa.methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.config.Config;

public class PybossaMethods {
	
	final static Logger logger = Logger.getLogger(PybossaMethods.class);
	
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
	public static JSONObject createProjectInPyBossa(String url,
			JSONObject jsonData) {
		JSONObject jsonResult = null;
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString(), "utf-8");
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
	public static JSONObject BuildJsonPorject(String name, String shortName,
			String description, String templeteFile) {

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
		app.put("published", true);
		app.put("owner_id", 1);
		app.put("featured", false);
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
	
	

	public static JSONObject inserTaskIntoPyBossa(String url,
			JSONObject jsonData) {
		JSONObject jsonResult = null;
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString(), "utf-8");
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch,utf-8");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));
			String output;
			logger.debug("Output from Server ...."
					+ response.getStatusLine().getStatusCode() + "\n");
			while ((output = br.readLine()) != null) {
				logger.debug(output);
				jsonResult = new JSONObject(output);
			}
			if (response.getStatusLine().getStatusCode() == 200
					|| response.getStatusLine().getStatusCode() == 204) {

				return jsonResult;
			} else {
				logger.error("PyBossa response failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
				logger.error("response " + response);
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
	public static JSONObject BuildJsonTaskContent(String text,
			String n_answers, String quorum, String calibration,
			int project_id, String priority_0, String media_url) {
		try {
			JSONObject app = new JSONObject();
			app.put("text", text);
			app.put("media_url", media_url);
			JSONObject app2 = new JSONObject();
			app2.put("info", app);
			app2.put("n_answers", n_answers);
			app2.put("quorum", quorum);
			app2.put("calibration", calibration);
			app2.put("project_id", project_id);
			app2.put("priority_0", priority_0);

			return app2;
		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}

	}
	
	public static ArrayList<String> getAllTasksTextsFromPyBossa(int project_id) {

		String url = Config.PyBossahost + Config.taskDir + "?project_id="
				+ project_id + "&limit=" + Config.TasksPerProject;

		ArrayList<String> texts = new ArrayList<>();

		HttpURLConnection con;
		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			// int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
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

			logger.info("text size " + texts.size());
			return texts;
		} catch (IOException e) {
			logger.error("Error ", e);
			return null;
		}

	}
	
	
	public static JSONObject insertTaskRunIntoPyBossa(String url, JSONObject jsonData) {
		JSONObject jsonResult = null;
		logger.debug("Json to be inserted into PyBossa:  " + jsonData);
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString(), "utf-8");
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			logger.debug("Output from Server ...." + response.getStatusLine().getStatusCode() + "\n");
			StringBuffer responseText = new StringBuffer();
			while ((output = br.readLine()) != null) {

				responseText.append(output);
			}
			jsonResult = new JSONObject(responseText);
			logger.debug("Post Response " + responseText);
			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 204) {
				return jsonResult;
			} else {
				logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				logger.error("Message " + response.getStatusLine());
				logger.error(response.toString());
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
	public static JSONObject BuildJsonTaskRunContent(String answer, int task_id, int project_id) {

		JSONObject app2 = new JSONObject();
		app2.put("project_id", project_id);

		app2.put("info", answer);
		app2.put("task_id", task_id);
		Random rn = new Random();
		int randomNum = rn.nextInt((250 - 1) + 1) + 1;
		String ip = "80.44.192." + randomNum;
		app2.put("user_ip", ip);
		logger.debug("Generated ip " + ip);
		return app2;
	}
	
	
	// public static Boolean getReqest(int project_id, String postURL,
	// JSONObject jsonData) {
	// String url = Config.PyBossahost + Config.projectDir + "/" + project_id +
	// "/newtask";
	// logger.debug("Inserting task run into PyBossa");
	//
	// HttpURLConnection con = null;
	// try {
	//
	// URL obj = new URL(url);
	// con = (HttpURLConnection) obj.openConnection();
	// // optional default is GET
	// con.setRequestMethod("GET");
	// con.setConnectTimeout(10000);
	// int responseCode = con.getResponseCode();
	// // System.out.println("\nSending 'GET' request to URL : " + url);
	// // System.out.println("Response Code : " + responseCode);
	//
	// BufferedReader in = new BufferedReader(new
	// InputStreamReader(con.getInputStream()));
	// String inputLine;
	// StringBuffer response = new StringBuffer();
	//
	// while ((inputLine = in.readLine()) != null) {
	// response.append(inputLine);
	// }
	// logger.debug("PyBossa get reqesut for Project_id: " + response);
	// if (responseCode == 200 || responseCode == 204) {
	// JSONObject postRequest = insertTaskRunIntoPyBossa(postURL, jsonData);
	// in.close();
	// if (postRequest != null) {
	// return true;
	// } else {
	// return false;
	// }
	// } else {
	// logger.error("GET request was not successful " + response);
	// return false;
	// }
	//
	// } catch (
	//
	// IOException e)
	//
	// {
	// logger.error("Error ", e);
	// return false;
	//
	// }
	//
	// }

	
	
	// public static Boolean getReqest(int project_id, String postURL,
	// JSONObject jsonData) {
	// String url = Config.PyBossahost + Config.projectDir + "/" + project_id +
	// "/newtask";
	//
	// HttpURLConnection con = null;
	// try {
	//
	// URL obj = new URL(url);
	// con = (HttpURLConnection) obj.openConnection();
	// // optional default is GET
	// con.setRequestMethod("GET");
	// con.setConnectTimeout(10000);
	// int responseCode = con.getResponseCode();
	// // System.out.println("\nSending 'GET' request to URL : " + url);
	// // System.out.println("Response Code : " + responseCode);
	//
	// BufferedReader in = new BufferedReader(new
	// InputStreamReader(con.getInputStream()));
	// String inputLine;
	// StringBuffer response = new StringBuffer();
	//
	// while ((inputLine = in.readLine()) != null) {
	// response.append(inputLine);
	// }
	// logger.debug("PyBossa get reqesut for Project_id: " + response);
	// if (responseCode == 200 || responseCode == 204) {
	// JSONObject postRequest = insertTaskRunIntoPyBossa(postURL, jsonData);
	// in.close();
	// if (postRequest != null) {
	// return true;
	// } else {
	// return true;
	// }
	// } else {
	// return false;
	// }
	//
	// } catch (
	//
	// IOException e)
	//
	// {
	// logger.error("Error ", e);
	// return false;
	//
	// }
	//
	// }
}
