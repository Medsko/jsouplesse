package jsouplesse.scraping;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import jsouplesse.util.CrappyLogger;

/**
 * This helper class offers utility methods to shape a request in such a way that
 * the server will be obliged to send a response (other than a 403 grrrmmbl).
 */
public class PlausibleRequestHelper {

	private String userAgent;
	
	private String referrer;
	
	private CrappyLogger logger;
	
	private Response response;
	
	/**
	 * Flag signifying if the scanner should proceed to scanning the next page on the web site,
	 * or if further requests are pointless. Is only set to false when status is 403 (forbidden).
	 */
	private boolean shouldResumeScanning;
	
	public PlausibleRequestHelper(CrappyLogger logger) {
		this.logger = logger;
		shouldResumeScanning = true;
	}
	
	/**
	 * Indicates whether the last response was parsed successfully, or if the raw
	 * text of the response was read and returned. 
	 */
	private boolean canParseResponse;
	
	public boolean sendPageRequest(String url) {
		// Execute the request. Try to get a response that can be parsed as an HTML document.
		return executeRequest(url, false);
	}
	
	public boolean sendImageRequest(String url) {
		// Since it will be pretty hard to parse the image, ignore the content type of the response.
		return executeRequest(url, true);
	}
	
	private boolean executeRequest(String url, boolean ignoreContentType) {
		// Establish the connection, with a sensible maximum of bytes for DOM document size (5 MiB)
		// and the desired value for ignoreContentType.
		Connection connection = Jsoup.connect(url)
				.maxBodySize(1024 * 1024 * 5)
				.ignoreContentType(ignoreContentType);
		
		// Set a plausible user agent.
		if (userAgent != null)
			connection.userAgent(userAgent);
		else
			// No custom user agent has been set, so use default.
			connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
					+ "(KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
		
		// Set a plausible referrer.
		if (referrer != null)
			connection.referrer(referrer);
		else
			// No customer referrer has been set, so use default.
			connection.referrer("https://www.google.nl");
		
		try {
			
			response = connection.execute();
			// If content type had to be ignored, that means we won't be able to parse the response.
			canParseResponse = !ignoreContentType;
			
		} catch (UnsupportedMimeTypeException umtex) {
			// Execute the request again, this time ignoring content type.
			return executeRequest(url, true);
		} catch (HttpStatusException hsex) {
			// The request returned a HTTP error.
			if (hsex.getStatusCode() == 403) {
				// If we've been blocked, further scanning is pointless...for now.
				shouldResumeScanning = false;
				logger.log("WebPageInitializer got a 'Forbidden' code response!");
			}
		} catch (IOException ioex) {
			logger.log("WebPageFetcher.fetch() - I/O exception during request.");
			logger.log(ioex.getMessage());
		}
		
		return true;
	}
	
	public boolean getCanParseResponse() {
		return canParseResponse;
	}
	
	public Response getResponse() {
		return response;
	}

	public boolean getShouldResumeScanning() {
		return shouldResumeScanning;
	}

	/** Uses the helpful site whatismyreferer.com to check the default value for Jsoup referer. */
	public String checkDefaultReferer() throws IOException {
		return Jsoup.connect("https://www.whatismyreferer.com/").get().text();
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
}
