package recoin.mongodb_version.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.bson.Document;

import sociam.pybossa.methods.MongodbMethods;

public class TestQueue3 {

	public static void main(String[] args) {

		ArrayList<Document> tasksToBePushed = MongodbMethods.getIncompletedTasksFromMongoDB("twitter_task_status");
		System.out.println("List1: " + tasksToBePushed.size());
		tasksToBePushed = getQueue(tasksToBePushed);
		Queue<Document> queue = new LinkedList<Document>(tasksToBePushed);
		for (Document document : queue) {
			Integer task_id = document.getInteger("task_id");
			Integer priority = document.getInteger("priority");
			System.out.println(priority + "|" + task_id + "|"+ document.getString("twitter_task_status") + "|" + document.getString("task_text").length());
		}
	}

	
	public static ArrayList<Document> getQueue(ArrayList<Document> tasksToBePushed) {
		
		
		Collections.sort(tasksToBePushed, new Comparator<Document>() {  
		    @Override  
		    public int compare(Document p1, Document p2) {  
		        return new CompareToBuilder().append(p2.getInteger("priority"), p1.getInteger("priority")).append(p2.getString("twitter_task_status"),p1.getString("twitter_task_status")).append(p2.getString("task_text").length(), p1.getString("task_text").length()).toComparison();  
		    }  
		}); 
		return tasksToBePushed;
	}
}
