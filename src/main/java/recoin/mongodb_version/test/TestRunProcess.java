package recoin.mongodb_version.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestRunProcess {

	public static void main(String[] args) throws IOException, InterruptedException {
		StringBuffer output = new StringBuffer();
		Process process = Runtime.getRuntime()
				.exec(new String[] { "/bin/sh", "-c", "/usr/local/bin/mongodump --db sdfsdf  --out ~/ddd" });
		process.waitFor();
		int exitCode = process.exitValue();
		BufferedReader reader = null;
		if (exitCode == 0) {
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		} else {
			reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		}

		String line = "";
		while ((line = reader.readLine()) != null) {
			output.append(line + "\n");
		}
		System.out.println(output);
		System.out.println(exitCode);
	}
}
