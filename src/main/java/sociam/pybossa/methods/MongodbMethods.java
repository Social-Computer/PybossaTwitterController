package sociam.pybossa.methods;

import static com.mongodb.client.model.Filters.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.config.Config;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class MongodbMethods {
	final static Logger logger = Logger.getLogger(MongodbMethods.class);

	public final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static Boolean updateProjectIntoMongoDB(ObjectId _id,
			String project_status, String project_type) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);

		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);

			UpdateResult result = database.getCollection(
					Config.projectCollection)
					.updateOne(
							new Document("_id", _id),
							new Document("$set", new Document("project_status",
									project_status).append("project_type",
									"validate")));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			mongoClient.close();
			logger.error("Error ", e);
			return false;
		}

	}

	public static Boolean updateProjectIntoMongoDB(ObjectId _id,
			int project_id, String project_status, String project_type) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);

		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);

			UpdateResult result = database.getCollection(
					Config.projectCollection).updateOne(
					new Document("_id", _id),
					new Document("$set", new Document("project_status",
							project_status).append("project_id", project_id)
							.append("project_type", "validate")));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			mongoClient.close();
			logger.error("Error ", e);
			return false;
		}
	}

	public static HashSet<Document> getAllProjects() {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			HashSet<Document> jsons = new LinkedHashSet<Document>();
			FindIterable<Document> iterable = database
					.getCollection(Config.projectCollection)
					.find(new Document())
					.limit(Integer.valueOf(Config.ProjectLimit));

			if (iterable.first() != null) {
				for (Document document : iterable) {
					jsons.add(document);
				}
			}
			mongoClient.close();
			return jsons;
		} catch (Exception e) {
			mongoClient.close();
			logger.error("Error ", e);
			return null;
		}
	}

	public static Boolean updateProjectToInsertedInMongoDB(int project_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			UpdateResult result = database.getCollection(
					Config.projectCollection).updateOne(
					new Document("project_id", project_id),
					new Document().append("$set", new Document(
							"project_status", "inserted").append("task_type",
							"validate")));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.projectCollection
							+ " Collection was updated with project_status: inserted");
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	public static Boolean updateProjectToInsertedInMongoDB(ObjectId project_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			UpdateResult result = database.getCollection(
					Config.projectCollection).updateOne(
					new Document("project_id", project_id),
					new Document().append("$set", new Document(
							"project_status", "inserted").append("task_type",
							"validate")));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.projectCollection
							+ " Collection was updated with project_status: inserted");
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	public static Boolean updatePriorityInTask(int task_id, int priority) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			UpdateResult result = database.getCollection(Config.taskCollection)
					.updateOne(
							new Document("task_id", task_id),
							new Document().append("$set", new Document(
									"priority", priority)));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.projectCollection
							+ " Collection was updated with priority: "
							+ priority);
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	// For encoding issue that makes the text changed after inserting it into
	// PyBossa
	public static Boolean updateBinString(ObjectId _id, String text_encoded,
			String binItem) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase binsDatabase = mongoClient
					.getDatabase(Config.binsDatabaseName);
			UpdateResult result = binsDatabase.getCollection(binItem)
					.updateOne(
							new Document("_id", _id),
							new Document().append("$set", new Document(
									"text_encoded", text_encoded)));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	public static HashSet<Document> getTweetsFromBinInMongoDB(
			String collectionName) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		HashSet<Document> tweetsjsons = new LinkedHashSet<Document>();
		try {
			MongoDatabase binsDatabase = mongoClient
					.getDatabase(Config.binsDatabaseName);
			FindIterable<Document> iterable = binsDatabase
					.getCollection(collectionName).find()
					.limit(Integer.valueOf(Config.TasksPerProject));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					// JSONObject app2 = new JSONObject(document);
					tweetsjsons.add(document);
				}
			}
			mongoClient.close();
			return tweetsjsons;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return tweetsjsons;
		}
	}

	public static HashSet<JSONObject> getnotCompletedProjects() {
		logger.debug("getting projects from collection "
				+ Config.projectCollection);
		HashSet<JSONObject> startedProjectsJsons = new HashSet<JSONObject>();
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.projectCollection).find(
					ne("project_status", "completed"));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					JSONObject app2 = new JSONObject(document);
					startedProjectsJsons.add(app2);
				}

				// iterable.forEach(new Block<Document>() {
				// @Override
				// public void apply(final Document document) {
				// JSONObject app2 = new JSONObject(document);
				// startedProjectsJsons.add(app2);
				// }
				// });
				// return startedProjectsJsons;
			}
			mongoClient.close();
			return startedProjectsJsons;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static Boolean insertTaskIntoMongoDB(JSONObject response,
			String task_status, String task_type) {

		try {
			Integer pybossa_task_id = response.getInt("id");
			// String created_String = response.getString("created");
			// Date publishedAt = PyBossaformatter.parse(created_String);
			Date date = new Date();
			String publishedAt = MongoDBformatter.format(date);
			// String targettedFormat = MongoDBformatter.format(publishedAt);
			Integer project_id = response.getInt("project_id");
			JSONObject info = response.getJSONObject("info");
			String task_text = info.getString("text");
			String media_url = info.getString("media_url");
			logger.debug("Inserting a task into MongoDB");
			if (pushTaskToMongoDB(pybossa_task_id, publishedAt, project_id,
					task_status, task_text, media_url, task_type)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}

	}

	public static Boolean insertTaskIntoMongoDB(Integer project_id,
			String bin_id_String, String task_text, String media_url,
			String task_status, String task_type) {

		try {
			// String created_String = response.getString("created");
			// Date publishedAt = PyBossaformatter.parse(created_String);
			Date date = new Date();
			String publishedAt = MongoDBformatter.format(date);
			// String targettedFormat = MongoDBformatter.format(publishedAt);
			logger.debug("Inserting a task into MongoDB");
			if (pushTaskToMongoDB(publishedAt, project_id, bin_id_String,
					task_status, task_text, media_url, task_type)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}

	}

	public static boolean pushTaskToMongoDB(String publishedAt,
			Integer project_id, String bin_id_String, String task_status,
			String task_text, String media_url, String task_type) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {

			if (publishedAt != null && project_id != null
					&& task_status != null && task_text != null
					&& media_url != null && task_type != null
					&& bin_id_String != null) {
				MongoDatabase database = mongoClient
						.getDatabase(Config.projectsDatabaseName);
				FindIterable<Document> iterable = database.getCollection(
						Config.taskCollection).find(
						new Document("project_id", project_id).append(
								"task_text", task_text));
				if (iterable.first() == null) {
					database.getCollection(Config.taskCollection)
							.insertOne(
									new Document()
											.append("publishedAt", publishedAt)
											.append("project_id", project_id)
											.append("bin_id_String",
													bin_id_String)
											.append("task_status", task_status)
											.append("twitter_task_status",
													task_status)
											.append("facebook_task_status",
													task_status)
											.append("task_status", task_status)
											.append("task_text", task_text)
											.append("media_url", media_url)
											.append("task_type", task_type)
											.append("priority", 0));
					logger.debug("One task is inserted into MongoDB");

				} else {
					logger.error("task is already in the collection!!");
				}

			}
			mongoClient.close();
			return true;
		} catch (Exception e) {
			logger.error("Error with inserting the task " + " "
					+ " publishedAt " + publishedAt + " project_id "
					+ project_id + " task_status " + task_status
					+ " task_text " + task_text + "\n" + e);
			mongoClient.close();
			return false;
		}

	}

	public static boolean pushTaskToMongoDB(Integer pybossa_task_id,
			String publishedAt, Integer project_id, String task_status,
			String task_text, String media_url, String task_type) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {

			if (publishedAt != null && project_id != null
					&& task_status != null && task_text != null
					&& media_url != null && task_type != null) {
				MongoDatabase database = mongoClient
						.getDatabase(Config.projectsDatabaseName);
				FindIterable<Document> iterable = database.getCollection(
						Config.taskCollection).find(
						new Document("project_id", project_id).append(
								"task_text", task_text));
				if (iterable.first() == null) {
					database.getCollection(Config.taskCollection)
							.insertOne(
									new Document()
											.append("pybossa_task_id",
													pybossa_task_id)
											.append("publishedAt", publishedAt)
											.append("project_id", project_id)
											.append("task_status", task_status)
											.append("twitter_task_status",
													task_status)
											.append("facebook_task_status",
													task_status)
											.append("task_status", task_status)
											.append("task_text", task_text)
											.append("media_url", media_url)
											.append("task_type", task_type));
					logger.debug("One task is inserted into MongoDB");

				} else {
					logger.error("task is already in the collection!!");
				}

			}
			mongoClient.close();
			return true;
		} catch (Exception e) {
			logger.error("Error with inserting the task " + " pybossa_task_id "
					+ pybossa_task_id + " publishedAt " + publishedAt
					+ " project_id " + project_id + " task_status "
					+ task_status + " task_text " + task_text + "\n" + e);
			mongoClient.close();
			return false;
		}

	}

	public static JSONObject getProjectByID(int project_id) {
		logger.debug("getting project by project_id from "
				+ Config.projectCollection + " collection");
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			JSONObject json = null;
			FindIterable<Document> iterable = database.getCollection(
					Config.projectCollection).find(
					new Document("project_id", project_id));
			if (iterable.first() != null) {
				Document document = iterable.first();
				json = new JSONObject(document);
			}
			mongoClient.close();
			return json;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static JSONObject getTasks(Integer offset) {
		JSONObject tasks = new JSONObject();
		JSONArray tasksArray = new JSONArray();
		logger.debug("Getting not completed tasks from "
				+ Config.taskCollection + " collection");
		MongoClient mongoClient = null;
		JSONObject json = null;
		try {
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database
					.getCollection(Config.taskCollection)
					.find(ne("task_status", "completed")).limit(200)
					.skip(offset);
			if (iterable.first() != null) {
				for (Document document : iterable) {
					json = new JSONObject(document);
					tasksArray.put(json);
				}
				tasks.put("tasks", tasksArray);
			} else {
				tasks.put("tasks", "");
			}
			mongoClient.close();
			return tasks;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static JSONObject getStatsFroRest(String collection,
			String field_name, Integer field_value, Integer offset,
			Integer limit) {

		JSONObject tasks = new JSONObject();
		JSONArray tasksArray = new JSONArray();
		logger.debug("Getting not completed tasks from "
				+ Config.taskCollection + " collection");
		MongoClient mongoClient = null;
		JSONObject json = null;
		try {
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = null;
			if (field_name == null) {
				iterable = database.getCollection(collection).find()
						.limit(limit).skip(offset);
			} else {
				iterable = database.getCollection(collection)
						.find(new Document(field_name, field_value))
						.limit(limit).skip(offset);
			}
			if (iterable.first() != null) {
				Integer counter = 0;
				for (Document document : iterable) {
					json = new JSONObject(document);
					tasksArray.put(json);
					counter++;
				}
				if (counter >= limit) {
					tasks.put("offset", limit);
				} else {
					tasks.put("offset", 0);
				}
				tasks.put(collection, tasksArray);
			} else {
				tasks.put("message", "");
			}
			mongoClient.close();
			return tasks;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static JSONObject getLatestUncompletedAnsweredTask() {
		logger.debug("Retriving the last uncompleted task");
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database
					.getCollection(Config.taskCollection)
					.find(ne("task_status", "completed"))
					.sort(new Document("publishedAt", -1)).limit(1);
			if (iterable.first() != null) {
				Document doc = iterable.first();
				JSONObject task = new JSONObject(doc);
				mongoClient.close();

				ArrayList<String> hashtags = getProjectHashTags(task
						.getInt("project_id"));
				if (hashtags != null) {
					Collections.sort(hashtags);
					task.put("hashtags", hashtags);
				}

				return task;
			} else {
				mongoClient.close();
				return null;
			}
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static ArrayList<String> getProjectHashTags(int project_id) {
		JSONObject project = getProjectByID(project_id);
		ArrayList<String> hashtags = new ArrayList<>();
		if (project != null) {
			JSONArray bin_id = project.getJSONArray("identifiers");
			for (int i = 0; i < bin_id.length(); i++) {
				String hashtag = bin_id.getString(i);
				hashtags.add("#" + hashtag);
			}
		} else {
			return null;
		}
		return hashtags;
	}

	public static Boolean updateTaskToPushedInMongoDB(ObjectId _id,
			String twitter_task_status) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {

			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			Date date = new Date();
			String lastPushAt = MongoDBformatter.format(date);
			UpdateResult result = database.getCollection(Config.taskCollection)
					.updateOne(
							new Document("_id", _id),
							new Document().append("$set", new Document(
									"twitter_task_status", twitter_task_status)
									.append("twitter_lastPushAt", lastPushAt)));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.taskCollection
							+ " Collection was updated where _id= "
							+ _id.toString() + " to twitter_task_status="
							+ twitter_task_status);
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	public static JSONObject getLatestUnAnsweredTask() {
		logger.debug("Retriving the last unanswered task");
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			Boolean foundTask = false;
			int pybossa_task_id;
			int offset = 0;
			JSONObject task = new JSONObject();
			while (!foundTask) {
				FindIterable<Document> iterable = database
						.getCollection(Config.taskCollection).find()
						.sort(new Document("publishedAt", -1)).limit(1)
						.skip(offset);
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
					logger.debug("There are no tasks without answeres");
					mongoClient.close();
					return null;
				}
			}
			logger.debug("Latest task is found");

			if (task.getString("task_type").equals("validate")) {
				task.put("question", Config.project_validation_question + "?");
			}

			ArrayList<String> hashtags = getProjectHashTags(task
					.getInt("project_id"));
			if (hashtags != null) {
				Collections.sort(hashtags);
				task.put("hashtags", hashtags);
			}

			mongoClient.close();
			return task;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static Boolean wasAnsweredBefore(int pybossa_task_id) {
		MongoClient mongoClient = null;
		try {
			Boolean exist = false;
			mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database
					.getCollection(Config.taskRunCollection)
					.find(new Document("task_id", pybossa_task_id)).limit(1);
			if (iterable.first() != null) {
				exist = true;
			} else {
				exist = false;
			}
			mongoClient.close();
			return exist;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static ArrayList<Document> getIncompletedTasksFromMongoDB(
			String source) {
		ArrayList<Document> NotPushedTasksjsons = new ArrayList<Document>();
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.taskCollection).find(ne(source, "completed"));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					NotPushedTasksjsons.add(document);
				}
			}
			mongoClient.close();
			return NotPushedTasksjsons;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static Boolean insertTaskRunIntoMongoDB(JSONObject jsonData,
			String contributor_name, String source) {

		try {
			// Integer pybossa_task_run_id = PyBossaResponse.getInt("id");
			// String created_String = response.getString("created");
			// Date publishedAt = PyBossaformatter.parse(created_String);
			Date date = new Date();
			String insertedAt = MongoDBformatter.format(date);
			Integer project_id = jsonData.getInt("project_id");
			Integer task_id = jsonData.getInt("task_id");
			String task_run_text = jsonData.getString("info");
			logger.debug("Inserting a task run into MongoDB");
			if (pushTaskRunToMongoDB(insertedAt, project_id, task_id,
					task_run_text, contributor_name, source)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}

	}

	public static Boolean insertTaskRun(String text, int task_id,
			int project_id, String contributor_name, String source) {

		if (!(source.equals("TaskView") && text.contains("PRIO"))) {
			Document taskRun = MongodbMethods.getTaskRunsFromMongoDB(task_id,
					contributor_name, text);
			if (taskRun != null) {
				logger.error("You are only allowed one contribution for each task.");
				logger.error("task_id= " + task_id + " screen_name: "
						+ contributor_name);
				return false;
			}
		}

		if (MongodbMethods.insertTaskRunIntoMongoDB(project_id, task_id, text,
				contributor_name, source)) {
			logger.debug("Task run was successfully inserted into MongoDB");
			// Project has to be reqested before inserting a task run
			return true;
		} else {
			logger.error("Task run was not inserted into MongoDB!");
			return false;
		}
	}

	public static Boolean insertTaskRunIntoMongoDB(Integer project_id,
			Integer task_id, String task_run_text, String contributor_name,
			String source) {

		try {
			// Integer pybossa_task_run_id = PyBossaResponse.getInt("id");
			// String created_String = response.getString("created");
			// Date publishedAt = PyBossaformatter.parse(created_String);
			Date date = new Date();
			String insertedAt = MongoDBformatter.format(date);
			logger.debug("Inserting a task run into MongoDB");
			if (pushTaskRunToMongoDB(insertedAt, project_id, task_id,
					task_run_text, contributor_name, source)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}

	}

	// maybe it's not needed to check id_str becasue we check it first!
	// so only do an insert?
	public static boolean pushTaskRunToMongoDB(String publishedAt,
			Integer project_id, Integer task_id, String task_text,
			String contributor_name, String source) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			if (publishedAt != null && project_id != null && task_text != null
					&& contributor_name != null && source != null) {

				database.getCollection(Config.taskRunCollection).insertOne(
						new Document().append("publishedAt", publishedAt)
								.append("project_id", project_id)
								.append("task_id", task_id)
								.append("task_run_text", task_text)
								.append("contributor_name", contributor_name)
								.append("source", source));
				logger.debug("One task run is inserted into MongoDB");
				mongoClient.close();
				return true;

			} else {
				mongoClient.close();
				return false;
			}

		} catch (Exception e) {
			logger.error("Error with inserting the task run " + " publishedAt "
					+ publishedAt + " project_id " + project_id + " isPushed "
					+ task_id + " task_id " + task_text + "\n", e);
			mongoClient.close();
			return false;
		}

	}

	public static Document getTaskRunsFromMongoDB(int task_id,
			String contributor_name, String text) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);

			// MongoCollection<Document> collection = database
			// .getCollection(Config.taskCollection);
			// Document myDoc = collection.find(
			// eq("pybossa_task_id", pybossa_task_id)).limit(1);

			FindIterable<Document> iterable = database.getCollection(
					Config.taskRunCollection).find(
					new Document("task_id", task_id).append("contributor_name",
							contributor_name).append("task_run_text", text));

			Document document = iterable.first();
			mongoClient.close();
			return document;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static ArrayList<Document> getTaskRunsFromMongoDB(int task_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		ArrayList<Document> docs = new ArrayList<Document>();
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);

			FindIterable<Document> iterable = database.getCollection(
					Config.taskRunCollection).find(
					new Document("task_id", task_id));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					docs.add(document);
				}
			}
			mongoClient.close();
			return docs;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static ArrayList<JSONObject> getTasksORRunsByProjectID(
			int project_id, String collection) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		ArrayList<JSONObject> docs = new ArrayList<JSONObject>();
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);

			FindIterable<Document> iterable = database
					.getCollection(collection).find(
							new Document("project_id", project_id));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					JSONObject json = new JSONObject(document);
					docs.add(json);
				}
			}
			mongoClient.close();
			return docs;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static Boolean updateTaskToPushedInMongoDB(ObjectId _id,
			String facebook_task_id, String facebook_task_status) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {

			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			Date date = new Date();
			String lastPushAt = MongoDBformatter.format(date);
			UpdateResult result = database.getCollection(Config.taskCollection)
					.updateOne(
							new Document("_id", _id),
							new Document().append(
									"$set",
									new Document("facebook_task_status",
											facebook_task_status).append(
											"facebook_lastPushAt", lastPushAt)
											.append("facebook_task_id",
													facebook_task_id)));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.taskCollection
							+ " Collection was updated where _id= "
							+ _id.toString() + " to facebook_task_status="
							+ facebook_task_status);
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	public static Document getTaskFromMongoDB(int task_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);

			// MongoCollection<Document> collection = database
			// .getCollection(Config.taskCollection);
			// Document myDoc = collection.find(
			// eq("pybossa_task_id", pybossa_task_id)).limit(1);

			FindIterable<Document> iterable = database.getCollection(
					Config.taskCollection).find(
					new Document("task_id", task_id));

			Document document = iterable.first();
			mongoClient.close();
			return document;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static Boolean updateProjectsByAddingCounters() {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.projectCollection).find(
					new Document("project_id", new Document("$exists", false)));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					ObjectId _id = document.getObjectId("_id");
					int project_id = _id.getCounter();
					database.getCollection(Config.projectCollection).updateOne(
							new Document("_id", _id),
							new Document().append("$set", new Document(
									"project_id", project_id)));
				}
			}

			mongoClient.close();
			return true;

		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static Boolean updateTasksByAddingCounters() {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.taskCollection).find(
					new Document("task_id", new Document("$exists", false)));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					ObjectId _id = document.getObjectId("_id");
					int task_id = _id.getCounter();
					database.getCollection(Config.taskCollection).updateOne(
							new Document("_id", _id),
							new Document().append("$set", new Document(
									"task_id", task_id)));
				}
			}
			mongoClient.close();
			return true;

		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static Boolean updateTaskRunsByAddingCounters() {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.taskRunCollection)
					.find(new Document("task_run_id", new Document("$exists",
							false)));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					ObjectId _id = document.getObjectId("_id");
					int task_run_id = _id.getCounter();
					database.getCollection(Config.taskRunCollection).updateOne(
							new Document("_id", _id),
							new Document().append("$set", new Document(
									"task_run_id", task_run_id)));
				}
			}
			mongoClient.close();
			return true;

		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static HashSet<Document> getUnPorcessedTaskRuns() {
		logger.debug("getting taskRuns from collection "
				+ Config.taskRunCollection);
		HashSet<Document> taskRunsDocuments = new HashSet<Document>();
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			FindIterable<Document> iterable = database.getCollection(
					Config.taskRunCollection).find(ne("wasProcessed", true));
			if (iterable.first() != null) {
				for (Document document : iterable) {
					taskRunsDocuments.add(document);
				}
			}
			mongoClient.close();
			return taskRunsDocuments;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}
	}

	public static Boolean updatetaskRunsToBeProcessed(ObjectId _id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			UpdateResult result = database.getCollection(
					Config.taskRunCollection).updateOne(
					new Document("_id", _id),
					new Document().append("$set", new Document("wasProcessed",
							true)));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.taskRunCollection
							+ " Collection was updated with wasProcessed: true");
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	public static Boolean updateTaskToBeCompleted(int task_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		try {
			MongoDatabase database = mongoClient
					.getDatabase(Config.projectsDatabaseName);
			UpdateResult result = database.getCollection(Config.taskCollection)
					.updateOne(
							new Document("task_id", task_id),
							new Document().append("$set", new Document(
									"facebook_task_status", "completed")
									.append("twitter_task_status", "completed")
									.append("task_status", "completed")));
			logger.debug(result.toString());
			if (result.wasAcknowledged()) {
				if (result.getMatchedCount() > 0) {
					logger.debug(Config.taskRunCollection
							+ " Collection was updated with wasProcessed: true");
					mongoClient.close();
					return true;
				}
			}
			mongoClient.close();
			return false;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

	// public static Document getTaskRunsFromMongoDB(String contribution_id) {
	// MongoClient mongoClient = new MongoClient(Config.mongoHost,
	// Config.mongoPort);
	// try {
	// MongoDatabase database =
	// mongoClient.getDatabase(Config.projectsDatabaseName);
	//
	// // MongoCollection<Document> collection = database
	// // .getCollection(Config.taskCollection);
	// // Document myDoc = collection.find(
	// // eq("pybossa_task_id", pybossa_task_id)).limit(1);
	//
	// FindIterable<Document> iterable =
	// database.getCollection(Config.taskRunCollection)
	// .find(new Document("contribution_id", contribution_id));
	//
	// Document document = iterable.first();
	// mongoClient.close();
	// return document;
	// } catch (Exception e) {
	// logger.error("Error ", e);
	// mongoClient.close();
	// return null;
	// }
	//
	// }
}
