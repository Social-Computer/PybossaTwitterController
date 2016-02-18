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

public class TestUpdateAndInsert {
	static MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
	static MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {
		
		FindIterable<Document> iterable = database.getCollection(Config.projectCollection)
				.find(new Document("project_name", "worldnews"));

		if (iterable.first() != null) {
			iterable.forEach(new Block<Document>() {
				@Override
				public void apply(final Document document) {
					JSONObject app2 = new JSONObject(document);
					ObjectId id =  document.getObjectId("_id");
					System.out.println(id);
					UpdateResult result = database.getCollection(Config.projectCollection).updateOne(
							new Document("_id", id),
							new Document().append("$set",new Document("field2", "new")));
					if (result.wasAcknowledged()) {
						if (result.getMatchedCount() > 0) {
							System.out.println("fould a match " + result.getMatchedCount());
						}
					}
					System.out.println(result.toString());
				}
			});
			
		}
		
		
		
		
	}

}
