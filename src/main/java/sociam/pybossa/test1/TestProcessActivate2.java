package sociam.pybossa.test1;

import org.apache.log4j.PropertyConfigurator;

import recoin.mongodb_version.Activate;
import sociam.pybossa.methods.TwitterMethods;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.Status;
import twitter4j.Twitter;

public class TestProcessActivate2 {

	public static void main(String[] args) {

		// String text = "PODIUM JUMP!! Thank you Sochi! #RussianGP #F1 #TeamNR6
		// @MercedesAMGF1";
		// extractHashtags(text);
		PropertyConfigurator.configure("log4j.properties");
		Twitter twitter = TwitterAccount.setTwitterAccount(2);
		Status status = TwitterMethods.getTweetStausByID("727463429137567744", twitter);
		Boolean result = Activate.processACTIVATE(status, "saud");
		System.out.println(result);

	}

}
