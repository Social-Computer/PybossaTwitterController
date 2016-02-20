package sociam.pybossa.test1;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test1 {
	final static SimpleDateFormat MongoDBformatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) {
		Date date = new Date();
		String publishedAt = MongoDBformatter.format(date);
		System.out.println(publishedAt);

	}

}
