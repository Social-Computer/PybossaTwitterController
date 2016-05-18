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

public class TaskRunRest {
	final static Logger logger = Logger.getLogger(TaskRest.class);

	// public static void main(String[] args) {
	// port(RestMethods.port);
	// PropertyConfigurator.configure("log4j.properties");
	// Config.reload();
	// get("/TaskRun", "application/json", (request, response) ->
	// getAllTaskRuns(request, response));
	// get("/TaskRun/:id", "application/json", (request, response) ->
	// getTaskWithID(request, response));
	// get("/TaskRun/task", "application/json", (request, response) ->
	// getTaskRunsWithTask(request, response));
	// get("/TaskRun/project", "application/json", (request, response) ->
	// getTaskRunswithProject(request, response));
	//
	// }

	public static JSONObject getAllTaskRuns(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			jsonResponse = MongodbMethods.getStatsFroRest(Config.taskRunCollection, null, null, offset, limit);
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

	public static JSONObject getTaskRunsWithTask(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			Integer task_id = Integer.valueOf(request.params("task_id"));
			jsonResponse = MongodbMethods.getStatsFroRest(Config.taskRunCollection, "task_id", task_id, offset, limit);
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

	public static JSONObject getTaskRunswithProject(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			Integer project_id = Integer.valueOf(request.params("project_id"));
			jsonResponse = MongodbMethods.getStatsFroRest(Config.taskRunCollection, "project_id", project_id, offset,
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

	public static JSONObject getTaskWithID(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			Integer id = Integer.valueOf(request.params(":id"));
			jsonResponse = MongodbMethods.getStatsFroRest(Config.taskRunCollection, "task_run_id", id, offset, limit);
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
