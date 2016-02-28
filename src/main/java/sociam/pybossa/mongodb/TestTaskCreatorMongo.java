package sociam.pybossa.mongodb;

import java.util.Date;

import sociam.pybossa.TaskCreator;

public class TestTaskCreatorMongo {

	public static void main(String[] args) {

		Date date = new Date();
		String publishedAt = TaskCreator.MongoDBformatter.format(date);

//		 TaskCreator.pushTaskToMongoDB(22, publishedAt, 2, "ready",
//		 "\u0441\u043c\u0430\u0440\u0442\u0444\u043e\u043d\u043e\u043c
//		 \u0438\u0437 \u043b\u0438\u043d\u0435\u0439\u043a\u0438 90");

	}

	
}
