package sociam.pybossa.twitter;

import java.io.File;

import sociam.pybossa.TwitterAccount;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

public class TestImageinTweet {

	public static void main(String[] args) {

		sendTaskToTwitter("test");
	}

	public static void sendTaskToTwitter(String taskContent) {
		Twitter twitter = TwitterAccount.setTwitterAccount(1);
		// Twitter twitter = TwitterFactory.getSingleton();

		try {
			File image = TestStringTOImage.convertToImage(
					"俄罗斯的英文翻译，免费在线翻译。俄语翻译",
					"is it possible for the below text to be translated?");

			StatusUpdate status = new StatusUpdate(taskContent);
			status.setMedia(image);
			twitter.updateStatus(status);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
