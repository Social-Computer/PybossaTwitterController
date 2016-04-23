package sociam.pybossa.mongodb;

import org.json.JSONObject;

import sociam.pybossa.methods.TwitterMethods;

public class TestEmbedingTwitterSnippet {

	public static void main(String[] args) {

		String a = "https://api.twitter.com/1/statuses/oembed.json?id=718196511608152064";
		JSONObject json = TwitterMethods.getOembed(a);
		System.out.println(json.toString());

	}
}
