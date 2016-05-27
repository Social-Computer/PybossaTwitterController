package recoin.mongodb_version.test;

public class TestRemoveHashTagsAndLinks {

	
	public static void main(String[] args) {
		
		String text = "@sdfsdf Fordere die Politik auf den #GlobalFund im Kampf gg. vermeidbare Krankheiten zu unterstützen https://t.co/bZ8dCa4DQv https://t.co/SB5ooSd6BX";
		System.out.println(removeHashtagsAndLinks(text));
	}
	
	public static String removeHashtagsAndLinks(String text) {
		text = text.replaceAll("#\\w+", "");
		text = text.replaceAll("(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?", "");
		text = text.replaceAll("@[a-zA-Z_]", "");
		
		return text;
	}
}
