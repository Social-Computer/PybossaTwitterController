package sociam.pybossa;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class ProjectCreator {
	final static Logger logger = Logger.getLogger(ProjectCreator.class);

	public static String host = "http://recoin.cloudapp.net:5000";
	public static String projectDir = "/api/project";
	public static String api_key = "?api_key=acc193bd-6930-486e-9a21-c80005cbbfd2";

	public static void main(String[] args) {
		BasicConfigurator.configure();

		String jsonData = BuildJsonPorject("test6", "test6", "test6");
		String url = host + projectDir + api_key;
		createProject(url, jsonData);

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
	public static Boolean createProject(String url, String jsonData) {

		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData);
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
				}
				return true;
			} else {
				logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				return false;
			}
		} catch (Exception ex) {
			logger.error(ex);
			return false;
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
	@SuppressWarnings("unchecked")
	private static String BuildJsonPorject(String name, String shortName, String description) {

		JSONObject app2 = new JSONObject();
		app2.put("task_presenter", "<style type=\"text/css\">\n" + "    .textContainer {\n"
				+ "        border-radius: 5px;\n" + "        background-color: #f2f2f2;\n" + "        color: #2B9884;\n"
				+ "        padding: 12px;\n" + "    }\n" + "</style>\n" + "\n"
				+ "<div id=\"warning\" class=\"modal fade\">\n" + "  <div class=\"modal-dialog\">\n"
				+ "    <div class=\"modal-content\">\n" + "      <div class=\"modal-header\">\n"
				+ "        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\n"
				+ "        <h4 class=\"modal-title\"></h4>\n" + "      </div>\n" + "      <div class=\"modal-body\">\n"
				+ "      </div>\n" + "      <div class=\"modal-footer\">\n"
				+ "        <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>\n"
				+ "      </div>\n" + "    </div><!-- /.modal-content -->\n" + "  </div><!-- /.modal-dialog -->\n"
				+ "</div><!-- /.modal -->\n" + "\n" + "<div class=\"row skeleton\">\n"
				+ "    <div class=\"col-xs-8 col-xs-offset-2\">\n"
				+ "        <h2 id=\"question\">Does this content indicate a real-world event?</h2>\n" + "    </div>\n"
				+ "    <div class=\"hidden-xs col-sm-2\">\n"
				+ "        <p style=\"margin-top:30px;\"><span id=\"i18n_working_task\">Task</span> <span id=\"task-id\">#</span></p>\n"
				+ "    </div>\n" + "</div>\n" + "<div class=\"row\">\n"
				+ "    <div class=\"top20 col-xs-8 col-xs-offset-2\">\n" + "        <p class=\"textContainer\">\n"
				+ "            <span id=\"text\"></span>\n" + "        </p>\n" + "    </div>\n"
				+ "    <div id=\"answer\" class=\"top20 col-xs-8 col-xs-offset-2\">\n"
				+ "        <button class=\"btn btn-answer\" value=\"yes\">Yes</button>\n"
				+ "        <button class=\"btn btn-answer\" value=\"no\">No</button>\n" + "    </div>\n" + "</div>\n"
				+ "<script type=\"text/javascript\">\n" + "(function() {\n" + "// Default language\n"
				+ "var userLocale = \"en\";\n" + "// Translations\n" + "var messages = {\"en\": {\n"
				+ "                        \"i18n_working_task\": \"Task\",\n" + "                      },\n"
				+ "               };\n" + "// Update userLocale with server side information\n"
				+ "$(document).ready(function(){\n"
				+ "    userLocale = document.getElementById('PYBOSSA_USER_LOCALE').textContent.trim();\n" + "});\n"
				+ "function i18n_translate() {\n" + "    var ids = Object.keys(messages[userLocale])\n"
				+ "    for (i=0; i<ids.length; i++) {\n" + "        console.log(\"Translating: \" + ids[i]);\n"
				+ "        document.getElementById(ids[i]).innerHTML = messages[userLocale][ids[i]];\n" + "    }\n"
				+ "}\n" + "pybossa.taskLoaded(function(task, deferred) {\n" + "    deferred.resolve(task);\n" + "});\n"
				+ "function warn_user(type, msg) {\n" + "    var p = $(\"<p/>\");\n" + "    var title;\n"
				+ "    if (type === 'info') {\n" + "        title = \"Hi there! Please, read this carefully\"\n"
				+ "    }\n" + "    if (type === 'error') {\n" + "        title = \"Ooops! Something went wrong!\"\n"
				+ "    }\n" + "    if (type === 'warning') {\n"
				+ "        title = \"Hi there! Please, read this carefully\"\n" + "    }\n" + "    p.text(msg);\n"
				+ "    $(\".modal-title\").text(title);\n" + "    $(\".modal-body\").html(p);\n"
				+ "    $(\"#warning\").modal();\n" + "}\n" + "pybossa.presentTask(function(task, deferred) {\n"
				+ "    if ( !$.isEmptyObject(task) ) {\n" + "        i18n_translate();\n"
				+ "        $('#task-id').html(task.id);\n"
				+ "        document.getElementById('text').innerHTML = task.info.text;\n"
				+ "        $('.btn-answer').off('click').on('click', function(evt) {\n"
				+ "            var $btn = $(this);\n" + "            var answer = $btn.attr(\"value\");\n"
				+ "            if (typeof answer != 'undefined') {\n"
				+ "                pybossa.saveTask(task.id, answer).done(function() {\n"
				+ "                    deferred.resolve();\n" + "                });\n" + "            }\n"
				+ "            else {\n" + "                warn_user('error', 'Unexpected type of answer.');\n"
				+ "            }\n" + "        });\n" + "    }\n" + "    else {\n"
				+ "        $(\".skeleton\").hide();\n"
				+ "        warn_user('info', 'You have contributed to all available tasks! Thanks!');\n" + "    }\n"
				+ "});\n" + "pybossa.run('" + shortName + "');\n" + "})();\n" + "</script>");
		JSONObject app = new JSONObject();

		app.put("name", name);
		app.put("short_name", shortName);
		app.put("description", description);
		app.put("created", true);
		app.put("allow_anonymous_contributors", true);
		// TODO: publishing through the api is not allowed - we leave it to be
		// done manually!
		// app.put("published", true);
		app.put("info", app2);

		return app.toJSONString();
	}

}
