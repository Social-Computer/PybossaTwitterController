package recoin.mongodb_version.rest;

import static spark.Spark.*;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import spark.Request;
import spark.Response;

public class Project {
	final static Logger logger = Logger.getLogger(Project.class);

	public static void main(String[] args) {
		port(1234);
		PropertyConfigurator.configure("log4j.properties");
		Config.reload();
		get("/Project", "application/json", (request, response) -> getAllProjects(request, response));
		get("/Project/:id/", "application/json", (request, response) -> getProjectWithID(request, response));

	}

	public static String getAllProjects(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			Integer offset = 0;
			Integer limit = 200;
			String offsetString = request.queryParams("offset");
			String limitString = request.queryParams("limit");
			if (offsetString != null) {
				offset = Integer.valueOf(offsetString);
			}
			if (limitString != null) {
				limit = Integer.valueOf(limitString);
			}

			jsonResponse = MongodbMethods.getStatsFroRest(Config.projectCollection, null, null, offset, limit);
			response.status(200);
			return jsonResponse.toString();
		} catch (Exception e) {
			logger.error("error", e);
			jsonResponse = new JSONObject();
			jsonResponse.put("status", "error");
			jsonResponse.put("message", e);
			response.status(500);
			return jsonResponse.toString();

		}
	}

	public static String getProjectWithID(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			Integer id = Integer.valueOf(request.params(":id"));
			Integer offset = 0;
			Integer limit = 200;
			String offsetString = request.queryParams("offset");
			String limitString = request.queryParams("limit");
			if (offsetString != null) {
				logger.debug("here");
				offset = Integer.valueOf(offsetString);
			}
			if (limitString != null) {
				limit = Integer.valueOf(limitString);
			}

			jsonResponse = MongodbMethods.getStatsFroRest(Config.projectCollection, "project_id", id, offset, limit);
			ArrayList<JSONObject> tasks = MongodbMethods.getTasksORRunsByProjectID("project_id", id,
					Config.taskCollection);
			Integer tasksCount = 0;
			if (tasks != null) {
				tasksCount = tasks.size();
			}

			ArrayList<JSONObject> taskRuns = MongodbMethods.getTasksORRunsByProjectID("project_id", id,
					Config.taskRunCollection);
			Integer taskRunsCount = 0;
			if (taskRuns != null) {
				taskRunsCount = taskRuns.size();
			}

			jsonResponse.put("tasks_count", tasksCount);
			jsonResponse.put("taskRuns_count", taskRunsCount);
			response.status(200);
			return jsonResponse.toString();
		} catch (Exception e) {
			logger.error("error", e);
			jsonResponse = new JSONObject();
			jsonResponse.put("status", "error");
			jsonResponse.put("message", e);
			response.status(500);
			return jsonResponse.toString();

		}
	}
}
