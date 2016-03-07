package sociam.pybossa.facetime.tests;

import facebook4j.Account;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.ResponseList;

public class Test1 {

	
	public static void main(String[] args) throws FacebookException {
		Facebook facebook = new FacebookFactory().getInstance();
		
		facebook.postStatusMessage("Hello World from Facebook4J.");
	}
}
