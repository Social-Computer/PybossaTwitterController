package sociam.pybossa.methods;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import sociam.pybossa.config.Config;

/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
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

	
	public static Boolean stopRetrivingTweetsAfterDate(Date lastPushAt, int hours) {
		try {

			Calendar cal = Calendar.getInstance();
			Date currentDate = new Date();
			cal.setTime(lastPushAt);
			cal.add(Calendar.HOUR, hours);
			Date convertedDate = cal.getTime();
			return convertedDate.before(currentDate);

		} catch (Exception e) {
			return false;
		}
	}
	
	public static float getPusingTime(float topSpeed, float lowestSpeed,
			float firstLimit, float pushedTaskNumber, float taskRunNumber) {
		if (pushedTaskNumber < firstLimit) {
			return topSpeed;
		}
		float result = setTaskPushRate(pushedTaskNumber, taskRunNumber);
		float calculateSpeed = (result * (topSpeed + lowestSpeed)) / 100;
		if (calculateSpeed < topSpeed) {
			return topSpeed;
		} else if (calculateSpeed > lowestSpeed) {
			return lowestSpeed;
		}
		return calculateSpeed;
	}

	private static float setTaskPushRate(float pushedTaskNumber,
			float taskRunNumber) {
		float result = (taskRunNumber * 100) / pushedTaskNumber;
		return 100 - result;
	}
}
