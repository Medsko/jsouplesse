package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

/**
 * Scans a list page of flavourites.nl.
 * Deprecated: only exists for future reference, when scanning raw HTML is going to be implemented.
 */
@Deprecated
public class FlavouritesListPageScanner {

	private WebPage listPage;
	
	public FlavouritesListPageScanner(SqlHelper sqlHelper, WebPage listPage) {
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
		
		// Create a new WebPage using the resulting URL and add it to the list.
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
			}
		}
	}	
}
