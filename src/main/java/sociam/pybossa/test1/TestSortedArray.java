package sociam.pybossa.test1;

import java.util.ArrayList;
import java.util.Collections;

public class TestSortedArray {
	
	public static void main(String[] args) {
		ArrayList<String> hashtags = new ArrayList<String>();
		hashtags.add("#news");
		hashtags.add("#rt");
		hashtags.add("#alpha");
		
		for (String string : hashtags) {
			System.out.println(string);
		}
		
		System.out.println("sorting");
		Collections.sort(hashtags);
		for (String string : hashtags) {
			System.out.println(string);
		}
		
	}

}
