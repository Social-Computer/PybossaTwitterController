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

import sociam.pybossa.config.Config;

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

		logger.info("ProjectCreator will be repeated every " + Config.ProjectCreatorTrigger + " ms");
		try {
			while (true) {
				run();
				logger.info("Sleeping for " + Config.ProjectCreatorTrigger + " ms");
				Thread.sleep(Integer.valueOf(Config.ProjectCreatorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}

	}

	public static void run() {
		try {
			HashSet<Document> projectsAsdocs = getAllProjects();

			if (projectsAsdocs != null) {
				logger.info("There are " + projectsAsdocs.size()
						+ " projects need to be inserted into PyBossa and then updated within MongoDB");
				logger.info("ProjectLimit " + Config.ProjectLimit);
				if (!projectsAsdocs.isEmpty()) {
					for (Document document : projectsAsdocs) {
						String project_status = document.getString("project_status");
						if (project_status.equals("empty")) {
							String project_name = document.getString("project_name");
							ObjectId _id = document.getObjectId("_id");
							JSONObject jsonData = BuildJsonPorject(project_name, project_name, project_name,
									Config.project_validation_templatePath);
							JSONObject PyBossaResponse = createProjectInPyBossa(url, jsonData);
							if (PyBossaResponse != null) {
								logger.debug("Project: " + project_name + " was sucessfully inserted into PyBossa");
								logger.debug(PyBossaResponse.toString());
								int project_id = PyBossaResponse.getInt("id");
								Boolean wasUpdated = updateProjectIntoMongoDB(_id, project_id, "ready");
								if (wasUpdated) {
									logger.debug(
											"Project " + project_name + " was sucessfully updated to have projectID: "
													+ project_id + " and project_started=true");
								} else {
									logger.error("Could't update project " + project_name + " in MongoDB");
								}
							} else {
								logger.error(
										"Porject with the name " + project_name + " couldn't be inserted into PyBossa");
							}
							// for testing
							// break;
						} else {
							logger.debug("There are no empty projects");
						}
					}
				} else {
					logger.debug("There are no projects in the collection " + Config.projectCollection);
				}
			} else {
				logger.debug("There are no projects in the collection " + Config.projectCollection);
			}
		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}

	public static Boolean updateProjectIntoMongoDB(ObjectId _id, int project_id, String project_status) {

		UpdateResult result = database.getCollection(Config.projectCollection).updateOne(new Document("_id", _id),
				new Document("$set", new Document("project_status", project_status).append("project_id", project_id)));
		logger.debug(result.toString());
		if (result.wasAcknowledged()) {
			if (result.getMatchedCount() > 0) {
				return true;
			}
		}
		return false;
	}

	// static HashSet<Document> jsons = new LinkedHashSet<Document>();

	public static HashSet<Document> getAllProjects() {
		try {
			HashSet<Document> jsons = new LinkedHashSet<Document>();
			FindIterable<Document> iterable = database.getCollection(Config.projectCollection).find(new Document())
					.limit(Integer.valueOf(Config.ProjectLimit));

			if (iterable.first() != null) {
				for (Document document : iterable) {
					jsons.add(document);
				}
			}
			return jsons;
		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}
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
	public static JSONObject createProjectInPyBossa(String url, JSONObject jsonData) {
		JSONObject jsonResult = null;
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
	public static JSONObject BuildJsonPorject(String name, String shortName, String description, String templeteFile) {

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
