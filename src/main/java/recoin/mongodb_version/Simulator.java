package recoin.mongodb_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;

public class Simulator {
	final static Logger logger = Logger.getLogger(Simulator.class);

	static int counter = 1;
	static String ShareFile = "testing/SimulatorDataset/SHARE.txt";
	static String EnrichFile = "testing/SimulatorDataset/ENRICH.txt";
	static String yandexUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
	static String linksRegex = "(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
	static String hashtagsRegex = "#\\w+";
	static String mentionsRegex = "@[a-zA-Z_]";
	static String SOURCE = "Simulator";
	static String[] instructionSet = { "PRIO", "SHARE", "ENRICH", "TRANS" };
	static String[] yandexLanguages = { "en", "ar", "de", "es", "fr" };

	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");
		logger.info("Starting the Simulator process");
		logger.info("Simulator will be repeated every " + Config.SimulatorTime
				+ " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.SimulatorTime + " ms");
				Thread.sleep(Integer.valueOf(Config.SimulatorTime));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}
	}

	public static void run() {
		try {
			ArrayList<Document> tasks = MongodbMethods
					.getIncompletedTasksFromMongoDB("task_status");
			if (tasks != null) {
				if (!tasks.isEmpty()) {
					Random random1 = new Random();
					int randomNum = random1.nextInt(tasks.size());
					Document doc = tasks.get(randomNum);
					Integer task_id = doc.getInteger("task_id");
					Integer project_id = doc.getInteger("project_id");
					String task_text = doc.getString("task_text");
					String contributor_name = SOURCE + counter;
					String source = SOURCE;
					String text = null;
					Random random2 = new Random();
					int randomNum2 = random2.nextInt(instructionSet.length);
					String chosenInstruction = instructionSet[randomNum2];
					switch (chosenInstruction) {
					case "PRIO":
						text = insert_Prio_instrctionSet();
						break;
					case "SHARE":
						text = insert_Share_instrctionSet();
						break;
					case "ENRICH":
						text = insert_Enrich_instrctionSet();
						break;
					case "TRANS":
						text = insert_Trans_instrctionSet(task_text);
						break;
					}
					if (text != null) {
						logger.debug("About to insert a task");
						logger.debug("task_id " + task_id + " " + "project_id "
								+ project_id + " contributor_name "
								+ contributor_name + " source" + source
								+ " text " + text);
						Boolean isInserted = MongodbMethods.insertTaskRun(text,
								task_id, project_id, contributor_name, source);
						if (isInserted) {
							logger.info("TaskRun was inserted");
						} else {
							logger.error("Error inserting the task run");
						}
					} else {
						logger.error("Error instatiating a text for the task Run!!");
					}

				} else {
					logger.error("There are no tasks to be processed");
				}
			}

			logger.debug("Adding task_run_id field to collection "
					+ Config.taskRunCollection);
			MongodbMethods.updateTaskRunsByAddingCounters();
			counter++;
		} catch (Exception e) {
			logger.error("Error ", e);
		}

	}

	public static String insert_Prio_instrctionSet() {
		Random random1 = new Random();
		int randomNum = random1.nextInt(1);
		if (randomNum == 0) {
			return "PRIO +1";
		} else {
			return "PRIO -1";
		}
	}

	public static String insert_Share_instrctionSet() {
		String user = getLineFromFile(ShareFile);
		if (user != null) {
			return "SHARE " + user;
		} else {
			return "SHARE random" + counter;
		}
	}

	public static String insert_Enrich_instrctionSet() {
		String enrich = getLineFromFile(EnrichFile);
		if (enrich != null) {
			return "ENRICH " + enrich;
		} else {
			return "ENRICH random" + counter;
		}
	}

	public static String insert_Trans_instrctionSet(String task_text) {
		task_text = removeHashtagsAndLinks(task_text);
		Random random1 = new Random();
		int randomNum = random1.nextInt(yandexLanguages.length);
		String chosenLanguage = yandexLanguages[randomNum];
		JSONObject translationJSON = getTranslation(task_text, chosenLanguage,
				"plain", 1);
		JSONArray transaltionArray = translationJSON.getJSONArray("text");
		String translationText = transaltionArray.getString(0);
		logger.debug("Translating text from " + task_text);
		logger.debug("Translating text to " + translationText);

		return "TRANS " + translationText;
	}

	public static String getLineFromFile(String file) {
		try {
			List<String> lines = FileUtils.readLines(new File(file), "utf-8");
			Random random1 = new Random();
			int randomNum = random1.nextInt(lines.size());
			return lines.get(randomNum);
		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}

	}

	public static String removeHashtagsAndLinks(String text) {
		text = text.replaceAll(hashtagsRegex, "");
		text = text.replaceAll(linksRegex, "");
		text = text.replaceAll(mentionsRegex, "");
		return text;
	}

	public static JSONObject getTranslation(String text, String lang,
			String format, int options) {

		String url = yandexUrl + "key=" + Config.yandexKey + "&text=" + text
				+ "&lang=" + lang + "&format=" + format + "&options=" + options
				+ "";

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			int responseCode = con.getResponseCode();
			logger.debug("\nSending 'GET' request to URL : " + url);
			logger.debug("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			logger.debug("Yandex Response " + response.toString());
			JSONObject json = null;
			if (responseCode == 200) {
				json = new JSONObject(response.toString());
				return json;
			} else {
				return null;
			}

		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}

	}
}
