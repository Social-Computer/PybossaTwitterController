package sociam.pybossa.mongodb;

import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.TwitterMethods;

public class TestEmbedingTwitterSnippet {

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		String a = "https://api.twitter.com/1/statuses/oembed.json?id=718196511608152064";
		JSONObject json = TwitterMethods.getOembed(a);
		System.out.println(json.toString());

		JSONObject jsonResponse = new JSONObject();
		jsonResponse = MongodbMethods.getStatsFroRest(Config.taskCollection, null, null, 0, 3);
		System.out.println(jsonResponse.toString());

	}
}
