package jsouplesse.scraping;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import jsouplesse.FailedScanBuilder;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.FailedScan;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;

/**
 * This class can be used to scan a list page for links to detail pages, which
 * can then be scraped for company data by an {@link AbstractScraper}, or it
 * can determine the location of list pages for which it can employ other
 * scanners to determine detail pages to scan. 
 */
public abstract class AbstractScanner implements Runnable {

	protected WebSiteRequestConscience timer;
	
	protected WebSite webSite;
	
	protected WebPage webPage;
	
	protected FailedScanBuilder failBuilder;
	
	/**
	 * The list of company data that was extracted by this scanner or its child
	 * scanners and their scrapers. 
	 */
	protected List<Company> companies = new ArrayList<>();
	
	/**
	 * Creates a new AbstractScanner with the given database connection and
	 * request timer. If the scanner will work on a web site that is already
	 * being scanned, pass the same {@link WebSiteRequestConscience} that other scanners
	 * for said web site have. If it is the first scanner, create a new timer. 
	 * 
	 * @param timer - the timer that determines when to make the next request.
	 * @param webSite - the data object representing the web site.
	 * @param webPage - the web page that this scanner will work on.
	 */
	public AbstractScanner(WebSiteRequestConscience timer, WebSite webSite, WebPage webPage) {
		this.timer = timer;
		this.webSite = webSite;
		this.webPage = webPage;
		failBuilder = new FailedScanBuilder(webSite.getSqlHelper());
	}
	
	// Child scanners and RequestHelper should not be instance, but local variables.
	
	public void run() {
		// Initialize the web page.
		WebPageInitializer initializer = new WebPageInitializer(failBuilder);
		if (!initializer.initializeWebPage(webPage)) {
			System.out.println("Failed to load the web page: " + webPage.getPageUrl());
			return;
		}
		
		if (webPage.getPageContents() != null
				&& !scanHtml(webPage.getPageContents())) {
			System.out.println("Failed to completely scan the web page: " + webPage.getPageUrl());
			
		} else if (webPage.getRawPageContents() != null
				&& !scanRawHtml(webPage.getRawPageContents())) {
			System.out.println("Failed to scan the raw HTML of web page: " + webPage.getPageUrl());
		} else {
			failBuilder.buildFailedScan(webPage, FailedScan.Reason.CONTENT_NOT_RETRIEVED);
		}
	}
		
	// TODO: to allow for more informative (success) logging, these next two methods should be made
	// TODO: concrete, with some logging logic built around new abstract methods that subs implement (selectElements(), processElements())
	
	/**
	 * Attempts to scan the raw HTML of the web page as plain text.
	 */
	protected abstract boolean scanRawHtml(String rawHtml);
	
	/**
	 * Attempts to scan the HTML of the web page.
	 */
	protected abstract boolean scanHtml(Document html);
	
	/**
	 * Helper method that retrieves the text from the given element.
	 */
	protected String nullSafeGetText(Element element) {
		if (element != null)
			return element.text();
		else
			return null;
	}

	
	public WebSite getWebSite() {
		return webSite;
	}

	public List<Company> getCompanies() {
		return companies;
	}
}
