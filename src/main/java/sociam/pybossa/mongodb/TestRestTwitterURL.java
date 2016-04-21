package sociam.pybossa.mongodb;

import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;

public class TestRestTwitterURL {

	public static void main(String[] args) {
		JSONObject json = MongodbMethods.getStatsFroRest(Config.taskCollection, null, null, 0, 2);
		System.out.println(json.toString());
	}
}
