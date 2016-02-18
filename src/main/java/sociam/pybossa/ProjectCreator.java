package sociam.pybossa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class ProjectCreator {
	final static Logger logger = Logger.getLogger(ProjectCreator.class);
	static MongoClient mongoClient = new MongoClient(Config.mongoHost, Config.mongoPort);
	static MongoDatabase database = mongoClient.getDatabase(Config.projectsDatabaseName);
	static String url = Config.PyBossahost + Config.projectDir + Config.api_key;

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");

		logger.info("ProjectCreator will be readped every " + Config.ProjectCreatorTrigger + " ms");
		try {
			while (true) {
				run();
				logger.info("Sleeping for " + Config.ProjectCreatorTrigger + " ms");
				Thread.sleep(Integer.valueOf(Config.ProjectCreatorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error " + e);
		}

	}

	public static void run() {
		HashSet<Document> projectsAsdocs = checkMongoForUnStartedProjects();

		if (!projectsAsdocs.isEmpty()) {
			logger.info("There are " + projectsAsdocs.size()
					+ " projects need to be inserted into PyBossa and then updated within MongoDB");
			for (Document document : projectsAsdocs) {
				String project_name = document.getString("project_name");
				ObjectId _id = document.getObjectId("_id");
				JSONObject jsonData = BuildJsonPorject(project_name, project_name, project_name,
						Config.project_validation_templatePath);
				JSONObject PyBossaResponse = createProjectInPyBossa(url, jsonData);
				if (PyBossaResponse != null) {
					logger.debug("Project: " + project_name + " was sucessfully inserted into PyBossa");
					logger.debug(PyBossaResponse.toString());
					int project_id = PyBossaResponse.getInt("id");
					Boolean wasUpdated = updateProjectIntoMongoDB(_id, project_id, true);
					if (wasUpdated) {
						logger.debug("Project " + project_name + " was sucessfully updated to have projectID: "
								+ project_id + " and project_started=true");
					} else {
						logger.error("Could't update project " + project_name + " in MongoDB");
					}
				} else {
					logger.error("Porject with the name " + project_name + " couldn't be inserted into PyBossa");
				}
			}
		} else {
			logger.debug("There are no new projects to be inserted into PyBossa!");
		}
	}

	private static Boolean updateProjectIntoMongoDB(ObjectId _id, int project_id, Boolean project_started) {

		UpdateResult result = database.getCollection(Config.projectCollection).updateOne(new Document("_id", _id),
				new Document("$set", new Document("project_started", String.valueOf(project_started))
						.append("project_id", String.valueOf(project_id))));
		logger.debug(result.toString());
		if (result.wasAcknowledged()) {
			if (result.getMatchedCount() > 0) {
				return true;
			}
		}
		return false;
	}

	static HashSet<Document> jsons = new LinkedHashSet<Document>();

	private static HashSet<Document> checkMongoForUnStartedProjects() {
		jsons = new LinkedHashSet<Document>();
		FindIterable<Document> iterable = database.getCollection(Config.projectCollection)
				.find(new Document("project_started", "false"));

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

	}

	/**
	 * This method creates a project on a given url that accepts json doc - in
	 * this case its the PyBossa url and credentials
	 * 
	 * @param url
	 *            the like for the host alongside credentials.
	 * @param jsonData
	 *            the json doc which should have the project parms.
	 * 
	 * @return Boolean true if its created, false otherwise.
	 **/
	private static JSONObject createProjectInPyBossa(String url, JSONObject jsonData) {
		JSONObject jsonResult = null;
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData.toString());
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
				return null;
			}
		} catch (Exception ex) {
			logger.error(ex);
			return null;
		}
	}

	/**
	 * This returns a json string from a given project's details
	 * 
	 * @param name
	 *            the name of the project
	 * @param shortName
	 *            a short name for the project
	 * @param description
	 *            a description for the project // This could be incrimental
	 *            later!
	 * @return Json string
	 */
	private static JSONObject BuildJsonPorject(String name, String shortName, String description, String templeteFile) {

		JSONObject app2 = new JSONObject();
		String templete = readFile(templeteFile);
		templete = templete.replaceAll("\\[project short name\\]", shortName);
		app2.put("task_presenter", templete);
		JSONObject app = new JSONObject();
		app.put("name", name);
		app.put("short_name", shortName);
		app.put("description", description);
		// app.put("created", true);
		app.put("allow_anonymous_contributors", true);
		// TODO: publishing through the api is not allowed - we leave it to be
		// done manually!
		// app.put("published", true);
		app.put("info", app2);

		return app;
	}

	static String readFile(String path) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			logger.error(e);
		}
		return new String(encoded, StandardCharsets.UTF_8);
	}
}
