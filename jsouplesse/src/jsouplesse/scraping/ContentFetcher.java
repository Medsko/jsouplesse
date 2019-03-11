package jsouplesse.scraping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jsoup.nodes.Document;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.util.CrappyLogger;
import jsouplesse.util.WebStringUtils;

/**
 * Fetches the content located at a given URL, be it a page or an image.
 * 
 * If a request has been made to the same web site too short a while ago,
 * the process is paused until a reasonable amount of time has passed.
 */
public class ContentFetcher {
	
	// Processing.
	/** Used to log errors. */
	private CrappyLogger logger;
	
	private SqlHelper sqlHelper;
	
	/** Helper that creates and sends a human-like request and returns the response. */
	private PlausibleRequestHelper hermes;
	
	/** Holds {@link WebSiteRequestConscience}s for every web site that requests are sent to. */
	private Map<String, WebSiteRequestConscience> collectiveConscience;

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
	
	/** The image being retrieved, as a byte array. */
	private byte[] fetchedImage;

	/**
	 * Constructor that does a whole lot of initializing. 
	 */
	public ContentFetcher(CrappyLogger logger, SqlHelper sqlHelper, String grandParentUrl) {
		this.logger = logger;
		this.sqlHelper = sqlHelper;
		this.grandParentUrl = grandParentUrl;
		this.hermes = new PlausibleRequestHelper(logger);
		currentParentUrl = grandParentUrl;
		collectiveConscience = new HashMap<>();
	}
	
	/**
	 * Initializes the given web page, by sending a request and parsing the response.
	 * If no connection could be created, or no response retrieved, or the contents could
	 * not be parsed, null is returned instead.
	 * @return flag indicating success.
	 */
	public boolean fetchWebPage(String pageUrl) {
		// Resolve the pageUrl against its parent, if it is relative.
		String fullPageUrl = WebStringUtils.resolveAgainstParent(pageUrl, currentParentUrl);

		if (!performSharedChecks(fullPageUrl))
			return false;

		// Attempt to retrieve the document of the page and set them on the web page object.
		Document pageContents = getWebPageDocument(fullPageUrl);
		webPage = new WebPage(sqlHelper, fullPageUrl);
		webPage.setWebPageTypeId(WebPage.TYPE_OTHER);
		webPage.setPageContents(pageContents);
		
		if (webPage.haveContentsBeenRetrieved()) {
			logger.log("WebPageFetcher has successfully retrieved the contents for page " 
					+ pageUrl);
			return true;
		} else {
			logger.log("WebPageFetcher failed to retrieve contents for page " + pageUrl);
			return false;
		}
	}
	
	/**
	 * Performs the standard checks that should be executed before making a request.
	 * @return {@code boolean} indicating whether checks succeeded. 
	 */
	private boolean performSharedChecks(String url) {
		if (url == null || url.isEmpty()) {
			// If no URL is provided, no web page can be constructed.
			logger.log("performSharedChecks() - a null or empty URL was provided.");
			return false;
		}
		// Get or initialize the conscience for this web site.
		WebSiteRequestConscience conscience = getWebSiteRequestConscience(url);
		
		// Check whether the request is moral (according to robots.txt).
		if (!conscience.isRequestEthical(url)) {
			// Do the right thing.
			logger.log("performSharedChecks() - the requested URL is explicitly forbidden "
					+ "according to robots.txt of web site" 
					+ WebStringUtils.determineWebSiteNameFromUrl(url));
			return false;
		}
		// Our conscience is clear. Now hold off sending the next request until the time is right.
		waitUntilTimeIsRight(conscience);
		
		return true;
	}
	
	/**
	 * Attempts to fetch the image located at the provided URL.
	 * @param url - the location of the desired image.
	 * @return {@code boolean} indicating success.  
	 */
	public boolean fetchImage(String url) {
		// Resolve the URL against its parent, if it is relative.
		String fullImageUrl = WebStringUtils.resolveAgainstParent(url, currentParentUrl);
		
		if (!performSharedChecks(fullImageUrl))
			return false;
		
		if (!hermes.sendImageRequest(fullImageUrl)) {
			// The request failed.
			logger.log("getWebPageDocument - fail while executing the request for URL: "
					+ fullImageUrl);
		}
		
		fetchedImage = hermes.getResponse().bodyAsBytes();
		
		return true;
	} 
	
	/**
	 * Retrieves the HTML document at the provided URL, parses it if possible and returns it.
	 * 
	 * @return the parsed HTML document, or null if something went wrong or the document could not
	 * be parsed. 
	 */
	private Document getWebPageDocument(String fullPageUrl) {
		
		Document pageContents = null;
		try {
			if (!hermes.sendPageRequest(fullPageUrl)) {
				// The request failed.
				logger.log("getWebPageDocument - fail while executing the request for URL: "
						+ fullPageUrl);
			}
			
			if (hermes.getCanParseResponse())
				// Parse the results and set the resulting HTML document on the list page.
				pageContents = hermes.getResponse().parse();
			
		} catch (IOException ioex) {
			logger.log("WebPageFetcher.getWebPageDocument() - failed to parse as HTML document.");
			logger.log(ioex.getMessage());
		}
		
		shouldResumeScanning = hermes.getShouldResumeScanning();
		
		return pageContents;
	}
	
	// TODO: logic for this operation should be moved to and executed in ElementEvaluator (or its 
	// successor): that way, the ElementEvaluator that retrieves the web page on the new site
	// can set the new parentUrl as current for as long as its subEvaluator(s) is working on that
	// particular site, and set it back once the subEvaluator (chain) is finished.
	private void updateCurrentParentUrl(String fullPageUrl, String resultUrl) {
		// Try to extract an external link.
		Optional<String> externalLink = 
				WebStringUtils.extractExternalLink(fullPageUrl, currentParentUrl);
		
		if (externalLink.isPresent())
			// An external link was found. Set it as the new currentParentUrl.
			currentParentUrl = externalLink.get();
		
		if (!resultUrl.contains(WebStringUtils.determineBaseUrl(fullPageUrl))) {
			// The URL of the response was from an external web site.
			// Set the base URL of the new web site as the currentParentUrl.
			currentParentUrl = WebStringUtils.determineBaseUrl(resultUrl);
		}
	}
	
	/**
	 * Attempts to get the {@link WebSiteRequestConscience} for the web site that the requested 
	 * page is on from the map. If no conscience has been initialized yet, a new one is constructed,
	 * added to the map and then returned.
	 * 
	 * @param pageUrl - the URL of the page that the next request will attempt to retrieve.
	 * @return the (possibly freshly constructed) conscience for the web site the page is on. 
	 */
	private WebSiteRequestConscience getWebSiteRequestConscience(String pageUrl) {
		// Check whether a conscience has already been added to the map for this web site.
		String webSiteName = WebStringUtils.determineWebSiteNameFromUrl(pageUrl);
		WebSiteRequestConscience conscience = collectiveConscience.get(webSiteName);
		
		if (conscience == null) {
			// Construct a new timer for this web site and add it to the map.
			conscience = new WebSiteRequestConscience(logger, webSiteName);
			collectiveConscience.put(webSiteName, conscience);
			
			// Retrieve the robots.txt file from the web site.
			String robotsUrl = WebStringUtils.determineBaseUrl(pageUrl);
			robotsUrl = WebStringUtils.appendSlash(robotsUrl) + "robots.txt";
			Document robotsTxt = getWebPageDocument(robotsUrl);
			conscience.processRobotsTxt(robotsTxt);
		}

		return conscience;
	}
	 
	
	/**
	 * Uses the provided conscience to wait until enough time has passed to make the next request.
	 * @param conscience - the conscience for the web site that the next requested web page is on.
	 */
	private void waitUntilTimeIsRight(WebSiteRequestConscience conscience) {
		try {
			// Wait until the time is right.
			while (!conscience.isRightTime()) {
				long randomInterval = conscience.getRandomInterval();
				logger.log("Waiting for " + randomInterval + " milliseconds before "
						+ "sending next request to " + conscience.getWebSiteBaseUrl() + ".");
				Thread.sleep(randomInterval);
			}
		} catch (InterruptedException iex) {
			// Should never happen while single threaded.
		}
	}
	
	public WebPage getWebPage() {
		return webPage;
	}

	public byte[] getFetchedImage() {
		return fetchedImage;
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
