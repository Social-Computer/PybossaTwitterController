package sociam.pybossa.twitter;

import java.util.ArrayList;

import org.json.JSONObject;

import sociam.pybossa.methods.TwitterMethods;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.Twitter;

public class TestMentions {
	static Twitter twitter;

	public static void main(String[] args) {
		twitter = TwitterAccount.setTwitterAccount(2);
		ArrayList<JSONObject> mentions = TwitterMethods.getMentionsTimelineAsJsons(twitter);
		for (JSONObject jsonObject : mentions) {
			System.out.println(jsonObject.toString());
		}
	}

}
