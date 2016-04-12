package sociam.pybossa.mongodb;


import org.json.JSONObject;

import sociam.pybossa.methods.MongodbMethods;

public class TestGetTasks {

	public static void main(String[] args) {
		JSONObject tasks = MongodbMethods.getTasks(0);
		System.out.println(tasks.toString());
	}

}
