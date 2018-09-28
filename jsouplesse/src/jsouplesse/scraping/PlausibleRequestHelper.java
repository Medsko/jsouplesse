package jsouplesse.scraping;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

/**
 * This helper class offers utility methods to shape a request in such a way that
 * the server will be obliged to send a response (other than a 403 grrrmmbl).
 */
public class PlausibleRequestHelper {

	private String userAgent;
	
	private String referrer;
	
	/**
	 * Indicates whether the last response was parsed successfully, or if the raw
	 * text of the response was read and returned. 
	 */
	private boolean canParseResponse;
	
	public Response sendRequest(String url) throws IOException {
		// (Re)set flag to default.
		canParseResponse = false;
		
		// Establish the connection.	
		Connection connection = Jsoup.connect(url);
		
		// Set a sensible maximum of bytes to read from the DOM document (2 MiB).
		connection.maxBodySize(1024 * 1024 * 2);
		
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
		
		Response response;
		
		try {
			
			response = connection.execute();
			canParseResponse = true;
			
		} catch (UnsupportedMimeTypeException umtex) {
			// Execute the request again, this time ignoring content type.
			connection.ignoreContentType(true);
			response = connection.execute();
		}
		
		return response;
	}
	
	public boolean getCanParseResponse() {
		return canParseResponse;
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
