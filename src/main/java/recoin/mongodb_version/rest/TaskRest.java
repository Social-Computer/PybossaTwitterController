package recoin.mongodb_version.rest;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import sociam.pybossa.config.Config;
import sociam.pybossa.methods.MongodbMethods;
import spark.Request;
import spark.Response;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */

public class TaskRest {
	final static Logger logger = Logger.getLogger(TaskRest.class);

	// public static void main(String[] args) {
	// port(RestMethods.port);
	// PropertyConfigurator.configure("log4j.properties");
	// Config.reload();
	// get("/Task", "application/json", (request, response) ->
	// getAllTasks(request, response));
	// get("/Task/:id", "application/json", (request, response) ->
	// getTaskWithID(request, response));
	// get("/Task/:id/Responses", "application/json", (request, response) ->
	// getTaskRunswithTask(request, response));
	// get("/Task/project", "application/json", (request, response) ->
	// getTaskWithProjectID(request, response));
	//
	// }

	public static JSONObject getAllTasks(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			jsonResponse = MongodbMethods.getStatsFroRest(Config.taskCollection, null, null, offset, limit);
			response.status(200);
			return jsonResponse;
		} catch (Exception e) {
			logger.error("error", e);
			jsonResponse = new JSONObject();
			jsonResponse.put("status", "error");
			jsonResponse.put("message", e);
			response.status(500);
			return jsonResponse;

		}
	}

	public static JSONObject getTaskWithID(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			Integer id = Integer.valueOf(request.params(":id"));
			jsonResponse = MongodbMethods.getStatsFroRest(Config.taskCollection, "task_id", id, offset, limit);
			response.status(200);
			return jsonResponse;
		} catch (Exception e) {
			logger.error("error", e);
			jsonResponse = new JSONObject();
			jsonResponse.put("status", "error");
			jsonResponse.put("message", e);
			response.status(500);
			return jsonResponse;

		}
	}

	public static JSONObject getTaskWithProjectID(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			Integer project_id = Integer.valueOf(request.queryParams("project_id"));
			jsonResponse = MongodbMethods.getStatsFroRest(Config.taskCollection, "project_id", project_id, offset,
					limit);
			response.status(200);
			return jsonResponse;
		} catch (Exception e) {
			logger.error("error", e);
			jsonResponse = new JSONObject();
			jsonResponse.put("status", "error");
			jsonResponse.put("message", e);
			response.status(500);
			return jsonResponse;
		}
	}

	public static JSONObject getTaskRunswithTask(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			Integer id = Integer.valueOf(request.params(":id"));
			jsonResponse = MongodbMethods.getStatsFroRest(Config.taskRunCollection, "task_id", id, offset, limit);
			response.status(200);
			return jsonResponse;
		} catch (Exception e) {
			logger.error("error", e);
			jsonResponse = new JSONObject();
			jsonResponse.put("status", "error");
			jsonResponse.put("message", e);
			response.status(500);
			return jsonResponse;
		}
	}
}
