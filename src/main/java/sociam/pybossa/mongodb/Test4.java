package sociam.pybossa.mongodb;

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.bson.Document;

import sociam.pybossa.Config;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class Test4 {
	static MongoClient mongoClient = new MongoClient(Config.mongoHost,
			Config.mongoPort);
	static MongoDatabase database = mongoClient
			.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {
		
		HashSet<Document> res = checkMongoForEmptyProjects();
		System.out.println(res.size());
		
	}
	static HashSet<Document> jsons = new LinkedHashSet<Document>();

	private static HashSet<Document> checkMongoForEmptyProjects() {
		try {
			jsons = new LinkedHashSet<Document>();
			FindIterable<Document> iterable = database.getCollection(
					Config.projectCollection).find(
					new Document("project_status", "empty"));

			if (iterable.first() != null) {
				iterable.forEach(new Block<Document>() {
					@Override
					public void apply(final Document document) {
						jsons.add(document);
					}
				});
				return jsons;
			}
			return jsons;
		} catch (Exception e) {
			return null;
		}
	}
}
