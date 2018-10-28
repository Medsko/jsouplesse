package jsouplesse.scraping;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

import org.jsoup.nodes.Document;

import jsouplesse.dataaccess.dao.WebSite;
import jsouplesse.util.CrappyLogger;
import jsouplesse.util.WebStringUtils;

/**
 * This helper class keeps track of the time passed since the last request was made.
 * For this mechanism to work, all scanners that are working on the same web site
 * should share one and the same {@link WebSiteRequestConscience} - meaning that in a multi-threaded
 * implementation, both essential methods should be synchronized.
 * 
 * Each time a scanner is initialized for a new web site, a new {@link WebSiteRequestConscience}
 * is also initialized.
 */
public class WebSiteRequestConscience {

	// TODO: rework this into a WebSiteRequestConscience:
	// 1) have WebPageFetcher check whether requested web page is on a new web site
	// 2) if so, before sending the request for the web page, retrieve the file at 
	// 	[webSiteBaseUrl]/robots.txt and process it
	// 3) save the result of 2) in this, so we can check if later requests are ethic.
	
	private CrappyLogger logger;
	
	private Instant tsNextRequest;
	
	private String webSiteBaseUrl;
	
	private long randomInterval;
	
	/** Directories that we are allowed to visit, according to robots.txt. */
	private List<String> allowedDirs;
	
	/** Directories that we are <strong>forbidden</strong> to visit, according to robots.txt. */
	private List<String> disAllowedDirs;
	
	public WebSiteRequestConscience(CrappyLogger logger, String webSiteBaseUrl) {
		this.logger = logger;
		this.webSiteBaseUrl = webSiteBaseUrl;
	}
	
	/**
	 * Determines whether the given request for a page is ethical, i.e. in accordance with the
	 * web site's robots.txt. 
	 */
	public boolean isRequestEthical(String pageUrl) {
		// Determine the path without the root.
		String urlPath = null;
		try {
			URI pageUri = new URI(pageUrl);
			urlPath = pageUri.getPath();			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		// TODO: finish this method.
//		if (disAllowedDirs.contains(o))
		
		return true;
	}
	
	/**
	 * Walks through the provided {@link Document} which was found at [webSiteBaseUrl]/robots.txt
	 * and extracts the allowed and disallowed directories from it, which are added to the lists.
	 */
	public void processRobotsTxt(Document robotsTxt) {
		// TODO: finish this method.
		// See https://www.google.nl/robots.txt for a nice inclusive example.
	}
	
	/**
	 * Determines whether a next request can be sent, without instantly coming across as
	 * non-/in-/super-human. Checks the current time against {@link WebSite#getTsLastRequest()}.
	 * 
	 * @param Calendar tsLastRequest - the time stamp of when the last request was made.
	 * @return {@code true} if the last request was made more than 10 seconds ago, {@code false} otherwise.
	 */
	public boolean isRightTime() {
		// Check whether the first request to this web site has been made. 
		if (tsNextRequest == null) {
			updateTsNextRequest();
			return true;
		}
		// Get the current time.
		Instant now = Instant.now();
		// If now is equal to or later than tsNextRequest, enough time has passed.
		return now.compareTo(tsNextRequest) >= 0;
	}
	
	/**
	 * Updates the field tsNextRequest, by getting the current time and adding a
	 * random number of milliseconds to it, making the interval between requests
	 * vary between 3 and 11 seconds.
	 */
	public void updateTsNextRequest() {
		// Create a random long stream.
		LongStream randomLongs = new Random().longs(3000L, 11000L);
		// Get the next random long from the stream.
		randomInterval = randomLongs.findFirst().getAsLong();
		// Log the result.
//		logger.log("Random number of milliseconds: " + millis);
		// Add it to the current time stamp as milliseconds and set as tsNextRequest.
		tsNextRequest = Instant.now().plus(randomInterval, ChronoUnit.MILLIS);
	}
	
	public long getRandomInterval() {
		return randomInterval;
	}

	public String getWebSiteName() {
		return WebStringUtils.determineWebSiteNameFromUrl(webSiteBaseUrl);
	}
	
	public String getWebSiteBaseUrl() {
		return webSiteBaseUrl;
	}

	public void setWebSiteBaseUrl(String webSiteBaseUrl) {
		this.webSiteBaseUrl = webSiteBaseUrl;
	}
}
