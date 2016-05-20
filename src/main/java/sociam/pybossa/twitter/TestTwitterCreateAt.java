package sociam.pybossa.twitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TestTwitterCreateAt {

	final static SimpleDateFormat Twitterformatter = new SimpleDateFormat(
			"EEE MMM dd HH:mm:ss ZZZZZ yyyy");
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public static void main(String[] args) throws ParseException {
		String dateString = "Fri May 20 15:04:05 +0000 2016";
		Date date = Twitterformatter.parse(dateString);
		System.out.println(Twitterformatter.format(date));
		System.out.println(stopRetrivingTweetsAfterDate(date, 2));
		
	}
	
	public static Boolean stopRetrivingTweetsAfterDate(Date lastPushAt, int hours) {
		try {

			Calendar cal = Calendar.getInstance();
			Date currentDate = new Date();
			cal.setTime(lastPushAt);
			cal.add(Calendar.HOUR, hours);
			Date convertedDate = cal.getTime();
			return convertedDate.after(currentDate);

		} catch (Exception e) {
			return false;
		}
	}
}
