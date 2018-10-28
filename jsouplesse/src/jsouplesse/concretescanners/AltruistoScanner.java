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

public class AltruistoScanner extends AbstractScanner {

	public AltruistoScanner(WebSiteRequestConscience timer, WebSite webSite, WebPage webPage) {
		super(timer, webSite, webPage);
	}

	@Override
	protected boolean scanHtml(Document html) {
		
		Elements links = html.select("a[target='_blank']");
		
		for (Element link : links) {
			String externalLink = link.attr("href");
			String companyName = WebStringUtils.determineWebSiteNameFromUrl(externalLink);
			Company company = new Company(companyName);
			company.setHomePageUrl(externalLink);
			companies.add(company);
			
			System.out.println("Successfully scraped: " + company.toString());
		}
		
		return true;
	}
	
	@Override
	protected boolean scanRawHtml(String rawHtml) {
		System.out.println("Only raw html could be retrieved!");
		return false;
	}
}
