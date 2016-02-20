package sociam.pybossa.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Config {
	public static final String PyBossahost;
	public static final String taskDir;
	public static final String projectDir;
	public static final String taskRunDir;

	public static final String api_key;
	public static final String project_validation_templatePath;

	public static final String mongoHost;
	public static final int mongoPort;
	public static final String binsDatabaseName;
	public static final String projectsDatabaseName;
	public static final String taskCollection;
	public static final String taskRunCollection;
	public static final String projectCollection;

	public static final String TaskCreatorTrigger;
	public static final String TaskCollectorTrigger;
	public static final String ProjectCreatorTrigger;
	public static final String TaskPerformerTrigger;
	public static final String TaskPerformerPushRate;

	public static final String RePushTaskToTwitter;

	public static final String TasksPerProject;

	static {
		Properties p = new Properties();
		try (FileInputStream stream = new FileInputStream(new File(
				"config.properties"))) {
			p.load(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		PyBossahost = p.getProperty("PyBossahost");
		taskDir = p.getProperty("taskDir");
		taskRunDir = p.getProperty("taskRunDir");
		projectDir = p.getProperty("projectDir");
		api_key = p.getProperty("api_key");
		project_validation_templatePath = p
				.getProperty("project_validation_templatePath");

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

	}

}
