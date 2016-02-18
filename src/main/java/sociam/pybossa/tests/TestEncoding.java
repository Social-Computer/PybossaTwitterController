package sociam.pybossa.tests;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TestEncoding {

	public static void main(String[] args) throws UnsupportedEncodingException {
		String stringA = "https://t.co/2QZoMaZVtp #news OMS lanza llamado para recaudar 56 millones de dÃ³lares en lucha contra zika - GlobovisiÃ³n";
		String stringB = "https://t.co/2QZoMaZVtp #news OMS lanza llamado para recaudar 56 millones de dólares en lucha contra zika - Globovisión";
		String c = "caudar 56 millones de d&#195;&#179;lares en lucha contra zika - Globovisi&#195;&#179;n";
		byte[] bytes = stringA.getBytes();
		byte[] cp1251encodedBytes = stringA.getBytes(Charset.forName("windows-1251"));
		String text = new String(cp1251encodedBytes, "UTF-8");
		String result = new String(stringA.getBytes("windows-1251"));
		System.out.println("c " + c);
		if (text.equals(stringB)) {
			System.out.println("equal");
		} else {
			
			System.out.println("stringB " + stringB);
			System.out.println("text " + text);
			System.out.println("not equal");
		}
	}
}
