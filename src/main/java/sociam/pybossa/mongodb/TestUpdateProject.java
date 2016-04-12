package sociam.pybossa.mongodb;

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import sociam.pybossa.config.Config;

public class TestUpdateProject {
	static MongoClient mongoClient = new MongoClient(Config.mongoHost,
			Config.mongoPort);
	static MongoDatabase database = mongoClient
			.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {

		// FindIterable<Document> iterable =
		// database.getCollection(Config.projectCollection)
		// .find(new Document("project_name", "rt"));
		//
		// if (iterable.first() != null) {
		// iterable.forEach(new Block<Document>() {
		// @Override
		// public void apply(final Document document) {
		// JSONObject app2 = new JSONObject(document);
		// ObjectId id = document.getObjectId("_id");
		// System.out.println(id);
		UpdateResult result = database.getCollection(Config.taskCollection)
				.updateMany(
						new Document("facebook_task_status","error"),
						new Document("$set", new Document("facebook_task_status",
								"ready")));
		if (result.wasAcknowledged()) {
			if (result.getMatchedCount() > 0) {
				System.out.println("fould a match " + result.getMatchedCount());
			}
		}
		System.out.println(result.toString());
	}

}
