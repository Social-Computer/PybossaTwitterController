package recoin.mongodb_version.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.config.Config;

public class TestTranslation {
	final static Logger logger = Logger.getLogger(TestTranslation.class);

	public static void main(String[] args) {

		JSONObject json = getTranslation("Virus & : What You Need To Know",
				"en", "plain", 1);
		System.out.println(json.toString());
		JSONArray transaltionArray = json.getJSONArray("text");
		String translationText = transaltionArray.getString(0);
		System.out.println("Text " + translationText);
	}

	public static JSONObject getTranslation(String text, String lang,
			String format, int options) {

		String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?"
				+ "key=" + Config.yandexKey + "&text=" + text + "&lang=" + lang
				+ "&format=" + format + "&options=" + options + "";

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			int responseCode = con.getResponseCode();
			logger.debug("\nSending 'GET' request to URL : " + url);
			logger.debug("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			logger.debug("Yandex Response " + response.toString());
			JSONObject json = null;
			if (responseCode == 200) {
				json = new JSONObject(response.toString());
				return json;
			} else {
				return null;
			}

		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}

	}
}
