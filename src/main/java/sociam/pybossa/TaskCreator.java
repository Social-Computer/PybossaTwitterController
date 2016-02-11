package sociam.pybossa;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class TaskCreator {

	final static Logger logger = Logger.getLogger(TaskCreator.class);

	public static String host = "http://recoin.cloudapp.net:5000";
	public static String projectDir = "/api/task";
	public static String api_key = "?api_key=acc193bd-6930-486e-9a21-c80005cbbfd2";

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String jsonData = "{\"info\": {\"contributors\": null, \"truncated\": false, \"text\": \"#Zika News: Stop The Zika Virus https://t.co/tYqAYlbPlc #PathogenPosse\", \"is_quote_status\": false, \"in_reply_to_status_id\": null, \"id\": 696727601113468928, \"favorite_count\": 0, \"source\": \"<a href=\\\"http://twitterfeed.com\\\" rel=\\\"nofollow\\\">twitterfeed</a>\", \"user_screen_name\": \"TheFlaviviruses\", \"retweeted\": false, \"coordinates\": null, \"entities\": {\"symbols\": [], \"user_mentions\": [], \"hashtags\": [{\"indices\": [0, 5], \"text\": \"Zika\"}, {\"indices\": [56, 70], \"text\": \"PathogenPosse\"}], \"urls\": [{\"url\": \"https://t.co/tYqAYlbPlc\", \"indices\": [32, 55], \"expanded_url\": \"http://bit.ly/1ol6A4L\", \"display_url\": \"bit.ly/1ol6A4L\"}]}, \"id_str\": \"696727601113468928\", \"in_reply_to_screen_name\": null, \"in_reply_to_user_id\": null, \"retweet_count\": 0, \"metadata\": {\"iso_language_code\": \"en\", \"result_type\": \"recent\"}, \"favorited\": false, \"user\": {\"follow_request_sent\": null, \"has_extended_profile\": false, \"profile_use_background_image\": true, \"id\": 837578882, \"verified\": false, \"profile_text_color\": \"333333\", \"profile_image_url_https\": \"https://pbs.twimg.com/profile_images/2633399565/6adf91e25871f243eee2b62e2e7a0f13_normal.jpeg\", \"profile_sidebar_fill_color\": \"DDEEF6\", \"is_translator\": false, \"geo_enabled\": false, \"entities\": {\"url\": {\"urls\": [{\"url\": \"https://t.co/PV2UXSz7IP\", \"indices\": [0, 23], \"expanded_url\": \"https://www.facebook.com/pages/The-Pathogen-Posse/206290052745233\", \"display_url\": \"facebook.com/pages/The-Path\\u2026\"}]}, \"description\": {\"urls\": []}}, \"followers_count\": 331, \"protected\": false, \"location\": \"\", \"default_profile_image\": false, \"id_str\": \"837578882\", \"lang\": \"en\", \"utc_offset\": -18000, \"statuses_count\": 8907, \"description\": \"West Nile, Yellow Fever, Dengue... If it's a virus, and it causes encephalitis, we know it. We know it well.\", \"friends_count\": 41, \"profile_link_color\": \"0084B4\", \"profile_image_url\": \"http://pbs.twimg.com/profile_images/2633399565/6adf91e25871f243eee2b62e2e7a0f13_normal.jpeg\", \"notifications\": null, \"profile_background_image_url_https\": \"https://pbs.twimg.com/profile_background_images/665409136/edaef635f0271e444226db8ed5364aef.jpeg\", \"profile_background_color\": \"C0DEED\", \"profile_background_image_url\": \"http://pbs.twimg.com/profile_background_images/665409136/edaef635f0271e444226db8ed5364aef.jpeg\", \"name\": \"Flavivirus Virus\", \"is_translation_enabled\": false, \"profile_background_tile\": true, \"favourites_count\": 0, \"screen_name\": \"TheFlaviviruses\", \"url\": \"https://t.co/PV2UXSz7IP\", \"created_at\": \"Fri Sep 21 11:25:22 +0000 2012\", \"contributors_enabled\": false, \"time_zone\": \"Eastern Time (US & Canada)\", \"profile_sidebar_border_color\": \"FFFFFF\", \"default_profile\": false, \"following\": null, \"listed_count\": 20}, \"geo\": null, \"in_reply_to_user_id_str\": null, \"possibly_sensitive\": false, \"lang\": \"en\", \"created_at\": \"Mon Feb 08 16:09:39 +0000 2016\", \"in_reply_to_status_id_str\": null, \"place\": null}, \"n_answers\": 30, \"quorum\": 0, \"calibration\": 0, \"created\": \"2016-02-08T16:06:57.205349\", \"state\": \"ongoing\", \"project_id\": 3, \"id\": 101, \"priority_0\": 0.0}";
		String url = host + projectDir + api_key;
		createProject(url, jsonData);

	}

	public static Boolean createProject(String url, String jsonData) {

		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(jsonData);
			params.setContentType("application/json");
			request.addHeader("content-type", "application/json");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			request.addHeader("Accept-Language", "en-US,en;q=0.8");
			request.setEntity(params);

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200
					|| response.getStatusLine().getStatusCode() == 204) {

				BufferedReader br = new BufferedReader(new InputStreamReader(
						(response.getEntity().getContent())));

				String output;
				logger.debug("Output from Server ...."
						+ response.getStatusLine().getStatusCode() + "\n");
				while ((output = br.readLine()) != null) {
					logger.debug(output);
				}
				return true;
			} else {
				logger.error("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
				return false;
			}
		} catch (Exception ex) {
			logger.error(ex);
			return false;
		}

	}
}
