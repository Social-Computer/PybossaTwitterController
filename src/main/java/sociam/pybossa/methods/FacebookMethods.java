package sociam.pybossa.methods;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import sociam.pybossa.config.Config;
import sociam.pybossa.util.FacebookAccount;
import sociam.pybossa.util.StringToImage;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.Media;
import facebook4j.PhotoUpdate;
import facebook4j.Post;
import facebook4j.PostUpdate;
import facebook4j.Reading;
import facebook4j.ResponseList;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class FacebookMethods {

	final static Logger logger = Logger.getLogger(FacebookMethods.class);

	public static String sendTaskToFacebook(String taskContent, String media_url, String taskTag,
			ArrayList<String> hashtags, int project_type) {
		String facebook_task_id;
		try {
			Facebook facebook = FacebookAccount.setFacebookAccount(project_type);

			// defualt
			String question = "";
			if (project_type == 1) {
				question = Config.project_validation_question;
			}

			String post = question;
			for (String string : hashtags) {
				if (post.length() == 0) {
					post = string;
				} else {
					String tmpResult = post + " " + string + taskTag;
					if (tmpResult.length() >= 140) {
						break;
					}
					post = post + " " + string;
				}
			}
			String tag = taskTag.replaceAll("#t", "");
			post = post + " " + taskTag + " " + "you can also monitor this task " + Config.domainURI + tag;

			// convert taskContent and question into an image
			File image = null;
			if (!media_url.equals("")) {
				image = StringToImage.combineTextWithImage(taskContent, media_url);
			} else {
				image = StringToImage.convertStringToImage(taskContent);
			}

			// image must exist
			if (image != null) {
				// status = facebook.updateStatus(post);

				Media media = new Media(image);
				PhotoUpdate photoUpdate = new PhotoUpdate(media);
				photoUpdate.message(post);
				facebook_task_id = facebook.postPhoto(photoUpdate);

				logger.debug("Successfully posting a task ");
				return facebook_task_id;
			} else {
				logger.error("Image couldn't br generated");
				return null;
			}
		} catch (FacebookException e) {
			logger.error("Error", e);
			if (e.getErrorCode() == 190){
				System.exit(1);
			}
			return null;
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}
	}

	public static String sendTaskToFacebookWithUrl(String taskTag, ArrayList<String> hashtags, int project_type,
			String url) {
		String facebook_task_id;
		try {
			Facebook facebook = FacebookAccount.setFacebookAccount(project_type);

			// defualt
			String question = "";
			if (project_type == 1) {
				question = Config.project_validation_question;
			}

			String post = question;
			for (String string : hashtags) {
				if (post.length() == 0) {
					post = string;
				} else {
					String tmpResult = post + " " + string + taskTag;
					if (tmpResult.length() >= 140) {
						break;
					}
					post = post + " " + string;
				}
			}
			String tag = taskTag.replaceAll("#t", "");
			post = post + " " + taskTag + " " + "you can also monitor this task " + Config.domainURI + tag;

			// status = facebook.updateStatus(post);

			PostUpdate facebookPost = new PostUpdate(new URL(url)).message(post);
			facebook_task_id = facebook.postFeed(facebookPost);
			logger.debug("Successfully posting a task ");
			return facebook_task_id;

		} catch (FacebookException e) {
			logger.error("Error", e);
			if (e.getErrorCode() == 190) {
				System.exit(1);
			}
			return null;
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}
	}

	public static Post getPostByID(String post_id, Facebook facebook) {

		try {
			facebook = FacebookAccount.setFacebookAccount(1);
			Post onePost = facebook.getPost(post_id, new Reading().fields("comments,message,name"));
			return onePost;
		} catch (FacebookException e) {
			logger.error("Error", e);
			if (e.getErrorCode() == 190) {
				System.exit(1);
			}
			return null;
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}
	}

	public static ArrayList<Post> getLatestPosts(Facebook facebook) {

		ArrayList<Post> validposts = new ArrayList<Post>();
		try {
			ResponseList<Post> feeds = facebook.getFeed("964602923577144",
					new Reading().limit(100).fields("comments,message,name"));
			for (Post post : feeds) {
				if (post.getComments().size() > 0) {
					validposts.add(post);
				}
			}
			if (!validposts.isEmpty()) {
				return validposts;
			} else {
				return null;
			}
		} catch (FacebookException e) {
			logger.error("Error", e);
			if (e.getErrorCode() == 190) {
				System.exit(1);
			}
			return null;
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}

	}

	public static ArrayList<Post> getLatestPostsEvenWithoutComments(Facebook facebook) {

		ArrayList<Post> validposts = new ArrayList<Post>();
		try {
			ResponseList<Post> feeds = facebook.getFeed("964602923577144", new Reading().limit(100));
			for (Post post : feeds) {
				validposts.add(post);

			}
			return validposts;

		} catch (FacebookException e) {
			logger.error("Error", e);
			if (e.getErrorCode() == 190) {
				System.exit(1);
			}
			return null;
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}

	}
}
