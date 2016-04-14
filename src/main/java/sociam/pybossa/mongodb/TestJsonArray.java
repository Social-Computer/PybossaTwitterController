package sociam.pybossa.mongodb;

import java.util.ArrayList;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;

public class TestJsonArray {
	public static void main(String[] args) {
		Config.reload();
		JSONObject data = new JSONObject();
		ArrayList<Document> taskRuns = new ArrayList<Document>();
		taskRuns = MongodbMethods.getTaskRunsFromMongoDB(16149589);
		JSONArray array = new JSONArray();
		for (Document document : taskRuns) {
			JSONObject obj = new JSONObject(document.toJson().toString());
			array.put(obj);
		}
		data.put("taskRuns", array);
		data.put("status", "success");
		System.out.println(data.toString());
	}
}
