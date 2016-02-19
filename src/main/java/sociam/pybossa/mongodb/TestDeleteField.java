package sociam.pybossa.mongodb;

import org.bson.Document;

import sociam.pybossa.Config;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class TestDeleteField {
	static MongoClient mongoClient = new MongoClient(Config.mongoHost,
			Config.mongoPort);
	static MongoDatabase database = mongoClient
			.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {

		
		UpdateResult result = database.getCollection(Config.projectCollection).updateMany(new Document(),
				new Document().append("$unset", new Document("project_started", "ready")));
		System.out.println(result.toString());

		
		
		

	}
}
