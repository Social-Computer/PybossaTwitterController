package sociam.pybossa.test1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestDataPushPeriod {
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) {

		try {
			String oldTime = "2016-02-19 00:41:43";
			Date oldDate = MongoDBformatter.parse(oldTime);
			Calendar cal = Calendar.getInstance();
			Date currentDate = new Date();
			cal.setTime(oldDate);
			cal.add(Calendar.HOUR, 15);
			Date convertedDate = cal.getTime();
			System.out.println(currentDate.before(convertedDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
