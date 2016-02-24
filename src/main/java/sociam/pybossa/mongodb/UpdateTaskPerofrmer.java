package sociam.pybossa.mongodb;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import sociam.pybossa.config.Config;

public class UpdateTaskPerofrmer {
	static MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
	static MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {

		UpdateResult result = database.getCollection(Config.taskCollection).updateMany(new Document(),
				new Document().append("$set", new Document("task_status", "ready")));
		System.out.println(result.toString());
		

	}


}
