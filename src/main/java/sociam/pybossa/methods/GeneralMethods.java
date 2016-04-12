package sociam.pybossa.methods;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import sociam.pybossa.config.Config;

public class GeneralMethods {
	
	final static Logger logger = Logger.getLogger(GeneralMethods.class);
	
	public static Boolean rePush(Date lastPushAt) {
		try {

			Calendar cal = Calendar.getInstance();
			Date currentDate = new Date();
			cal.setTime(lastPushAt);
			cal.add(Calendar.HOUR, Integer.valueOf(Config.RePushTaskToTwitter));
			Date convertedDate = cal.getTime();
			return currentDate.before(convertedDate);

		} catch (Exception e) {
			logger.error("Error ", e);
			return false;
		}
	}

}
