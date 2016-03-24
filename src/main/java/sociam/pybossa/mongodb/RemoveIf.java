package sociam.pybossa.mongodb;

import org.bson.Document;

import sociam.pybossa.config.Config;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class RemoveIf {
	static MongoClient mongoClient = new MongoClient(Config.mongoHost,
			Config.mongoPort);
	static MongoDatabase database = mongoClient
			.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {

		database.getCollection(Config.projectCollection).deleteMany(
				new Document("project_status", "ready"));
	}

}
