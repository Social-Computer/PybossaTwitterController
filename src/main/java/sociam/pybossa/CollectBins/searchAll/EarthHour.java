package sociam.pybossa.CollectBins.searchAll;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.types.ObjectId;

import sociam.pybossa.config.Config;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class EarthHour {
	static PrintWriter writer = null;
	static final String delimate = ";";
	static Pattern patter = Pattern
			.compile("earthhour|earthhour2016|earthhour16|myearthhour");

	public static void main(String[] args) {
		try {
			writer = new PrintWriter("EarthHour.csv", "UTF-8");
			writer.println("id" + delimate + "content" + delimate + "timestamp"
					+ delimate);
			ArrayList<String> collectionNames = getCollectionNames();
			printCollectionsContent(collectionNames);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getCollectionNames() {
		ArrayList<String> collectionNames = new ArrayList<String>();
		MongoClient mongoClient = new MongoClient(Config.mongoHost,
				Config.mongoPort);
		MongoDatabase database = mongoClient.getDatabase("EarthOur");

		MongoIterable<String> colls = database.listCollectionNames();

		for (String s : colls) {
			collectionNames.add(s);

		}

		mongoClient.close();

		return collectionNames;
	}

	public static void printCollectionsContent(ArrayList<String> collectionNames) {
		try {
			MongoClient mongoClient = new MongoClient(Config.mongoHost,
					Config.mongoPort);
			MongoDatabase database = mongoClient.getDatabase("EarthOur");
			for (String collection : collectionNames) {
				System.out.println("Processing Collection: " + collection);
				FindIterable<Document> iterable = database.getCollection(
						collection).find();
				if (iterable.first() != null) {
					for (Document document : iterable) {
						ObjectId _id = document.getObjectId("_id");
						String content = document.getString("text");
						Matcher matcher = patter.matcher(content.toLowerCase());
						if (matcher.find()) {
							String timeStamp = document.getString("timestamp");
							content = content.replaceAll(delimate, "");
							content = content.replaceAll("\n", "");
							content = content.replaceAll("\"", "");
							content = content.replaceAll("\'", "");
							System.out.println(_id.toString() + delimate
									+ content + delimate + timeStamp);
							writer.println(_id.toString() + delimate + "\""
									+ content + "\"" + delimate + "\""
									+ timeStamp + "\"" + delimate);
						}

					}
				}
			}
			mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
