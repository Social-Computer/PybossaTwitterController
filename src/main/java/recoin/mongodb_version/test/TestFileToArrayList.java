package recoin.mongodb_version.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class TestFileToArrayList {

	public static void main(String[] args) {
		try {
			List<String> lines = FileUtils.readLines(new File("testing/SimulatorDataset/SHARE.txt"), "utf-8");
			for (String string : lines) {
				System.out.println(string);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
