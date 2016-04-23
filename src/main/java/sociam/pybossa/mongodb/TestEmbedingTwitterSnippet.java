package sociam.pybossa.mongodb;

import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.TwitterMethods;

public class TestEmbedingTwitterSnippet {

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");

//		String a = "https://twitter.com/statuses/718622697912008704";
//		JSONObject Json = TwitterMethods.getOembed(a);
//		System.out.println("Json " + Json);
		
		JSONObject jsonResponse = new JSONObject();
		jsonResponse = MongodbMethods.getStatsFroRest(Config.taskCollection, null, null, 0, 2000);
		System.out.println("Json " + jsonResponse.toString());

	}
}
