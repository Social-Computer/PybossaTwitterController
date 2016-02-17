package sociam.pybossa;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class TaskPerformer {

	final static Logger logger = Logger.getLogger(TaskPerformer.class);

	static MongoClient mongoClient = new MongoClient(Config.mongoHost,
			Config.mongoPort);
	static MongoDatabase database = mongoClient
			.getDatabase(Config.projectsDatabaseName);

	public static void main(String[] args) {
		BasicConfigurator.configure();
		getTasksFromMongoDB();

	}

	public static HashSet<JSONObject> getTasksFromMongoDB() {
		HashSet<JSONObject> jsons = new LinkedHashSet<JSONObject>();
		FindIterable<Document> iterable = database.getCollection(
				Config.taskCollection).find(new Document("isPushed", false));
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
