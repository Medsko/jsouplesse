package jsouplesse.scraping;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector.SelectorParseException;

import jsouplesse.Result;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.util.CrappyLogger;
import jsouplesse.util.WebStringUtils;

/**
 * Evaluates an element.
 */
public class ElementEvaluator {

	private CrappyLogger logger;
	
	private SqlHelper sqlHelper;
	
	/** The selector used to select certain HTML elements. */
	private String selector;
	
	private ElementEvaluator subEvaluator;
	
	private boolean shouldFetchWebPage;
	
	private WebPageFetcher webPageFetcher;
	
	private String parentUrl;
	
	private WebPage webPage;
	
	private boolean foundOne = false;
	
	// Output
	/** The result of the operation. */
	private Result result;
	
	/** The list of company data that was retrieved in the search. */
	protected List<Company> companies = new ArrayList<>();
	
	/**
	 * Constructor to create a {@link ElementEvaluator} that selects the first viable link
	 * inside the last element selected based on user input.
	 */
	@Deprecated
	public ElementEvaluator(CrappyLogger logger, SqlHelper sqlHelper) {
		this.logger = logger;
		this.sqlHelper = sqlHelper; 
	}
	
	// Use ElementEvaluator(CrappyLogger, SqlHelper, WebPageFetcher, String)
	@Deprecated
	public ElementEvaluator(CrappyLogger logger, SqlHelper sqlHelper, String selector) {
		this.logger = logger;
		this.selector = selector;
		this.sqlHelper = sqlHelper;
	}
	
	/**
	 * Only legal constructor.
	 */
	public ElementEvaluator(CrappyLogger logger, 
			SqlHelper sqlHelper, 
			WebPageFetcher webPageFetcher, 
			String selector) {
		
		this.logger = logger;
		this.sqlHelper = sqlHelper;
		this.webPageFetcher = webPageFetcher;
		this.selector = selector;
	}
	
	/**
	 * Overloaded version of the core method, which kicks off the search by loading
	 * the first web page and then calling {@link #evaluate(Element)}.
	 */
	public boolean evaluate(String pageUrl) {
		if (!fetchPage(pageUrl)) {
			handleError("Failed to fetch the HTML document for page: " 
					+ pageUrl, "Fetch fail!");
			return false;
		}
		
		return evaluate(webPage.getPageContents());
	}
	
	/**
	 * Core method of this class. Takes an element and selects a URL from it, possibly
	 * uses it to fetch the next web page. A sub{@link ElementEvaluator} may be
	 * employed to dig further down in the selected elements or the fetched web page.
	 */
	public boolean evaluate(Element element) {
		// Check whether we should dig (even) deeper.
		if (selector != null) {
			// We should. Use the selector to select matching elements.
			Elements subElements;
			
			try {
				subElements = element.select(selector);
				
			} catch (SelectorParseException spex) {
				// Invalid selector. Set the result and return false.
				handleSelectorError(constructSelectorErrorMessage());
				return false;
			}
			
			if (shouldFetchWebPage) {
				
				if (!fetchPage(subElements)) {
					// Result has already been set.
					return false;
				}
				// Let the sub evaluator do its thing on the retrieved HTML document.
				if (!subEvaluate(webPage.getPageContents())) {
					// The sub evaluator had an error. Result has been set.
					return false;
				}
			} else {
				// Evaluate the sub elements.
				if (!subEvaluate(subElements)) {
					// The sub evaluator had an error. Result has been set.
					return false;
				}
			}			
		} else {
			// No deeper digging required. Select the URL from the current element.
			String webShopUrl = element.attr("href");
			
			if (webShopUrl.isEmpty()) {
				// The currently selected element is not an anchor. 
				// Select the first child that is an anchor with an href.
				Element subElement = element.selectFirst("a[href]");
				webShopUrl = subElement.attr("href");
			}
			
			// Check if the result matches the most basic requirements for a URL.
			if (webShopUrl.matches(WebStringUtils.INCLUSIVE_URL_REGEX)) {
				
				String companyName = WebStringUtils.determineWebSiteNameFromUrl(webShopUrl);
				Company company = new Company(companyName);
				company.setHomePageUrl(webShopUrl);
				companies.add(company);
				logger.log("Successfully scraped: " + company.toString());
			}
		}
		return true;
	}
	
	/**
	 * Uses the {@link #subEvaluator} to evaluate a single HTML element.
	 * A result is constructed in case of an error. 
	 */
	private boolean subEvaluate(Element element) {
// For testing purposes		
//		if (foundOne) {
//			boolean foundAll = true;
//			return foundAll;
//		}
		
		if (!subEvaluator.evaluate(element)) {
			// The sub evaluator had an error. Get the result from the failed 
			// evaluator, then return false so an error message can be built.
			result = subEvaluator.getResult();
			return false;
		}
		// Add the company data that the sub evaluator gathered to the
		// company list.
		companies.addAll(subEvaluator.getCompanies());
		
		// For testing purposes		
//		if (subEvaluator.getCompanies().size() > 0)
//			foundOne = true;

		return true;
	}
	
	private boolean subEvaluate(Elements elements) {
		for (Element element : elements) {
			if (!subEvaluate(element))
				return false;
		}
		return true;
	}
	
	/**
	 * Fetches the web page that corresponds to the	first link found in 
	 * the provided {@link Elements}.
	 */
	private boolean fetchPage(Elements selectedElements) {
		// Select the first viable link in the selected elements.
		Element link = selectedElements.select("a[href]").first();
		
		if (link == null) {
			// The selected elements did not contain an anchor tag with an href.
			String deconstructedSelector = deconstructSelector();
			handleSelectorError("ElementEvaluator.fetchPage() - the input " + deconstructedSelector
					+ " did not select an element with a valid link.");
			return false;
		}
		String pageUrl = link.attr("href");
		
		return fetchPage(pageUrl);
	}
	
	/**
	 * Fetches the page for the given URL. 
	 */
	private boolean fetchPage(String pageUrl) {
		
		if (!webPageFetcher.fetch(pageUrl)) {
			handleSelectorError("ElementEvaluator.fetchPage() - the request for the web page " 
					+ pageUrl + " did not return a response that could be parsed.");
			return false;
		}
		webPage = webPageFetcher.getWebPage();
		
		return webPage.getPageContents() != null;
	}
	
	private String constructSelectorErrorMessage() {
		// Reconstruct the selector input from the selector.
		String deconstructedSelector = deconstructSelector();
		// Construct a helpful error message.
		String message = "The input " + deconstructedSelector + " did not result in a valid selector. ";
		message += "Please try again, with different values for 'tag' and/or 'attribute'.";		
		return message;
	}

	private String deconstructSelector() {
		String deconstructedSelector = "tag: '";
		if (selector.startsWith("*")) {
			// The tag field was left empty in the input.
			deconstructedSelector += "<empty>,";
		} else if (selector.contains("[")) {
			// An attribute was provided in the input. First, end the tag part.
			deconstructedSelector += selector.substring(0, selector.indexOf("[")) + "', ";
			// Now determine the input for attribute.
			String attribute = selector.substring(selector.indexOf("[") + 1, selector.indexOf("]"));
			// Replace the single quotes with double.
			attribute = attribute.replace("'", "\"");
			deconstructedSelector += "attribute: '" + attribute + "'";
		} else {
			// Only a tag was provided.
			deconstructedSelector += selector;
		}
		return deconstructedSelector;
	}

	private void handleSelectorError(String message) {
		handleError(message, "Selector fail!");
	}
	
	private void handleError(String message, String title) {
		result = new Result(message, title);
		logger.log(message);
	}
	
	public String getSelector() {
		return selector;
	}

	public Result getResult() {
		return result;
	}
	
	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public void setSubElementEvaluator(ElementEvaluator subElementEvaluator) {
		if (subEvaluator == null)
			subEvaluator = subElementEvaluator;
		else
			subEvaluator.setSubElementEvaluator(subElementEvaluator);
	}
	
	public void setShouldFetchWebPage(boolean shouldFetchWebPage) {
		this.shouldFetchWebPage = shouldFetchWebPage;
	}

	public List<Company> getCompanies() {
		return companies;
	}
	
	public String getWebSiteName() {
		String ueberWebSiteUrl = webPageFetcher.getGrandParentUrl();
		return WebStringUtils.determineWebSiteNameFromUrl(ueberWebSiteUrl);
	}
}
