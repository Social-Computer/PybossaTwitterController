package recoin.mongodb_version.test;

public class TestPushRate {

	public static void main(String[] args) {

		setTaskPushRate();
	}

	public static void setTaskPushRate() {
		double lowerLimit = 120;
		double upperLimit = 2;
		double taskNumber = 100;
		double taskRunNumber = 100;
		double result = 2;
		for (int i = 0; i < 10; i++) {
			result = ((taskRunNumber - i) * taskNumber) / taskNumber;
			// taskRunNumber+=2;
			System.out.println(result);
		}

	}
}
