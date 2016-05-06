package sociam.pybossa.mongodb;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import sociam.pybossa.config.Config;

public class SetNullPrioToZero {

	public static void main(String[] args) {
		set();
	}

	public static void set() {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(Config.taskCollection)
					.find(new Document("priority", null));
			int counter = 0;
			if (iterable.first() != null) {
				
				for (Document document : iterable) {
					UpdateResult result = database.getCollection(Config.taskCollection).updateMany(new Document(),
							new Document().append("$set", new Document("priority", 0)));
					System.out.println(document.getInteger("priority"));
					counter++;
				}
			}
			System.out.println(counter);
			mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mongoClient.close();
	}
}
