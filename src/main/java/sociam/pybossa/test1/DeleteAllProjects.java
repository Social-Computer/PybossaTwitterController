package sociam.pybossa.test1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import sociam.pybossa.config.Config;

public class DeleteAllProjects {
	final static Logger logger = Logger.getLogger(DeleteAllProjects.class);

	public static void main(String[] args) throws IOException {

		JSONObject project = getReqest();
		while (project != null && !project.isNull("id")) {
			int prjectID = project.getInt("id");
			System.out.println("Deleting project with name "
					+ project.getString("name"));
			URL url = new URL(Config.PyBossahost + Config.projectDir + "/"
					+ prjectID + Config.api_key);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("DELETE");
			int responseCode = connection.getResponseCode();
			System.out.println("Response code " + responseCode);

			project = getReqest();
		}
		System.out.println("finished deleting");

	}

	public static JSONObject getReqest() {
		String url = Config.PyBossahost + Config.projectDir + Config.api_key;
		logger.debug("Inserting task run into PyBossa");

		HttpURLConnection con = null;
		try {

			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			logger.debug("PyBossa get reqesut for Project_id: " + response);
			if (responseCode == 200 || responseCode == 204) {
				if (response.toString().length() > 2) {
					String res = response.substring(1, response.toString()
							.length() - 1);
					JSONObject getRequestJson = new JSONObject(res);
					in.close();
					return getRequestJson;
				} else {
					return null;
				}
			} else {
				logger.error("GET request was not successful "
						+ response.toString());
				return null;
			}

		} catch (IOException e) {
			logger.error("Error ", e);
			return null;
		}

	}

}
