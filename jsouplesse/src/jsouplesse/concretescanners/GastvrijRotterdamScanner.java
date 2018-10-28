package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;
import jsouplesse.scraping.AbstractScanner;
import jsouplesse.scraping.WebSiteRequestConscience;
import jsouplesse.util.WebStringUtils;

public class GastvrijRotterdamScanner extends AbstractScanner {

	public GastvrijRotterdamScanner(WebSiteRequestConscience timer, WebSite webSite, WebPage webPage) {
		super(timer, webSite, webPage);
	}

	@Override
	protected boolean scanHtml(Document html) {
		
		// Select all links with class 'exit'.
		Elements links = html.getElementsByClass("exit");
		
		for (Element link : links) {
			
			String referenceLink = link.attr("href");
			// Links on this page refer to the actual company page via this pattern:
			// "https://www.gastvrij-rotterdam.nl/exit/https://2food.nl/". To determine the
			// original link, remove everything up to and including "exit/"
			String directLink = referenceLink.substring(referenceLink.indexOf("exit/") + 5);
			
			String companyName = WebStringUtils.determineWebSiteNameFromUrl(directLink);
			Company company = new Company(companyName);
			company.setHomePageUrl(directLink);
			companies.add(company);
			// TODO: remove after test!!!
			if (companyName.matches("adbibendum"))
				break;

			// TODO: fix regex for urls
//			System.out.println("Found url '" + directLink + "' does not appear to be an actual web site.");
//			failBuilder.buildFailedScan(webPage, FailedScan.Reason.BAD_RESULT);
			
		}
		
		return true;
	}

	@Override
	protected boolean scanRawHtml(String rawHtml) {
		System.out.println("Only raw html could be acquired!");
		return false;
	}
}
