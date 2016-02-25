package sociam.pybossa.twitter;

import java.io.File;

import sociam.pybossa.util.StringToImage;
import sociam.pybossa.util.TwitterAccount;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

public class TestImageinTweet {

	public static void main(String[] args) {

		sendTaskToTwitter("test");
	}

	public static void sendTaskToTwitter(String taskContent) {
		Twitter twitter = TwitterAccount.setTwitterAccount(2);
		// Twitter twitter = TwitterFactory.getSingleton();

		try {
			File image = StringToImage.convertStringToImage("俄罗斯的英文翻译，免费在线翻译。俄语翻译");

			StatusUpdate status = new StatusUpdate(taskContent);
			status.setMedia(image);
			twitter.updateStatus(status);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
