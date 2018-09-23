package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.AbstractDetailPageScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesseutil.WebStringUtils;

/**
 * Scans a detail page of EcoGoodies.com. Really boring detail page scanner,
 * since the site clearly links to the web shop by means of a button that
 * always has the text 'Bezoek deze webshop'. This link is used to construct a 
 * WebShopHomePageScanner which is used to scan the home page of the web shop.
 */
public class EcoGoodiesDetailPageScanner extends AbstractDetailPageScanner {

	/**
	 * The scanner used to extract company data from the web shop home page.
	 */
	private WebShopHomePageScanner webShopHomePageScanner;
	
	public EcoGoodiesDetailPageScanner(SqlHelper sqlHelper, WebPage detailPage) {
		super(sqlHelper, detailPage);
	}

	@Override
	protected void scanRawHtml(String rawHtml) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void scanHtml(Document html) {
		
		// NB: the <a> on this page also spans an image, so test to make sure
		// the check for link.text() works
		
		// Select all links.
		Elements links = html.select("a");
		
		for (Element link : links) {
			System.out.println(link.text());
			// Check whether the text of the links is 'Bezoek deze webshop'.
			if (link.text().matches("Bezoek deze webshop")) {
			// This is the link to the web site of the web shop. Extract the URL.
				String href = link.attr("href");
				// TODO: finish WebShopHomePageScanner and use it here.
//				WebPage webShopHomePage = new WebPage(connector, href);
				
				company = new Company(WebStringUtils.determineWebSiteNameFromUrl(href));
				company.setHomePageUrl(href);
			}
		}
	}	
}
