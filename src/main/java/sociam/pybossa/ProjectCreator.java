package sociam.pybossa;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import sociam.pybossa.methods.PybossaMethods;


/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */

public class ProjectCreator {
	final static Logger logger = Logger.getLogger(ProjectCreator.class);
	static String url = Config.PyBossahost + Config.projectDir + Config.api_key;

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");

		logger.info("ProjectCreator will be repeated every "
				+ Config.ProjectCreatorTrigger + " ms");
		try {
			while (true) {
				Config.reload();
				run();
				logger.info("Sleeping for " + Config.ProjectCreatorTrigger
						+ " ms");
				Thread.sleep(Integer.valueOf(Config.ProjectCreatorTrigger));
			}
		} catch (InterruptedException e) {
			logger.error("Error ", e);
		}

	}

	public static void run() {
		try {
			HashSet<Document> projectsAsdocs = MongodbMethods.getAllProjects();

			if (projectsAsdocs != null) {
				logger.info("There are "
						+ projectsAsdocs.size()
						+ " projects need to be inserted into PyBossa and then updated within MongoDB");
				logger.info("ProjectLimit " + Config.ProjectLimit);
				if (!projectsAsdocs.isEmpty()) {
					for (Document document : projectsAsdocs) {
						String project_status = document
								.getString("project_status");
						if (project_status.equals("empty")) {
							String project_name = document
									.getString("project_name");
							ObjectId _id = document.getObjectId("_id");
							JSONObject jsonData = PybossaMethods.BuildJsonPorject(
									project_name, project_name, project_name,
									Config.project_validation_templatePath);
							JSONObject PyBossaResponse = PybossaMethods.createProjectInPyBossa(
									url, jsonData);
							if (PyBossaResponse != null) {
								logger.debug("Project: "
										+ project_name
										+ " was sucessfully inserted into PyBossa");
								logger.debug(PyBossaResponse.toString());
								int project_id = PyBossaResponse.getInt("id");
								Boolean wasUpdated = MongodbMethods.updateProjectIntoMongoDB(
										_id, project_id, "ready", "validate");
								if (wasUpdated) {
									logger.debug("Project "
											+ project_name
											+ " was sucessfully updated to have projectID: "
											+ project_id
											+ " and project_started=true");
								} else {
									logger.error("Could't update project "
											+ project_name + " in MongoDB");
								}
							} else {
								logger.error("Porject with the name "
										+ project_name
										+ " couldn't be inserted into PyBossa");
							}
							// for testing
							// break;
						} else {
							logger.debug("There are no empty projects");
						}
					}
				} else {
					logger.debug("There are no projects in the collection "
							+ Config.projectCollection);
				}
			} else {
				logger.debug("There are no projects in the collection "
						+ Config.projectCollection);
			}
		} catch (Exception e) {
			logger.error("Error ", e);
		}
	}


}
