package jsouplesse;

import java.util.Optional;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Evaluates an HTML element that has been selected by a {@link CustomScraper}
 * because it matched the selector based on the input provided by the user.
 * 
 * Since the user might not have directly selected an anchor element containing
 * a link to the web shop in question, a {@link SubElementEvaluator} can be used
 * to 'dig down' into the selected element. 
 */
public class SelectedElementEvaluator {

	private Optional<String> webShopUrl;
	
	private String aggregateWebSiteUrl;
	
	private SubElementEvaluator subEvaluator;
	
	public SelectedElementEvaluator(String aggregateWebSiteUrl) {
		this.aggregateWebSiteUrl = aggregateWebSiteUrl;
	}
	
	public Optional<String> evaluate(Element element) {
		// Initialize the web shop URL to an empty optional.
		webShopUrl = Optional.empty();
		
		String rawWebShopUrl = element.attr("href");
		
		// Node.attr() returns an empty String, so no point in null checks.
		if (rawWebShopUrl.isEmpty()) {
			
			subEvaluator = new SubElementEvaluator();
			
			// The selector did not directly target an anchor tag. Try selecting
			// the anchors in the element, and checking each one for URL validity.
			Elements subElements = element.select("a"); 
			
			for (Element subElement : subElements) {
				
				Optional<String> maybeWebShopUrl = subEvaluator.evaluate(subElement);
				
				if (maybeWebShopUrl.isPresent()) {
					// The sub evaluator returned a result. Extract it and break 
					// the loop so it can be processed.
					rawWebShopUrl = maybeWebShopUrl.get();
					break;
				}
			}
		}
		// The selector selected an anchor tag directly. Process it.
		rawWebShopUrl = determineDirectWebShopLink(rawWebShopUrl);
		webShopUrl = Optional.of(rawWebShopUrl);
		
		return webShopUrl;
	}
	
	/**
	 * Determines whether the link to the web shop was a relative link, and if so,
	 * attempts to reconstruct a direct URL.
	 */
	private String determineDirectWebShopLink(String webShopUrl) {
		
		// TODO: move this to IOUtils.
		
		if (webShopUrl.contains(aggregateWebSiteUrl)) {
			// The URL is relative. Strip away the base URL of the aggregate web site.
			// Find the first slash after the base URL.
			int index = webShopUrl.indexOf("/", aggregateWebSiteUrl.length() - 1);
			// Select everything to the right of the found slash. 
			String tempWebShopUrl = webShopUrl.substring(index + 1);
			
			int startOfDirectUrl = tempWebShopUrl.indexOf("www.");
			
			if (startOfDirectUrl == -1)
				// Return the full relative URL to avoid out-of-bounds exception.
				return webShopUrl;
			else
				webShopUrl = tempWebShopUrl.substring(startOfDirectUrl);
		}
		
		return webShopUrl;
	}
}
