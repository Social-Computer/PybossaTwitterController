package sociam.pybossa;

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.log4j.BasicConfigurator;
import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class TestBins {
	static MongoClient mongoClient = new MongoClient("recoin.cloudapp.net",
			Config.mongoPort);

	static MongoDatabase database = mongoClient
			.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {
		BasicConfigurator.configure();
		HashSet<JSONObject> tasksAsJsons = getStartedProjects();
		if (!tasksAsJsons.isEmpty()) {
			for (JSONObject jsonObject : tasksAsJsons) {
				System.out.println(jsonObject.toString());
			}
		}

	}
	private static HashSet<JSONObject> getStartedProjects() {

		HashSet<JSONObject> jsons = new LinkedHashSet<JSONObject>();
		FindIterable<Document> iterable = database.getCollection(
				Config.projectCollection).find(
				new Document("project_started", "false"));

		if (iterable.first() != null) {
			iterable.forEach(new Block<Document>() {
				@Override
				public void apply(final Document document) {
					JSONObject app2 = new JSONObject(document);
					jsons.add(app2);
				}
			});
			return jsons;
		} else {

		}
		return jsons;
	}

}
