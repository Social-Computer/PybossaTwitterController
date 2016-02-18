package sociam.pybossa;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Config {
	public static final String PyBossahost;
	public static final String taskDir;
	public static final String projectDir;
	public static final String api_key;
	public static final String project_validation_templatePath;

	public static final String mongoHost;
	public static final int mongoPort;
	public static final String binsDatabaseName;
	public static final String projectsDatabaseName;
	public static final String taskCollection;
	public static final String projectCollection;

	public static final String TaskCreatorTrigger;
	public static final String ProjectCreatorTrigger;

	public static final String TasksPerProject;

	static {
		Properties p = new Properties();
		try (FileInputStream stream = new FileInputStream(new File("config.properties"))) {
			p.load(stream);
		} catch (Exception e) {
			// handle exceptions
		}

		PyBossahost = p.getProperty("PyBossahost");
		taskDir = p.getProperty("taskDir");
		projectDir = p.getProperty("projectDir");
		api_key = p.getProperty("api_key");
		project_validation_templatePath = p.getProperty("project_validation_templatePath");

		mongoHost = p.getProperty("mongoHost");
		mongoPort = Integer.valueOf(p.getProperty("mongoPort"));
		binsDatabaseName = p.getProperty("binsDatabaseName");
		projectsDatabaseName = p.getProperty("projectsDatabaseName");
		taskCollection = p.getProperty("taskCollection");
		projectCollection = p.getProperty("projectCollection");

		TaskCreatorTrigger = p.getProperty("TaskCreatorTrigger");
		ProjectCreatorTrigger = p.getProperty("ProjectCreatorTrigger");

		TasksPerProject = p.getProperty("TasksPerProject");

	}

}
