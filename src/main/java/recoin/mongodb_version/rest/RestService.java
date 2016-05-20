package recoin.mongodb_version.rest;

import static spark.Spark.get;
import static spark.Spark.port;

import org.apache.log4j.PropertyConfigurator;

import sociam.pybossa.config.Config;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */

public class RestService {

	public static void main(String[] args) {
		port(Integer.valueOf(Config.restPort));
		PropertyConfigurator.configure("log4j.properties");
		Config.reload();
		get("/Project", "application/json", (request, response) -> ProjectRest.getAllProjects(request, response));
		get("/Project/:id/", "application/json",
				(request, response) -> ProjectRest.getProjectWithID(request, response));
		get("/Task", "application/json", (request, response) -> TaskRest.getAllTasks(request, response));
		get("/Task/:id", "application/json", (request, response) -> TaskRest.getTaskWithID(request, response));
		get("/Task/:id/Responses", "application/json",
				(request, response) -> TaskRest.getTaskRunswithTask(request, response));
		get("/Task/project", "application/json",
				(request, response) -> TaskRest.getTaskWithProjectID(request, response));
		get("/TaskRun", "application/json", (request, response) -> TaskRunRest.getAllTaskRuns(request, response));
		get("/TaskRun/:id", "application/json", (request, response) -> TaskRunRest.getTaskWithID(request, response));
		get("/TaskRun/task", "application/json",
				(request, response) -> TaskRunRest.getTaskRunsWithTask(request, response));
		get("/TaskRun/project", "application/json",
				(request, response) -> TaskRunRest.getTaskRunswithProject(request, response));
		get("/getTasks", "application/json", (request, response) -> GetTasks.getSomeTasks(request, response));

	}
}
