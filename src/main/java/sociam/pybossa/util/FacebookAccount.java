package sociam.pybossa.util;

import org.apache.log4j.Logger;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.conf.ConfigurationBuilder;
import sociam.pybossa.config.Config;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class FacebookAccount {
	// TODO: build the upper method for mapping between IDs and project type -
	// given that each project type should be related to a particular facebook
	// account.
	final static Logger logger = Logger.getLogger(FacebookAccount.class);

	/**
	 * This is an intermediate method that is supposed to get a facebook object
	 * based on a simple id mapping ( id=1 for trnaslation account).
	 * 
	 * @param i
	 *            This should be modelled somewhere else.
	 * @return facebook object of a specific account.
	 */
	public static Facebook setFacebookAccount(int i) {
		Facebook facebook = null;
		try {
			logger.debug("Setting up a facebook account with its credintials!");
			ConfigurationBuilder cb = new ConfigurationBuilder();

			cb.setJSONStoreEnabled(true);

			// validation account
			if (i == 1) {
				cb.setDebugEnabled(true).setOAuthAppId(Config.Facebook_appId)
						.setOAuthAppSecret(Config.Facebook_appSecret)
						.setOAuthAccessToken(Config.Facebook_accessToken)
						.setOAuthPermissions(Config.Facebook_permissions);

				// Transltion account
			} else if (i == 3) {
				cb.setDebugEnabled(true)
						.setOAuthAppId("*********************")
						.setOAuthAppSecret(
								"******************************************")
						.setOAuthAccessToken(
								"**************************************************")
						.setOAuthPermissions(
								"******************************************");

			} else if (i == 4) {
				cb.setDebugEnabled(true)
						.setOAuthAppId("*********************")
						.setOAuthAppSecret(
								"******************************************")
						.setOAuthAccessToken(
								"**************************************************")
						.setOAuthPermissions(
								"******************************************");

			} else {
				return null;
			}

			FacebookFactory ff = new FacebookFactory(cb.build());
			facebook = ff.getInstance();

			logger.debug("The facebook account " + facebook.getName()
					+ " is being set!");
		} catch (IllegalStateException e) {
			e.printStackTrace();
			logger.error("Error", e);
		} catch (FacebookException e) {
			logger.error("Errore", e);
		}
		return facebook;

	}
}
