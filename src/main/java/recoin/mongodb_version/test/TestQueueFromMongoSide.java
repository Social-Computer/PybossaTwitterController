package recoin.mongodb_version.test;

import java.util.Queue;

import org.bson.Document;

import sociam.pybossa.methods.MongodbMethods;

public class TestQueueFromMongoSide {

	public static void main(String[] args) {

		Queue<Document> queue = MongodbMethods.getSortedQueue();
		System.out.println("List: " + queue.size());
		for (Document document : queue) {
			Integer task_id = document.getInteger("task_id");
			Integer priority = document.getInteger("priority");
			System.out.println(priority + "|" + task_id + "|"+ document.getString("twitter_task_status"));
		}
	}
}
