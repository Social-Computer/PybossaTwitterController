package sociam.pybossa.tests;

import java.io.File;

import sociam.pybossa.util.StringToImage;

public class TestMedia {
	
	public static void main(String[] args) {
		String text = "LED電球は本当に長持ちなのか…「10年もつ」に疑問の声も #ldnews https://t.co/oNKheAkIbX";
		
		File file = StringToImage.convertStringToImage(text);
				
	}

}
