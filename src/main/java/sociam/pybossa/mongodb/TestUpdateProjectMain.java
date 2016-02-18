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

import sociam.pybossa.Config;

public class TestUpdateProjectMain {
	static MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
	static MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {

		UpdateResult result = database.getCollection(Config.projectCollection).updateMany(
				new Document("project_name", "acquisition"),
				new Document().append("$set", new Document("project_started", "false")));
		System.out.println(result.toString());

	}

}
