package sociam.pybossa.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	public static String PyBossahost;
	public static String taskDir;
	public static String projectDir;
	public static String taskRunDir;

	public static String api_key;
	public static String project_validation_templatePath;

	public static String mongoHost;
	public static int mongoPort;
	public static String binsDatabaseName;
	public static String projectsDatabaseName;
	public static String taskCollection;
	public static String taskRunCollection;
	public static String projectCollection;

	public static String TaskCreatorTrigger;
	public static String TaskCollectorTrigger;
	public static String ProjectCreatorTrigger;
	public static String TaskPerformerTrigger;
	public static String TaskPerformerPushRate;

	public static String RePushTaskToTwitter;

	public static String TasksPerProject;
	public static String ProjectLimit;

	public static String project_validation_question;

	public static void reload() {

		Properties p = new Properties();
		try (InputStream stream = Config.class.getResourceAsStream("/config.properties")) {
			p.load(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		PyBossahost = p.getProperty("PyBossahost");
		taskDir = p.getProperty("taskDir");
		taskRunDir = p.getProperty("taskRunDir");
		projectDir = p.getProperty("projectDir");
		api_key = p.getProperty("api_key");
		project_validation_templatePath = p.getProperty("project_validation_templatePath");

		mongoHost = p.getProperty("mongoHost");
		mongoPort = Integer.valueOf(p.getProperty("mongoPort"));
		binsDatabaseName = p.getProperty("binsDatabaseName");
		projectsDatabaseName = p.getProperty("projectsDatabaseName");
		taskCollection = p.getProperty("taskCollection");
		taskRunCollection = p.getProperty("taskRunCollection");
		projectCollection = p.getProperty("projectCollection");

		TaskCreatorTrigger = p.getProperty("TaskCreatorTrigger");
		TaskCollectorTrigger = p.getProperty("TaskCollectorTrigger");
		ProjectCreatorTrigger = p.getProperty("ProjectCreatorTrigger");
		TaskPerformerTrigger = p.getProperty("TaskPerformerTrigger");
		TaskPerformerPushRate = p.getProperty("TaskPerformerPushRate");

		RePushTaskToTwitter = p.getProperty("RePushTaskToTwitter");

		TasksPerProject = p.getProperty("TasksPerProject");
		ProjectLimit = p.getProperty("ProjectLimit");

		project_validation_question = p.getProperty("project_validation_question");

	}

	static {
		Properties p = new Properties();
		try (InputStream stream = Config.class.getResourceAsStream("/config.properties")) {
			p.load(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		PyBossahost = p.getProperty("PyBossahost");
		taskDir = p.getProperty("taskDir");
		taskRunDir = p.getProperty("taskRunDir");
		projectDir = p.getProperty("projectDir");
		api_key = p.getProperty("api_key");
		project_validation_templatePath = p.getProperty("project_validation_templatePath");

		mongoHost = p.getProperty("mongoHost");
		mongoPort = Integer.valueOf(p.getProperty("mongoPort"));
		binsDatabaseName = p.getProperty("binsDatabaseName");
		projectsDatabaseName = p.getProperty("projectsDatabaseName");
		taskCollection = p.getProperty("taskCollection");
		taskRunCollection = p.getProperty("taskRunCollection");
		projectCollection = p.getProperty("projectCollection");

		TaskCreatorTrigger = p.getProperty("TaskCreatorTrigger");
		TaskCollectorTrigger = p.getProperty("TaskCollectorTrigger");
		ProjectCreatorTrigger = p.getProperty("ProjectCreatorTrigger");
		TaskPerformerTrigger = p.getProperty("TaskPerformerTrigger");
		TaskPerformerPushRate = p.getProperty("TaskPerformerPushRate");

		RePushTaskToTwitter = p.getProperty("RePushTaskToTwitter");

		TasksPerProject = p.getProperty("TasksPerProject");
		ProjectLimit = p.getProperty("ProjectLimit");

		project_validation_question = p.getProperty("project_validation_question");

	}

}
