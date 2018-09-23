package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.AbstractListPageScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.FailedScan;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesseutil.WebStringUtils;

/**
 * Scans a list page of flavourites.nl. 
 */
public class FlavouritesListPageScanner extends AbstractListPageScanner {

	private String mangledWebShopUrl;
		
	public FlavouritesListPageScanner(SqlHelper sqlHelper, WebPage listPage) {
		super(sqlHelper, listPage);
	}
	
	protected void scanRawHtml(String rawHtml) {
		// The mangled URL to the web page is set in the attribute "Guid". Scan for this attribute.
		int startOfMangledUrl = rawHtml.indexOf("Guid");
		// Check if there are no more links to web shops present in the request.
		if (startOfMangledUrl == -1)
			// No more links to be found, so we're done scanning.
			return;
		// Found the start of a web shop URL attribute. Add seven to skip 'Guid":"' part.
		startOfMangledUrl += 7;
		// Strip away everything to the left of the first mangled URL.
		rawHtml = rawHtml.substring(startOfMangledUrl);
		// Extract the mangled URL, which should end at the next double quote.
		String mangledWebShopUrl = rawHtml.substring(0, rawHtml.indexOf('"'));
		// Determine the actual web shop home page URL.
		String webShopUrl = deduceOriginalUrl(mangledWebShopUrl);
		// Create a new WebPage using the resulting URL and add it to the list.
		WebPage webShopHomePage = new WebPage(sqlHelper, webShopUrl);
		detailPages.add(webShopHomePage);
		// Repeat the process until the last mangled URL has been found.
		scanRawHtml(rawHtml);
	}
	
	protected void scanHtmlDocument(Document html) {
		// Select all links.
		Elements links = listPage.getPageContents().select("a");
		
		// Loop through the links.
		for (Element link : links) {
			// Check whether the link text contains 'Bezoek shop' (or matches it, ignoring whitespace).
			if (link.text().matches("Bezoek shop")) {
				// This is a possible relative link to the actual web site of the web shop.
				String webShopUrl = deduceOriginalUrl(link.attr("href"));
				
				if (webShopUrl == null)
					continue;

				// Use the resulting URL to create a new WebPage and add it to the list.
				WebPage webShopHomePage = new WebPage(sqlHelper, webShopUrl);
				detailPages.add(webShopHomePage);
			}
		}
	}
	
	/** Reconstructs the original URL that leads to the home page of a web shop. */
	private String deduceOriginalUrl(String mangledWebShopUrl) {
		// The direct URL can be reconstructed from this by replacing the underscore by a period
		// and prefixing "http://www.".
		String originalUrl = mangledWebShopUrl.replaceAll("_", "\\.");
		originalUrl = "http://www." + originalUrl;
		
		if (!originalUrl.matches(WebStringUtils.URL_IN_TEXT_REGEX)) {
			// The URL is not yet a valid one. Try to fix it.
			String[] urlParts = originalUrl.split("-");
			originalUrl = urlParts[0];
			
			outer:
			for (int i=1; i<urlParts.length; i++) {
				
				for (String countryCode : WebStringUtils.SUPPORTED_COUNTRY_CODES) {
					
					if (urlParts[i].equals(countryCode)) {
						originalUrl += "." + urlParts[i];
						break outer;
					}
				}
			}
		}
		
		if (!originalUrl.matches(WebStringUtils.URL_IN_TEXT_REGEX)) {
			// The URL still doesn't appear to be viable. Build a report and return null.
			WebPage failedShopHomePage = new WebPage(sqlHelper, mangledWebShopUrl);
			failBuilder.buildFailedScan(failedShopHomePage, FailedScan.Reason.INVALID_URL);
			return null;
		}
		
		return originalUrl;
	}
	
	public String getMangledWebShopUrl() {
		return mangledWebShopUrl;
	}
}
