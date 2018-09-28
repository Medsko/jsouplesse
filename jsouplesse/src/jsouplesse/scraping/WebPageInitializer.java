package jsouplesse.scraping;

import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import jsouplesse.FailedScanBuilder;
import jsouplesse.dataaccess.dao.FailedScan;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.util.CrappyLogger;
import jsouplesse.util.WebStringUtils;

/**
 * Helper class that creates a new WebPage object by creating and sending a human-like
 * request and parsing the resulting HTML.
 */
public class WebPageInitializer {

	// TODO: This class should be reworked into a WebPageFetcher, which should determine whether the web page is on a web site that was queried earlier
	// TODO: and if so, directly use a RequestTimer (possibly one per web site queried) to determine whether enough time passed since the last request.
	// TODO: Hurry up with refactoring this class, as the bullshit it causes is getting ridiculous. 
	
	/** Used to log errors. */
	private CrappyLogger logger;
	
	/** The web page being retrieved. */
	private WebPage webPage;
	
	/** Helper that creates and sends a human-like request and returns the response. */
	private PlausibleRequestHelper hermes = new PlausibleRequestHelper();

	/** Used to build a report when a web page can't be scanned. */
	private FailedScanBuilder failBuilder;
	
	private String parentUrl;
	
	/** 
	 * Flag signifying if the scanner should proceed to scanning the next page on the web site,
	 * or if further requests are pointless. Is only set to false when status is 403 (forbidden).
	 */
	private boolean shouldResumeScanning = true;

	/**
	 * The web page type that will be set for all web pages passed to 
	 * {@link #initializeWebPage(WebPage)}.
	 */
	private Integer webPageTypeId;
	
	public WebPageInitializer() {}
	
	public WebPageInitializer(CrappyLogger logger) {
		this.logger = logger;
	}
	
	public WebPageInitializer(FailedScanBuilder failBuilder) {
		this.failBuilder = failBuilder;
	}
	
	/**
	 * Initializes the given web page, by sending a request and parsing the response.
	 * If no connection could be created, or no response retrieved, or the contents could
	 * not be parsed, null is returned instead.
	 * @return flag indicating success.
	 */
	public boolean initializeWebPage(WebPage webPage) {
		
		// Check if the type of web page we are initializing has been set.
		if (webPage.getWebPageTypeId() == null && webPageTypeId == null)
			throw new UnsupportedOperationException("The web page type has not been set!");
		
		String pageUrl = webPage.getPageUrl();
		
		if (webPage.getPageUrl() == null)
			// If no URL is provided, no web page can be constructed.
			return false;
		
		// Resolve the pageUrl against its parent, if it is relative.
		pageUrl = WebStringUtils.resolveAgainstParent(pageUrl, parentUrl);
		
		webPage.setPageUrl(pageUrl);
		
		Response response = null;
		Document pageContents = null;
		String rawPageContents = null;
		
		try {
			// Use the helper to construct a human-like request.
			response = hermes.sendRequest(pageUrl);
			
			if (hermes.getCanParseResponse())
				// Parse the results and set the resulting HTML document on the list page.
				pageContents = response.parse();
			else
				// The response has an unsupported format. Get the raw text content. 
				rawPageContents = response.body();
			
		} catch (HttpStatusException hsex) {
			// The request returned a HTTP error.
			if (hsex.getStatusCode() == 403)
				// If we've been blocked, further scanning is pointless...for now.
				shouldResumeScanning = false;
			
			handleError("WebPageInitializer got a 'Forbidden' code response!");
			
			handleException(FailedScan.Reason.HTTP_STATUS_403, hsex);			
			hsex.printStackTrace();
			
		} catch (IOException ioex) {
			
			handleException(null, ioex);
			ioex.printStackTrace();
		}
		// Set the contents on the web page.
		webPage.setPageContents(pageContents);
		webPage.setRawPageContents(rawPageContents);
		// If the web page type was not already set, do so now.
		if (webPage.getWebPageTypeId() == null)
			webPage.setWebPageTypeId(webPageTypeId);
		
		if (webPage.haveContentsBeenRetrieved()) {
			handleError("WebPageInitializer has successfully retrieved the contents for "
					+ "page " + pageUrl);
			return true;
		} else {
			handleError("WebPageInitializer failed to retrieve contents for "
					+ "page " + pageUrl);
			return false;
		}
	}
	
	/**
	 * Temporary (we all know that means nothing) convenience method that avoids
	 * null pointers when logging. 
	 */
	private void handleError(String message) {
		if (logger != null)
			logger.log(message);
	}
	
	/** Constructs a FailedScan using the thrown exception. */
	private void handleException(FailedScan.Reason reason, Exception exception) {
		
		if (failBuilder != null) {
			
			failBuilder.buildFailedScan(webPage, FailedScan.Reason.HTTP_STATUS_403, exception);
			
		} else {
			
			System.out.println("WebPageInitializer - (one of) the scanner(s) for web site " 
					+ WebStringUtils.determineWebSiteNameFromUrl(webPage.getPageUrl())
					+ " is not yet using WebPageInitializer(FailedScanBuilder).");
		}
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public void setWebPageTypeId(Integer webPageTypeId) {
		this.webPageTypeId = webPageTypeId;
	}

	public WebPage getWebPage() {
		return webPage;
	}

	public boolean getShouldResumeScanning() {
		return shouldResumeScanning;
	}	
}
