package sociam.pybossa.mongodb;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import sociam.pybossa.config.Config;

public class GetLastestByDate {

	public static void main(String[] args) {

		JSONObject obj = getLatestTask();
		System.out.println(obj);
	}

	public static JSONObject getLatestTask() {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);

			Boolean foundTask = false;
			int pybossa_task_id;
			int offset = 0;
			JSONObject task = new JSONObject();
			while (!foundTask) {
				FindIterable<Document> iterable = database.getCollection(Config.taskCollection).find()
						.sort(new Document("publishedAt", -1)).limit(1).skip(offset);
				if (iterable.first() != null) {
					Document doc = iterable.first();
					pybossa_task_id = doc.getInteger("pybossa_task_id");
					Boolean hadAnswer = wasAnsweredBefore(pybossa_task_id);
					if (!hadAnswer) {
						task.put("task_id", doc.getInteger("pybossa_task_id"));
						task.put("project_id", doc.getInteger("project_id"));
						task.put("task_text", doc.getString("task_text"));
						task.put("publishedAt", doc.getString("publishedAt"));
						task.put("task_type", doc.getString("task_type"));
						break;
					} else {
						offset++;
					}
				} else {
					mongoClient.close();
					return null;
				}
			}

			if (task.getString("task_type").equals("validate")) {
				task.put("question", Config.project_validation_question + "?");
			}

			mongoClient.close();
			return task;
		} catch (Exception e) {
			mongoClient.close();
			return null;
		}
	}

	public static Boolean wasAnsweredBefore(int pybossa_task_id) {
		MongoClient mongoClient = null;
		try {
			Boolean exist = false;
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(Config.taskRunCollection)
					.find(new Document("task_id", pybossa_task_id)).limit(1);

			if (iterable.first() != null) {
				exist = true;
			} else {
				exist = false;
			}
			mongoClient.close();
			return exist;
		} catch (Exception e) {
			mongoClient.close();
			return null;
		}
	}

}
