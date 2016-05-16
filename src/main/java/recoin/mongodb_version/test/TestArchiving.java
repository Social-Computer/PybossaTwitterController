package recoin.mongodb_version.test;

import org.apache.log4j.PropertyConfigurator;

public class TestArchiving {

	
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		recoin.mongodb_version.archving.Backup.removeFacebookPosts();
	}
}
