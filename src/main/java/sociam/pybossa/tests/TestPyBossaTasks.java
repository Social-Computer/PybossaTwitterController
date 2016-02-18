package sociam.pybossa.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestPyBossaTasks {

	public static void main(String[] args) {

		String url = "http://recoin.cloudapp.net:5000/api/task?project_id=37";
		
		HashSet<String> texts = new HashSet<>();

		HttpURLConnection con;
		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			System.out.println(response.toString());
			// print result
			JSONArray jsonData = new JSONArray(response.toString());
			System.out.println(jsonData);
			for (Object object : jsonData) {
				JSONObject json = new JSONObject(object.toString());
				JSONObject info = json.getJSONObject("info");
				String text = info.getString("text");

				texts.add(text);
				
			}
			

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
