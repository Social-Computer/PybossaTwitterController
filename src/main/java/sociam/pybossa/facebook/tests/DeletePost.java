package sociam.pybossa.facebook.tests;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.Post;
import sociam.pybossa.config.Config;
import sociam.pybossa.methods.FacebookMethods;
import sociam.pybossa.util.FacebookAccount;

public class DeletePost {

	final static Logger logger = Logger.getLogger(DeletePost.class);

	public static void main(String[] args) throws FacebookException,
			InterruptedException {
		Facebook facebook = FacebookAccount.setFacebookAccount(1);
		Config.reload();
		PropertyConfigurator.configure("log4j.properties");
		while (true) {
			ArrayList<Post> posts = FacebookMethods
					.getLatestPostsEvenWithoutComments(facebook);
			logger.debug("post size " + posts.size());
			for (Post post : posts) {
				try {
					logger.debug(facebook.deletePost(post.getId()));
				} catch (Exception e) {
					logger.error("Error", e);
				}
			}
			logger.debug("sleeping for one min");
			Thread.sleep(60000);
		}

	}
}
