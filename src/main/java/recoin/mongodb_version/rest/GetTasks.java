package recoin.mongodb_version.rest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import sociam.pybossa.methods.MongodbMethods;
import spark.Request;
import spark.Response;

public class GetTasks {
	final static Logger logger = Logger.getLogger(GetTasks.class);

	// public static void main(String[] args) {
	// port(RestMethods.port);
	// PropertyConfigurator.configure("log4j.properties");
	// Config.reload();
	// get("/getTasks", "application/json", (request, response) ->
	// getSomeTasks(request, response));
	//
	// }

	public static JSONObject getSomeTasks(Request request, Response response) {
		JSONObject jsonResponse = new JSONObject();
		try {
			response.type("application/json");
			jsonResponse = MongodbMethods.getLatestUnAnsweredTask();
			if (jsonResponse != null) {
				jsonResponse.put("message", "The task has not been answered before");
			} else {
				jsonResponse = MongodbMethods.getLatestUncompletedAnsweredTask();
				if (jsonResponse != null) {
					jsonResponse.put("message", "The task has some answeres, but not yet completed");
				} else {
					jsonResponse = new JSONObject();
					jsonResponse.put("message", "There are no tasks to be answered!");
				}
			}

			jsonResponse.put("status", "success");
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
