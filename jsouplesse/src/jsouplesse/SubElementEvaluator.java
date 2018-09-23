package jsouplesse;

import java.util.Optional;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesseutil.WebStringUtils;

/**
 * First and thoroughly crappy implementation of a class that can evaluate
 * an element inside a previously selected element.
 * 
 * In the #future, these should be dynamically created based on user input.
 * Also, there should only be an {@link SelectedElementEvaluator}, which
 * should follow this evaluator's composite (GoF pattern) blueprint.
 */
public class SubElementEvaluator {

	private Optional<String> webShopUrl;
	
	private String selector;
	
	private SubElementEvaluator subEvaluator;
	
	public SubElementEvaluator() {}
		
	public SubElementEvaluator(String selector, SubElementEvaluator subEvaluator) {
		this.selector = selector;
		this.subEvaluator = subEvaluator;
	}
	
	public Optional<String> evaluate(Element element) {
		
		// Initialize the web shop URL to an empty optional.
		webShopUrl = Optional.empty();

		// Check whether we should dig even deeper.
		if (selector != null) {
			
			Elements subElements = element.select(selector);
			
			for (Element subElement : subElements) {
				
				webShopUrl = subEvaluator.evaluate(subElement);
				
				if (webShopUrl.isPresent()) {
					// Conditions of the sub evaluator were met.
					// Return the result.
					return webShopUrl;
				}
			}
		}
		// No deeper digging required. Select the URL from the current element.
		String rawWebShopUrl = element.attr("href");
		
		if (rawWebShopUrl.matches(WebStringUtils.INCLUSIVE_URL_REGEX)) {
			webShopUrl = Optional.of(rawWebShopUrl);
		}
		
		return webShopUrl;
	}
}
