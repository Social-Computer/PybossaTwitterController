package sociam.pybossa.test1;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class testConfig {
	public static final String name1;

	static {
		Properties p = new Properties();
		try (FileInputStream stream = new FileInputStream(new File("config.properties"))) {
			p.load(stream);
		} catch (Exception e) {
			// handle exceptions
		}

		name1 = p.getProperty("name1");

	}
}