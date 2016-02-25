package sociam.pybossa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import sociam.pybossa.config.Config;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class TaskCollector {

	final static Logger logger = Logger.getLogger(TaskCollector.class);
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	final static SimpleDateFormat PyBossaformatter = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss.SSSSSS");

	// caching tasksIDs
	static HashMap<Integer, Integer> cachedTaskIDsAndProjectsIDs = new HashMap<>();

	static Twitter twitter;

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");
		twitter = TwitterAccount.setTwitterAccount(2);
		logger.info("TaskCollector will be repeated every " + Config.TaskCollectorTrigger + " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.TaskCollectorTrigger + " ms");
				Thread.sleep(Integer.valueOf(Config.TaskCollectorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}

	}

	public static void run() {
		try {

			logger.debug("Getting time line from Twitter");
			ArrayList<JSONObject> ResponsesFromTwitter = getTimeLineAsJsons(twitter);
			if (ResponsesFromTwitter != null) {
				logger.debug("There are " + ResponsesFromTwitter.size() + " tweets to be processed");
				for (JSONObject jsonObject : ResponsesFromTwitter) {

					logger.debug("Processing a new twitter object ");
					if (!jsonObject.isNull("in_reply_to_status_id_str")) {
						logger.debug("Found a reply tweet " + jsonObject);
						String in_reply_to_status_id_str = jsonObject.getString("in_reply_to_status_id_str");
						String reply = jsonObject.getString("text");
						String in_reply_to_screen_name = jsonObject.getString("in_reply_to_screen_name");
						String taskResponse = reply.replaceAll("@" + in_reply_to_screen_name, "");

						// store the reply id
						String id_str = jsonObject.getString("id_str");

						// store the use screen name
						JSONObject userJson = jsonObject.getJSONObject("user");

						// store the replier user name
						String screen_name = userJson.getString("screen_name");

						logger.debug("Checking if the reply has already being stored");
						Document taskRun = getTaskRunsFromMongoDB(id_str);
						if (taskRun == null) {

							logger.debug("Looking for the original tweet for the reply");
							JSONObject orgTweet = getTweetByID(String.valueOf(in_reply_to_status_id_str), twitter);
							// loop through tweets till you find the orginal
							// tweet
							while (!orgTweet.isNull("in_reply_to_status_id_str")) {
								orgTweet = getTweetByID(orgTweet.getString("in_reply_to_status_id_str"), twitter);
							}
							logger.debug("Original tweet was found");

							String orgTweetText = orgTweet.getString("text");
							Pattern pattern = Pattern.compile("(#t[0-9]+)");
							Matcher matcher = pattern.matcher(orgTweetText);
							String taskID = "";
							if (matcher.find()) {
								logger.debug("Found a taskID in the orginal tweet");
								taskID = matcher.group(1).replaceAll("#t", "");
								Integer intTaskID = Integer.valueOf(taskID);
								// cache taskIDs
								if (!cachedTaskIDsAndProjectsIDs.containsKey(intTaskID)) {
									logger.debug("TaskID is not in the cache");
									logger.debug("Retriving Task id from Collection: " + Config.taskCollection);
									Document doc = getTaskFromMongoDB(intTaskID);
									if (doc != null) {
										int project_id = doc.getInteger("project_id");
										cachedTaskIDsAndProjectsIDs.put(intTaskID, project_id);
										if (insertTaskRun(taskResponse, intTaskID, project_id, id_str, screen_name)) {
											logger.debug("Task run was completely processed");
										} else {
											logger.error("Failed to process the task run");
										}
									} else {
										logger.error("Couldn't find task with ID " + taskID);
										// TODO: Remove tweets that do not have
										// records in MongoDB

									}
								} else {
									logger.debug("Task ID was found in the cache");
									insertTaskRun(taskResponse, intTaskID, cachedTaskIDsAndProjectsIDs.get(intTaskID),
											id_str, screen_name);
								}

							} else {
								logger.error("reply: \\" + reply
										+ " was not being identified with an associated task in the original text: \\"
										+ orgTweetText);
							}
						} else {
							logger.debug("Reply has already being stored");
						}
					} else {
						logger.debug("This is not a reply tweet");

					}

				}
			} else {
				logger.info("Time line was null");
			}

		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

	public static Boolean insertTaskRun(String text, int task_id, int project_id, String id_str, String screen_name) {

		JSONObject jsonData = BuildJsonTaskRunContent(text, task_id, project_id);
		if (insertTaskRunIntoMongoDB(jsonData, id_str, screen_name)) {
			logger.debug("Task run was successfully inserted into MongoDB");
			// Project has to be reqested before inserting a task run
			logger.debug("Requesting the project ID from PyBossa before inserting it");
			if (getReqest(project_id)) {
				String url = Config.PyBossahost + Config.taskRunDir;
				logger.debug("Inserting task run into PyBossa");
				JSONObject PyBossaResponse = insertTaskRunIntoPyBossa(url, jsonData);
				if (PyBossaResponse != null) {
					logger.debug("Task run was successfully inserted into PyBossa");
					return true;
				} else {
					logger.error("Task run was not inserted into PyBossa!");
					return false;
				}
			} else {
				return false;
			}

		} else {
			logger.error("Task run was not inserted into MongoDB!");
			return false;
		}

	}

	public static Boolean insertTaskRunIntoMongoDB(JSONObject jsonData, String id_str, String screen_name) {

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
			if (pushTaskRunToMongoDB(insertedAt, project_id, task_id, task_run_text, id_str, screen_name)) {
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
	public static boolean pushTaskRunToMongoDB(String publishedAt, Integer project_id, Integer task_id,
			String task_text, String id_str, String screen_name) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
		try {
			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);
			if (publishedAt != null && project_id != null && task_text != null && id_str != null
					&& screen_name != null) {
				FindIterable<Document> iterable = database.getCollection(Config.taskRunCollection)
						.find(new Document("id_str", id_str));
				if (iterable.first() == null) {
					database.getCollection(Config.taskRunCollection).insertOne(
							new Document().append("publishedAt", publishedAt).append("project_id", project_id)
									.append("task_id", task_id).append("task_text", task_text).append("id_str", id_str)
									.append("screen_name", screen_name));
					logger.debug("One task run is inserted into MongoDB");
					mongoClient.close();
					return true;
				} else {
					logger.error("The task run is already stored in MongoDB!");
					mongoClient.close();
					return false;
				}
			} else {
				mongoClient.close();
				return false;
			}

		} catch (Exception e) {
			logger.error("Error with inserting the task run " + " publishedAt " + publishedAt + " project_id "
					+ project_id + " isPushed " + task_id + " task_id " + task_text + "\n", e);
			mongoClient.close();
			return false;
		}

	}

	public static JSONObject insertTaskRunIntoPyBossa(String url, JSONObject jsonData) {
		JSONObject jsonResult = null;
		logger.debug("Json to be inserted into PyBossa:  " + jsonData);
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString(), "utf-8");
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 204) {
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				String output;
				logger.debug("Output from Server ...." + response.getStatusLine().getStatusCode() + "\n");
				while ((output = br.readLine()) != null) {
					logger.debug(output);
					jsonResult = new JSONObject(output);
				}
				return jsonResult;
			} else {
				logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				logger.error(response.toString());
				return null;
			}
		} catch (Exception ex) {
			logger.error(ex);
			return null;
		}

	}

	public static Boolean getReqest(int project_id) {
		String url = Config.PyBossahost + Config.projectDir + "/" + project_id + "/newtask";

		HttpURLConnection con = null;
		try {

			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
			con.setConnectTimeout(10000);
			int responseCode = con.getResponseCode();
			// System.out.println("\nSending 'GET' request to URL : " + url);
			// System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			logger.debug("PyBossa get reqesut for Project_id: " + response);
			in.close();

			if (responseCode == 200 || responseCode == 204) {
				return true;
			} else {
				return false;
			}

		} catch (IOException e) {
			logger.error("Error ", e);
			return false;

		}

	}

	/**
	 * It returns a json string for task creation from a given task details
	 * 
	 * @param text
	 *            the task content
	 * @param n_answers
	 * @param quorum
	 * @param calibration
	 * @param project_id
	 *            The project ID
	 * @param priority_0
	 * @return Json string
	 */
	public static JSONObject BuildJsonTaskRunContent(String answer, int task_id, int project_id) {

		JSONObject app2 = new JSONObject();
		app2.put("project_id", project_id);

		app2.put("info", answer);
		app2.put("task_id", task_id);
		app2.put("user_ip", "80.44.145.144");
		return app2;
	}

	public static Document getTaskFromMongoDB(int pybossa_task_id) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
		try {
			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);

			// MongoCollection<Document> collection = database
			// .getCollection(Config.taskCollection);
			// Document myDoc = collection.find(
			// eq("pybossa_task_id", pybossa_task_id)).limit(1);

			FindIterable<Document> iterable = database.getCollection(Config.taskCollection)
					.find(new Document("pybossa_task_id", pybossa_task_id));

			Document document = iterable.first();
			mongoClient.close();
			return document;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static Document getTaskRunsFromMongoDB(String id_str) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
		try {
			MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);

			// MongoCollection<Document> collection = database
			// .getCollection(Config.taskCollection);
			// Document myDoc = collection.find(
			// eq("pybossa_task_id", pybossa_task_id)).limit(1);

			FindIterable<Document> iterable = database.getCollection(Config.taskRunCollection)
					.find(new Document("id_str", id_str));

			Document document = iterable.first();
			mongoClient.close();
			return document;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return null;
		}

	}

	public static JSONObject getTweetByID(String status_id_str, Twitter twitter) {

		try {
			Status status = twitter.showStatus(Long.parseLong(status_id_str));
			if (status == null) { //
				return null;
			} else {
				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				JSONObject json = new JSONObject(rawJSON);
				return json;
			}
		} catch (TwitterException e) {
			logger.error("Error ", e);
			return null;
		}
	}

	public static ArrayList<JSONObject> getTimeLineAsJsons(Twitter twitter) {

		ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
		try {
			Paging p = new Paging();
			p.setCount(200);
			List<Status> statuses = twitter.getHomeTimeline(p);
			for (Status status : statuses) {

				String rawJSON = TwitterObjectFactory.getRawJSON(status);
				JSONObject jsonObject = new JSONObject(rawJSON);
				jsons.add(jsonObject);
			}
		} catch (TwitterException te) {
			logger.error("Failed to get timeline: ", te);
			return null;
		}

		return jsons;

	}

}
