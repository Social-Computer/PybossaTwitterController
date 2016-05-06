package recoin.mongodb_version.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

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
	
	public static Queue<Document> stackQueue(
			ArrayList<Document> tasksToBePushed, int seed, String task_status) {
		Queue<Document> queue = new LinkedList<Document>();
		ArrayList<Document> upVotedTasks = new ArrayList<>();
		ArrayList<Document> normalVotedTasks = new ArrayList<>();
		ArrayList<Document> randomisedNormalVotedTasks = new ArrayList<>();
		ArrayList<Document> downVotedTasks = new ArrayList<>();

		for (Document document : tasksToBePushed) {

			Integer priority = document.getInteger("priority");
			if (priority == null) {
				priority = 0;
			}
			if (priority > 0) {
				upVotedTasks.add(document);
			} else if (priority.equals(0)) {
				normalVotedTasks.add(document);
			} else {
				downVotedTasks.add(document);
			}
		}

		randomisedNormalVotedTasks = randomiseNormalVote(normalVotedTasks, seed);
		randomisedNormalVotedTasks = sortNormalVote(randomisedNormalVotedTasks,task_status);
		upVotedTasks = sortUpVote(upVotedTasks);
		downVotedTasks = sortDownVote(downVotedTasks);

		for (Document document : upVotedTasks) {
			queue.add(document);
		}
		for (Document document : randomisedNormalVotedTasks) {
			queue.add(document);
		}
		for (Document document : downVotedTasks) {
			queue.add(document);
		}

		return queue;
	}

	private static ArrayList<Document> randomiseNormalVote(
			ArrayList<Document> normalVotedTasks, int seed) {
		ArrayList<Document> randomisedNormalVotedTasks = new ArrayList<>();
		HashSet<Integer> taskIDs = new HashSet<Integer>();
		while (taskIDs.size() < normalVotedTasks.size()) {
			Random random = new Random(seed);
			seed++;
			Integer genertatedTaskID = random.nextInt(normalVotedTasks.size());
			if (taskIDs.contains(genertatedTaskID)) {
				continue;
			} else {
				taskIDs.add(genertatedTaskID);
			}

			randomisedNormalVotedTasks.add(normalVotedTasks
					.get(genertatedTaskID));
		}

		return randomisedNormalVotedTasks;
	}

	private static ArrayList<Document> sortNormalVote(
			ArrayList<Document> randomisedNormalVotedTasks, String task_status) {
		if (task_status.equals("twitter_task_status")) {
			Collections.sort(randomisedNormalVotedTasks,
					new Comparator<Document>() {
						@Override
						public int compare(Document doc1, Document doc2) {

							return doc2
									.getString("twitter_task_status")
									.compareTo(
											doc1.getString("twitter_task_status"));
						}
					});

		} else if (task_status.equals("facebook_task_status")) {
			Collections.sort(randomisedNormalVotedTasks,
					new Comparator<Document>() {
						@Override
						public int compare(Document doc1, Document doc2) {

							return doc2
									.getString("facebook_task_status")
									.compareTo(
											doc1.getString("facebook_task_status"));
						}
					});
		}
		return randomisedNormalVotedTasks;
	}

	private static ArrayList<Document> sortUpVote(
			ArrayList<Document> upVotedTasks) {
		// sort the upvotes
		Collections.sort(upVotedTasks, new Comparator<Document>() {
			@Override
			public int compare(Document doc1, Document doc2) {

				return doc2.getInteger("priority").compareTo(
						doc1.getInteger("priority"));
			}
		});

		return upVotedTasks;
	}

	private static ArrayList<Document> sortDownVote(
			ArrayList<Document> downVotedTasks) {
		// downVoted
		Collections.sort(downVotedTasks, new Comparator<Document>() {
			@Override
			public int compare(Document doc1, Document doc2) {

				return doc1.getInteger("priority").compareTo(
						doc2.getInteger("priority"));
			}
		});

		return downVotedTasks;
	}

}
