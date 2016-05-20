package recoin.mongodb_version.test;

public class TestPushRate {

	public static void main(String[] args) {
		float lowestSpeed = 20;
		float topSpeed = 2;
		float taskRunNumber = 0;
		for (int i = 1; i <= 20; i++) {
			if (i == 17) {
				taskRunNumber = 5;
			}
			System.out.println(getPusingTime(topSpeed, lowestSpeed, 5, i,
					taskRunNumber));
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
