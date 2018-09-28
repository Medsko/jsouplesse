package jsouplesse.scraping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jsoup.HttpStatusException;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.util.CrappyLogger;
import jsouplesse.util.WebStringUtils;

/**
 * Builds new {@WebPage} objects and fetches their HTML document based
 * on an input URL.
 * 
 * If a request has been made to the same web site too short a while ago,
 * the process is paused until a reasonable amount of time has passed.
 */
public class WebPageFetcher {

	// Processing.
	/** Used to log errors. */
	private CrappyLogger logger;
	
	private SqlHelper sqlHelper;
	
	/** Helper that creates and sends a human-like request and returns the response. */
	private PlausibleRequestHelper hermes = new PlausibleRequestHelper();
	
	/** Holds {@link RequestTimer}s for every web site that requests are sent to. */
	private Map<String, RequestTimer> requestTimers;

	/** The URL of the page the search started on. */
	private String grandParentUrl;
	
	/** 
	 * Flag signifying if the scanner should proceed to scanning the next page on the web site,
	 * or if further requests are pointless. Is only set to false when status is 403 (forbidden).
	 */
	private boolean shouldResumeScanning = true;
	
	private String currentParentUrl;
	
	// Output.
	/** The web page being retrieved. */
	private WebPage webPage;

	public WebPageFetcher(CrappyLogger logger, SqlHelper sqlHelper, String grandParentUrl) {
		this.logger = logger;
		this.sqlHelper = sqlHelper;
		this.grandParentUrl = grandParentUrl;
		currentParentUrl = grandParentUrl;
		requestTimers = new HashMap<>();
	}
	
	/**
	 * Initializes the given web page, by sending a request and parsing the response.
	 * If no connection could be created, or no response retrieved, or the contents could
	 * not be parsed, null is returned instead.
	 * @return flag indicating success.
	 */
	public boolean fetch(String pageUrl) {
				
		if (pageUrl == null || pageUrl.isEmpty())
			// If no URL is provided, no web page can be constructed.
			return false;
		
		// Resolve the pageUrl against its parent, if it is relative.
		String fullPageUrl = WebStringUtils.resolveAgainstParent(pageUrl, currentParentUrl);

		webPage = new WebPage(sqlHelper, fullPageUrl);
		webPage.setWebPageTypeId(WebPage.TYPE_OTHER);

		waitUntilTimeIsRight(fullPageUrl);
		
		Response response = null;
		Document pageContents = null;
		
		try {
			// Use the helper to construct a human-like request.
			response = hermes.sendRequest(fullPageUrl);
			
			if (hermes.getCanParseResponse())
				// Parse the results and set the resulting HTML document on the list page.
				pageContents = response.parse();
			
		} catch (HttpStatusException hsex) {
			// The request returned a HTTP error.
			if (hsex.getStatusCode() == 403) {
				// If we've been blocked, further scanning is pointless...for now.
				shouldResumeScanning = false;
				logger.log("WebPageInitializer got a 'Forbidden' code response!");
				return false;
			}
			
		} catch (IOException ioex) {
			logger.log("WebPageFetcher.fetch() - I/O exception during request.");
			logger.log(ioex.getMessage());
		}
		// Set the contents on the web page.
		webPage.setPageContents(pageContents);
		
		// Update the currentParentUrl if necessary.
		// TODO: figure out a way to reset to the grandParentUrl when appropriate.
//		updateCurrentParentUrl(fullPageUrl, response.url().toString());
		
		// Try to extract an external link.
		Optional<String> externalLink = WebStringUtils.extractExternalLink(fullPageUrl, currentParentUrl);
		
		if (externalLink.isPresent())
			// An external link was found. Set it as the new currentParentUrl.
			currentParentUrl = externalLink.get();	
		
		if (webPage.haveContentsBeenRetrieved()) {
			logger.log("WebPageFetcher has successfully retrieved the contents for "
					+ "page " + pageUrl);
			return true;
		} else {
			logger.log("WebPageFetcher failed to retrieve contents for "
					+ "page " + pageUrl);
			return false;
		}
	}
	
	private void updateCurrentParentUrl(String fullPageUrl, String resultUrl) {
		// Try to extract an external link.
		Optional<String> externalLink = WebStringUtils.extractExternalLink(fullPageUrl, currentParentUrl);
		
		if (externalLink.isPresent())
			// An external link was found. Set it as the new currentParentUrl.
			currentParentUrl = externalLink.get();
		
		if (!resultUrl.contains(WebStringUtils.determineBaseUrl(fullPageUrl))) {
			// The URL of the response was from an external web site.
			// Set the base URL of the new web site as the currentParentUrl.
			currentParentUrl = WebStringUtils.determineBaseUrl(resultUrl);
		}
	}
	
	private void waitUntilTimeIsRight(String pageUrl) {
		// Check whether a timer has already been added to the map for this web site.
		String webSiteName = WebStringUtils.determineWebSiteNameFromUrl(pageUrl);
		RequestTimer timer = requestTimers.get(webSiteName);
		
		if (timer == null) {
			// Construct a new timer for this web site and add it to the map.
			timer = new RequestTimer(logger, webSiteName);
			requestTimers.put(webSiteName, timer);
		}
		
		try {
			// Wait until the time is right.
			while (!timer.isRightTime()) {
				long randomInterval = timer.getRandomInterval();
				logger.log("Waiting for " + randomInterval + " milliseconds before "
						+ "sending next request to " + webSiteName + ".");
				Thread.sleep(randomInterval);
			}
		} catch (InterruptedException iex) {
			// Should never happen while single threaded.
		}
	}
	
	public WebPage getWebPage() {
		return webPage;
	}

	public String getGrandParentUrl() {
		return grandParentUrl;
	}

	public void setGrandParentUrl(String grandParentUrl) {
		this.grandParentUrl = grandParentUrl;
	}

	public boolean shouldResumeScanning() {
		return shouldResumeScanning;
	}

	public void setCurrentParentUrl(String currentParentUrl) {
		this.currentParentUrl = currentParentUrl;
	}
}
