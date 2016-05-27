package recoin.mongodb_version.test;

import java.util.Random;

public class TestRandom {

	public static void main(String[] args) {
		Random random2 = new Random();
		int randomNum2 = random2.nextInt((2 - 0) + 1) + 0;
		System.out.println(randomNum2);
	}
}
