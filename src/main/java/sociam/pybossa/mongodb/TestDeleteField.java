package sociam.pybossa.mongodb;

import org.bson.Document;

import sociam.pybossa.config.Config;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.UpdateResult;

public class TestDeleteField {
	static MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
	static MongoDatabase database = mongoClient.getDatabase(Config.binsDatabaseName);

	public static void main(String[] args) {

		MongoIterable<String> iterable = database.listCollectionNames();
		for (String string : iterable) {
			if (!string.contains("indexes")) {
				System.out.println("Collection " + string);
				UpdateResult result = database.getCollection(string).updateMany(new Document(),
						new Document().append("$set", new Document("wasProcessed", false)));
				System.out.println(result.toString());
			}
		}

	}
}
