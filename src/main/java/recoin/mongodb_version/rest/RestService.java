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
		String domain = "/RecoinRestController";
		Config.reload();
		get(domain +"/Project", "application/json", (request, response) -> ProjectRest.getAllProjects(request, response));
		get(domain +"/Project/:id", "application/json",
				(request, response) -> ProjectRest.getProjectWithID(request, response));
		get(domain +"/Task", "application/json", (request, response) -> TaskRest.getAllTasks(request, response));
		get(domain +"/Task/project", "application/json",
				(request, response) -> TaskRest.getTaskWithProjectID(request, response));
		get(domain +"/Task/:id", "application/json", (request, response) -> TaskRest.getTaskWithID(request, response));
		get(domain +"/Task/:id/Responses", "application/json",
				(request, response) -> TaskRest.getTaskRunswithTask(request, response));
		get(domain +"/TaskRun", "application/json", (request, response) -> TaskRunRest.getAllTaskRuns(request, response));
		get(domain +"/TaskRun/project", "application/json",
				(request, response) -> TaskRunRest.getTaskRunswithProject(request, response));
		get(domain +"/TaskRun/:id", "application/json", (request, response) -> TaskRunRest.getTaskWithID(request, response));
		get(domain +"/TaskRun/task", "application/json",
				(request, response) -> TaskRunRest.getTaskRunsWithTask(request, response));
		get(domain +"/getTasks", "application/json", (request, response) -> GetTasks.getSomeTasks(request, response));
		get(domain +"/sendTaskRun", "application/json", (request, response) -> TaskRunRest.insertTaskRun(request, response));

	}
}
