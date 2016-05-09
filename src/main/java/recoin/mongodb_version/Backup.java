package recoin.mongodb_version;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import facebook4j.Facebook;
import facebook4j.Post;
import facebook4j.json.DataObjectFactory;
import sociam.pybossa.config.Config;
import sociam.pybossa.methods.FacebookMethods;
import sociam.pybossa.util.FacebookAccount;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class Backup {
	final static Logger logger = Logger.getLogger(Backup.class);
	static DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");

	public static void main(String[] args) {

		processBackup();
	}

	public static void processBackup() {

		String root = "/data/SocialComputerBuackups/";
		Date date = new Date();
		String dateString = dateFormat.format(date);
		String platformData = "platforms";
		String facebook = "facebook";
		String twitter = "twitter";
		String facbookPath = root + dateString + "/" + platformData + "/" + facebook;
		String twitterPath = root + dateString + "/" + platformData + "/" + twitter;

		String mongodbPath = root + dateString + "/" + "MongodbBackups";
		logger.debug("Starting the cleaning process");

		logger.debug("==============================");
		logger.debug("Removing posts from Facebook!");
		JSONObject facebookJson = removeFacebookPosts();
		if (facebookJson != null) {
			logger.debug("posts were successfuly deelted!");
			logger.debug("Storing data from facebook to  " + facbookPath);
			Boolean result = writeJsonToFile(facbookPath, facebookJson);
			if (result) {
				logger.debug("Successful storing of facebook JSON to file");
			} else {
				logger.error("Error in storing facebook JSON to file");
			}
		}

		logger.debug("==============================");
		logger.debug("Removing tweets from Twitter!");
		JSONObject twitterJson = removeTweets();
		if (twitterJson != null) {
			logger.debug("tweets were successfuly deleted!");
			logger.debug("Storing data from twitter to  " + twitterPath);
			Boolean result = writeJsonToFile(twitterPath, twitterJson);
			if (result) {
				logger.debug("Successful storing of twitter JSON to file");
			} else {
				logger.error("Error in storing twitter JSON to file");
			}
		}

		logger.debug("==============================");
		logger.debug("Backing up Mongodb databases!");
		Boolean mongodbResult = mongodbDump(mongodbPath);
		if (mongodbResult) {
			logger.debug("successful packup of mongodb databases");
		} else {
			logger.debug("Error in backing up Mongodb databases");
		}

		// loop thorugh the two databases and delete their collections
		ArrayList<String> databases = new ArrayList<String>();
		databases.add(Config.projectsDatabaseName);
		databases.add(Config.binsDatabaseName);
		for (String string : databases) {
			logger.debug("==============================");
			logger.debug("deleting collections from dataabse " + string);
			Boolean dropCollections = deleteCollections(string);
			if (dropCollections) {
				logger.debug("Successful deleting of " + string);
			} else {
				logger.debug("Error in deleting of " + string);
			}
		}

		logger.debug("==============================");
		logger.debug("Cleaning is done ;)");

	}

	public static Boolean writeJsonToFile(String path, JSONObject json) {
		try {
			FileWriter file = new FileWriter(path);
			file.write(json.toString());
			file.close();
			return true;
		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}
	}

	public static JSONObject removeTweets() {
		Twitter twitter = TwitterAccount.setTwitterAccount(2);
		JSONObject json = new JSONObject();
		try {
			Paging p = new Paging();
			p.setCount(200);
			List<Status> statuses = twitter.getUserTimeline(p);
			while ((statuses = twitter.getUserTimeline(p)) != null) {
				if (statuses.size() == 0) {
					return json;
				}
				logger.debug("Number of status " + statuses.size());
				for (Status status : statuses) {
					try {
						long id = status.getId();
						String rawJSON = TwitterObjectFactory.getRawJSON(status);
						json.put(String.valueOf(id), rawJSON);
						logger.debug("Id " + id + " " + rawJSON);
						twitter.destroyStatus(id);
						logger.debug("Id " + id + " is deleted");
						Thread.sleep(1000);
					} catch (Exception e) {
						logger.error("Error", e);
					}
				}
				logger.debug("Waiting 1 minute before getting 200 responses");
				statuses = twitter.getUserTimeline(p);
				Thread.sleep(60000);
			}
			return json;
		} catch (TwitterException e) {
			e.printStackTrace();
			if (e.exceededRateLimitation()) {
				try {
					logger.debug("Twitter rate limit is exceeded!");
					int waitfor = e.getRateLimitStatus().getSecondsUntilReset();
					logger.debug("Waiting for " + (waitfor + 100) + " seconds");
					Thread.sleep((waitfor * 1000) + 100000);
					removeTweets();
				} catch (InterruptedException e2) {
					logger.error("Error", e2);
				}
			}
			return null;
		} catch (InterruptedException e) {
			logger.error("Error", e);

			return null;
		}
	}

	public static JSONObject removeFacebookPosts() {
		JSONObject json = new JSONObject();

		try {
			Facebook facebook = FacebookAccount.setFacebookAccount(1);
			ArrayList<Post> posts;
			while ((posts = FacebookMethods.getLatestPostsEvenWithoutComments(facebook)) != null) {
				logger.debug("post size " + posts.size());
				for (Post post : posts) {
					try {
						String rawJSON = DataObjectFactory.getRawJSON(post);
						json.put(String.valueOf(post.getId()), rawJSON);
						logger.debug("ID " + post.getId() + " " + rawJSON);
						logger.debug("Post with id " + facebook.deletePost(post.getId()) + " is deleted");
					} catch (Exception e) {
						logger.error("Error", e);
					}
				}
				logger.debug("sleeping for one min");
				Thread.sleep(60000);
			}
			return json;
		} catch (Exception e) {
			logger.error("Error", e);

			return null;
		}
	}

	public static Boolean mongodbDump(String dateString) {

		try {
			Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c",
					"/usr/bin/mongodump --db " + Config.binsDatabaseName + " --out " + dateString });
			process.waitFor();
			int exitCode = process.exitValue();
			StringBuffer output = new StringBuffer();
			BufferedReader reader = null;
			if (exitCode == 0) {
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			} else {
				reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			}
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			logger.debug("Process output " + output);

			if (exitCode == 0) {
				output = new StringBuffer();
				Process process2 = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c",
						"/usr/bin/mongodump --db " + Config.projectsDatabaseName + " --out " + dateString });
				process2.waitFor();
				exitCode = process2.exitValue();
				if (exitCode == 0) {
					reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				} else {
					reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				}
				line = "";
				while ((line = reader.readLine()) != null) {
					output.append(line + "\n");
				}
				logger.debug("Process output " + output);
				if (exitCode == 0) {
					return true;
				}
			}
			return false;
		} catch (IOException | InterruptedException e) {
			logger.error("Error", e);
			return false;
		}
	}

	public static Boolean deleteCollections(String databaseName) {
		MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
		try {
			MongoDatabase database = mongoClient.getDatabase(databaseName);
			MongoIterable<String> iterable = database.listCollectionNames();
			if (iterable.first() != null) {
				for (String string : iterable) {
					database.getCollection(string).drop();
					logger.debug("Deleted Collection " + string + " from Database " + databaseName);
				}
			}
			mongoClient.close();
			return true;
		} catch (Exception e) {
			logger.error("Error ", e);
			mongoClient.close();
			return false;
		}
	}

}
