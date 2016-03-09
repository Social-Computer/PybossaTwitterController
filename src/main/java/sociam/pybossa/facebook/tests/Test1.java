package sociam.pybossa.facebook.tests;

import java.io.File;
import java.util.ArrayList;

import facebook4j.Account;
import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Media;
import facebook4j.PagableList;
import facebook4j.Page;
import facebook4j.PhotoUpdate;
import facebook4j.Post;
import facebook4j.PostUpdate;
import facebook4j.Reading;
import facebook4j.ResponseList;
import sociam.pybossa.util.FacebookAccount;

public class Test1 {

	public static void main(String[] args) throws FacebookException {
		Facebook facebook = FacebookAccount.setFacebookAccount(1);

		ArrayList<Post> posts = getTopPosts();
		for (Post post : posts) {
			System.out.println(post.getMessage());
			PagableList<Comment> comments = post.getComments();
			for (Comment comment : comments) {
				System.out.println(comment.getFrom().getName());
				System.out.println(comment.getMessage());
			}

		}

		// PostUpdate post = new PostUpdate("new post");
		// System.out.println(facebook.postFeed(post));

		// Post onePost = facebook.getPost("964602923577144_965366313500805",
		// new Reading().fields("comments,name"));
		// PagableList<Comment> comments = onePost.getComments();
		// for (Comment comment : comments) {
		// System.out.println(comment.getFrom().getName());
		// System.out.println(comment.getMessage());
		// }

		// ResponseList<Post> feeds = facebook.getFeed("964602923577144",
		// new Reading().limit(25).fields("comments"));
		//
		// for (Post post : feeds) {
		// if (post != null) {
		// PagableList<Comment> comments = post.getComments();
		// for (Comment comment : comments) {
		// System.out.println(comment.getMessage());
		// }
		// }
		// }

		// PostUpdate post = new PostUpdate("from app too page");
		// System.out.println(facebook.postFeed(post));

		// String id;
		// Media media = new Media(new File(
		// "/Users/user/Eclipse/PybossaTwitterController/textToImage.jpg"));
		// PhotoUpdate photoUpdate = new PhotoUpdate(media);
		// photoUpdate.message("new onw");
		// id = facebook.postPhoto(photoUpdate);
		// System.out.println(id);

	}

	public static ArrayList<Comment> getPostComments(String id) {

		ArrayList<Comment> validComments = new ArrayList<Comment>();
		try {
			Facebook facebook = FacebookAccount.setFacebookAccount(1);
			Post onePost = facebook.getPost(id,
					new Reading().fields("comments,name"));
			PagableList<Comment> comments = onePost.getComments();
			for (Comment comment : comments) {
				validComments.add(comment);
			}
			if (!validComments.isEmpty()) {
				return validComments;
			} else {
				return null;
			}
		} catch (Exception e) {

			return null;
		}

	}

	public static ArrayList<Post> getTopPosts() {

		ArrayList<Post> validposts = new ArrayList<Post>();
		try {
			Facebook facebook = FacebookAccount.setFacebookAccount(1);
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

		} catch (Exception e) {

			return null;
		}

	}
}
