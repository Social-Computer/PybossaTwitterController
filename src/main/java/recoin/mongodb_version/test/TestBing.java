package recoin.mongodb_version.test;

import org.apache.log4j.Logger;
import net.billylieurance.azuresearch.AzureSearchResultSet;
import net.billylieurance.azuresearch.AzureSearchWebQuery;
import net.billylieurance.azuresearch.AzureSearchWebResult;
import sociam.pybossa.config.Config;

public class TestBing {
	final static Logger logger = Logger.getLogger(TestBing.class);

	public static void main(String[] args) {

		AzureSearchWebQuery aq = new AzureSearchWebQuery();
		aq.setAppid(Config.bingKey);
		aq.setQuery("Findings frm & Emory Univ. study to help focus prevention for gay/bi-sexual men of color");
		aq.doQuery();
		AzureSearchResultSet<AzureSearchWebResult> ars = aq.getQueryResult();
		for (AzureSearchWebResult anr : ars) {
			System.out.println(anr.getTitle());
			System.out.println(anr.getUrl());
		}
	}

	public static AzureSearchResultSet<AzureSearchWebResult> getEnriches(String query) {
		AzureSearchWebQuery aq = new AzureSearchWebQuery();
		aq.setAppid(Config.bingKey);
		aq.setQuery(query);
		aq.doQuery();
		AzureSearchResultSet<AzureSearchWebResult> ars = aq.getQueryResult();
		return ars;
	}

}
