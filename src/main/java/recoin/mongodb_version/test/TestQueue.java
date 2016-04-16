package recoin.mongodb_version.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import twitter4j.Query;

public class TestQueue {
	public static void main(String[] args) {
		ArrayList<Integer> source = new ArrayList<>();
		ArrayList<Integer> upVoted = new ArrayList<>();
		ArrayList<Integer> normalVoted = new ArrayList<>();
		ArrayList<Integer> downVoted = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			source.add(i);
		}

		for (Integer num : source) {

			if (num == 7) {
				normalVoted.add(num);
			} else if ((num & 1) == 0) {
				upVoted.add(num);
			} else {
				downVoted.add(num);
			}
		}

		Queue<Integer> queue = new LinkedList<Integer>();

		for (Integer num : upVoted) {
			queue.add(num);
		}

		for (Integer num : normalVoted) {
			queue.add(num);
		}

		for (Integer num : downVoted) {
			queue.add(num);
		}

		for (Integer integer : queue) {
			System.out.println("Item " + integer);
		}

	}
}
