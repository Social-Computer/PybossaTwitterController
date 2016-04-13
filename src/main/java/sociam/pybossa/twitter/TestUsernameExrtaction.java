package sociam.pybossa.twitter;

import java.util.HashSet;

import recoin.mongodb_version.InstructionSetPorcessor;

public class TestUsernameExrtaction {

	public static void main(String[] args) {
		String text = "@aadf @saud sdfsdf dfgdf g @Ramine";
		System.out.println("he");
		HashSet<String> users = InstructionSetPorcessor.extractUserToBeSharedWith(text);
		for (String string : users) {
			System.out.println("user " + string);
		}
		
	}

}
