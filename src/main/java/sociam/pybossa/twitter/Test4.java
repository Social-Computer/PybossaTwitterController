package sociam.pybossa.twitter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test4 {
	public static void main(String[] args) {

		String text = "SHARE http://twitter.com/Suad_Aljaloud";
		Pattern pattern = Pattern.compile("http://twitter.com/([A-Za-z0-9_]+)");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			System.out.println(("@" + matcher.group(1)));
		}

		
	}
}
