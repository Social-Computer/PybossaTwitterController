package sociam.pybossa.twitter;

import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.methods.TwitterMethods;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.Twitter;

public class JsonByID {

	public static void main(String[] args) {
		Twitter twitter = TwitterAccount.setTwitterAccount(2);
		JSONObject json = TwitterMethods.getTweetByID("721304143697219584",
				twitter);
		System.out.println(json);

		String media_url = null;
		JSONObject entities = json.getJSONObject("entities");
		if (entities != null) {
			System.out.println(entities);
			if (entities.has("media")) {
				JSONArray media = entities.getJSONArray("media");
				if (media != null) {
					for (int i = 0; i < media.length(); i++) {
						JSONObject oneMedia = media.getJSONObject(i);
						if (oneMedia.has("type")) {
							if (oneMedia.getString("type").equals("photo")) {
								media_url = oneMedia.getString("media_url");
							}
						}
					}
				}
			} else {
				System.out.println("no media");
			}
		} else {
			System.out.println("no entities");
		}

		System.out.println("Media " + media_url);

	}

}
