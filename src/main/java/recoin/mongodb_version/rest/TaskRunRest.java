package recoin.mongodb_version.rest;

import org.apache.log4j.Logger;
import org.bson.Document;
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
			jsonResponse = MongodbMethods.getStatsFroRest(
					Config.taskRunCollection, null, null, offset, limit);
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

	public static JSONObject getTaskRunsWithTask(Request request,
			Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			Integer task_id = Integer.valueOf(request.queryParams("task_id"));
			jsonResponse = MongodbMethods
					.getStatsFroRest(Config.taskRunCollection, "task_id",
							task_id, offset, limit);
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

	public static JSONObject getTaskRunswithProject(Request request,
			Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			Integer offset = RestMethods.setOffset(request);
			Integer limit = RestMethods.setLimit(request);
			Integer project_id = Integer.valueOf(request.queryParams("project_id"));
			jsonResponse = MongodbMethods.getStatsFroRest(
					Config.taskRunCollection, "project_id", project_id, offset,
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
			jsonResponse = MongodbMethods.getStatsFroRest(
					Config.taskRunCollection, "task_run_id", id, offset, limit);
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

	public static JSONObject insertTaskRun(Request request, Response response) {

		JSONObject data = new JSONObject();
		JSONObject status = new JSONObject();
		try {
			String text = request.queryParams("text");
			Integer task_id = Integer.valueOf(request.queryParams("task_id"));
			Integer project_id = Integer.valueOf(request.queryParams("project_id"));
			String contributor_name = request.queryParams("contributor_name");
			String source = request.queryParams("source");

			if (text != null && task_id != null && project_id != null
					&& contributor_name != null && source != null) {
				logger.debug("receiving a GET request with the following data + text="
						+ text
						+ " task_id="
						+ task_id
						+ " project_id="
						+ project_id
						+ " contributor_name="
						+ contributor_name
						+ " source=" + source);
				if (!(source.equals("TaskView") && text.contains("PRIO"))) {
					Document taskRun = MongodbMethods.getTaskRunsFromMongoDB(
							task_id, contributor_name, text);
					if (taskRun != null) {
						logger.error("You are only allowed one contribution for each task.");
						logger.error("task_id= " + task_id + " screen_name: "
								+ contributor_name);
						status.put("message",
								"duplicate - no instruction added");
						status.put("status", "error");
						response.status(500);
						return status;
					}
				}
				Boolean isInserted = MongodbMethods.insertTaskRun(text,
						task_id, project_id, contributor_name, source);
				if (isInserted) {
					logger.info("TaskRun was inserted");

					data.put("text", text);
					data.put("task_id", task_id);
					data.put("project_id", project_id);
					data.put("contributor_name", contributor_name);
					data.put("source", source);
					status.put("data", data);
					status.put("status", "success");
					response.status(200);
					return status;
				} else {
					logger.error("Task run could not be inserted");
					status.put("message",
							"Did you already inserted the task run?");
					status.put("status", "error");
					response.status(500);
					return status;
				}
			} else {
				logger.error("All parameters should be provided");
				status.put("status",
						"Error: All parameters should be provided. text="
								+ text + "task_id=" + task_id + " project_id="
								+ project_id + " contributor_name="
								+ contributor_name + " source=" + source);
				response.status(500);
				return status;
			}
		} catch (Exception e) {
			logger.error("error", e);
			status = new JSONObject();
			status.put("status", "error");
			status.put("message", e);
			response.status(500);
			return status;
		}
	}
}
