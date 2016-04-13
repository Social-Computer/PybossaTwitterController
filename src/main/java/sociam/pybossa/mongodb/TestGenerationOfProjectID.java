package sociam.pybossa.mongodb;

import sociam.pybossa.methods.MongodbMethods;

public class TestGenerationOfProjectID {

	public static void main(String[] args) {
		MongodbMethods.updateProjectsByAddingCounters();
	}

}
