package sociam.pybossa.test1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPriority {

	public static void main(String[] args) {
		int oldPrio = 3;
		String response = "PRIO  -1";
		Pattern patter = Pattern.compile("-?[0-9]+");
		Matcher matcher = patter.matcher(response);
		Integer priority_number = null;

		if (matcher.find()) {
			priority_number = Integer.valueOf(matcher.group(0));
		}
		if (priority_number == null) {
			System.err.println("error");
			System.exit(-1);
		}
		Integer result = oldPrio + priority_number;
		System.out.println(result);

		

	}

}
