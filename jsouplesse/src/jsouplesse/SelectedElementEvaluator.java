package jsouplesse;

import java.util.Optional;

import org.jsoup.nodes.Element;

/**
 * Evaluates an HTML element that has been selected by a {@link CustomScraper}
 * because it matched the selector based on the input provided by the user.
 * 
 * Since the user might not have directly selected an anchor element containing
 * a link to the 
 */
public class SelectedElementEvaluator {

	private Optional<String> webShopUrl;
	
	private String aggregateWebSiteUrl;
	
	public SelectedElementEvaluator(String aggregateWebSiteUrl) {
		this.aggregateWebSiteUrl = aggregateWebSiteUrl;
	}
	
	public Optional<String> evaluate(Element element) {
		// Initialize the web shop URL to an empty optional.
		webShopUrl = Optional.empty();
		
		String directHit = element.attr("href");
		
		if (directHit.isEmpty()) {
			// TODO: create SubElementEvaluator, which will be called here (and
			// possibly call its own SubElementEvaluator, depending on how deep
			// a search the user's input suggests).
			
			// The selector did not directly target an anchor tag. Try selecting
			// the first anchor in the element, and calling this method for it.
			Element subElement = element.selectFirst("a");
			
			if (subElement != null)
				evaluate(subElement);
			
		} else {
			directHit = determineDirectWebShopLink(directHit);
			webShopUrl = Optional.of(directHit);
		}
		
		return webShopUrl;
	}
	
	/**
	 * Determines whether the link to the web shop was a relative link, and if so,
	 * attempts to reconstruct a direct URL.
	 */
	private String determineDirectWebShopLink(String webShopUrl) {
		
		if (webShopUrl.contains(aggregateWebSiteUrl)) {
			// The URL is relative. Strip away the base URL of the aggregate web site.
			// Find the first slash after the base URL.
			int index = webShopUrl.indexOf("/", aggregateWebSiteUrl.length() - 1);
			// Select everything to the right of the found slash. 
			String tempWebShopUrl = webShopUrl.substring(index + 1);
			
			int startOfDirectUrl = tempWebShopUrl.indexOf("www.");
			
			if (startOfDirectUrl == -1)
				// Return the full relative URL to avoid out-of-bounds Exception.
				return webShopUrl;
			else
				webShopUrl = tempWebShopUrl.substring(startOfDirectUrl);
		}
		
		return webShopUrl;
	}
}
