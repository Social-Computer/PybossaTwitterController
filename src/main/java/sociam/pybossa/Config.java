package sociam.pybossa;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Config {
	public static final String PyBossahost;
	public static final String taskDir;
	public static final String projectDir;
	public static final String api_key;
	public static final String project_validation_templetePath;
	
	public static final String mongoHost;
	public static final int mongoPort;
	public static final String databaseName;
	public static final String taskCollection;

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
		project_validation_templetePath = p.getProperty("project_validation_templetePath");
		
		mongoHost = p.getProperty("mongoHost");
		mongoPort = Integer.valueOf(p.getProperty("mongoPort"));
		databaseName = p.getProperty("databaseName");
		taskCollection = p.getProperty("taskCollection");

	}

}
