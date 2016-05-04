package recoin.mongodb_version.test;

import java.util.ArrayList;
import java.util.Queue;

import org.bson.Document;

import recoin.mongodb_version.TwitterTaskPerformer;
import sociam.pybossa.methods.MongodbMethods;

public class TestQueue2 {

	public static void main(String[] args) {

		int seed = 200;
		ArrayList<Document> tasksToBePushed = MongodbMethods.getIncompletedTasksFromMongoDB("twitter_task_status");
		Queue<Document> queue = TwitterTaskPerformer.stackQueue(tasksToBePushed, seed, "twitter_task_status");
		System.out.println("List: " + queue.size());
		for (Document document : queue) {
			Integer task_id = document.getInteger("task_id");
			Integer priority = document.getInteger("priority");
			System.out.println(priority + "|" + task_id + "|"+ document.getString("twitter_task_status"));
		}
	}
}
