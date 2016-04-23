package sociam.pybossa.mongodb;

import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.TwitterMethods;

public class TestEmbedingTwitterSnippet {

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");

		JSONObject jsonResponse = new JSONObject();
		jsonResponse = MongodbMethods.getStatsFroRest(Config.taskCollection, null, null, 0, 1000);
		System.out.println(jsonResponse.toString());

	}
}
