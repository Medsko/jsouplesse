package jsouplesse.scraping;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
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
	
	/** The selector used to select certain HTML elements. */
	private String selector;
	
	private ElementEvaluator subEvaluator;
	
	private boolean shouldFetchWebPage;
	
	private boolean shouldFetchTextInLink;
	
	private ContentFetcher webPageFetcher;
	
	private boolean shouldHuntForLogo;
	
	private LogoHunter hunter;
	
	private String parentUrl;
	
	private WebPage webPage;
	
	private Elements firstRoundSelectedElements;
	
	/** Indicates whether this evaluator is the last in the chain of evaluations. */
	private boolean lastInChain;
	
	private boolean firstRound = true;
	
	private boolean selectOnlyOne;
	
	// Output
	/** The result of the operation. */
	private Result result;
	
	/** The list of company data that was retrieved in the search. */
	protected List<Company> companies = new ArrayList<>();
	
	public ElementEvaluator(CrappyLogger logger, 
			SqlHelper sqlHelper, // Might get useful in the future...
			ContentFetcher webPageFetcher, 
			String selector) {
		
		this.logger = logger;
		this.webPageFetcher = webPageFetcher;
		this.selector = selector;
	}
	
	// TODO: towards self-learning Crawling
	// 1) follow a CrawlPath (select element > link through > select element etc.)
	// 2) check if the path is valid > if not, save failing step of path in List<CrawlStep>
	// 3) check if the path yields result > if so, add to List<Company>
	// 4) follow next path, while checking with each new step if that step has already been tried 
	// 		unsuccessfully once (check List<CrawlStep>) > if so, move on to next CrawlPath
	
	
	/**
	 * Overloaded version of the core method, which kicks off the search by loading
	 * the first web page and then calling {@link #evaluate(Element)}.
	 */
	public boolean evaluate(String pageUrl) {
		// Update the parent URL for this Evaluator, if necessary. This is safe to do here, since
		// this method is only called with a String URL argument from the ScrapeService.
		parentUrl = WebStringUtils.removeFilterFromUrlIfPresent(pageUrl);
		
		Document pageContents = fetchPage(pageUrl); 
		
		if (pageContents == null) {
			handleError("Failed to fetch the HTML document for page: " 
					+ pageUrl, "Fetch fail!");
			return false;
		}
		
		return evaluate(pageContents);
	}
	
	/**
	 * Core method of this class. Takes an element and selects a URL from it, possibly
	 * uses it to fetch the next web page. A sub{@link ElementEvaluator} may be
	 * employed to dig further down in the selected elements or the fetched web page.
	 */
	public boolean evaluate(Element element) {

		Elements subElements;
		
		try {
			// Use the selector to select matching elements.
			subElements = element.select(selector);
		} catch (SelectorParseException spex) {
			// Invalid selector. Set the result and return false.
			handleSelectorError(constructSelectorErrorMessage());
			return false;
		}
		
		// If this is the first time we walk this path, do some validity checks and provide 
		// feedback if the search yields no results.
		if (firstRound) {
			// Save the elements that where selected in the first round.
			firstRoundSelectedElements = subElements;
			
			if (subElements.size() > 0) {
				logger.log("Selector '" + selector + "' was used to select section: ");
				logger.logHtml(subElements.get(0));
			} else {
				handleSelectorError("Selector '" + selector + "' yielded no results!");
				return false;
			}
			firstRound = false;
		}
		
		for (Element subElement : subElements) {
			// Evaluate the sub element.
			if (!subEvaluate(subElement)) {
				// An error occurred. Result has already been set.
				return false;
			}
			
			if (selectOnlyOne)
				return true;
		}
		return true;
	}
	
	/**
	 * Checks whether an element is a link, and if not, selects the first child element that is a
	 * link and returns it. If no such element is found, null is returned.
	 */
	private Element selectFirstLink(Element element) {
		if (!element.tagName().equals("a"))
			// The currently selected element is not an anchor. 
			// Select the first child that is an anchor with an href.
			element = element.selectFirst("a[href]");
		return element;
	}
	
	/**
	 * Uses evaluates a single HTML element. If this is the last evaluator in the evaluation chain,
	 * company data will be extracted. In other cases, the {@link #subEvaluator} is used to execute
	 * the next step(s) of the path. In case of an error, a result is constructed. 
	 */
	private boolean subEvaluate(Element element) {
		
		if (lastInChain) {
			// No deeper digging required. Select the URL from the current element.
			String webShopUrl = null;

			element = selectFirstLink(element);
			// Check whether we should select the attribute of the anchor, or its text.
			if (shouldFetchTextInLink) {
				webShopUrl = element.text();
			} else {
				webShopUrl = element.attr("href");
			}
			
			String companyName = WebStringUtils.determineWebSiteNameFromUrl(webShopUrl);
			Company company = new Company(companyName);
			company.setHomePageUrl(webShopUrl);				
			companies.add(company);
			logger.log("Successfully scraped: " + company.toString());
			
		} else {
			// A sub evaluation should be carried out.
			if (shouldFetchWebPage) {
				// The sub evaluation should be performed on the linked-to web page. Fetch it.
				element = fetchPage(element);
				
				if (element == null)
					// Failure to retrieve the web page. Result has been set.
					return false;
				
				if (shouldHuntForLogo) {
					// The logo should be tried to be retrieved from this freshly fetched page.
					hunter = new LogoHunter(logger, webPageFetcher);
					hunter.hunt(element, 
							WebStringUtils.determineWebSiteNameFromUrl(webPage.getPageUrl()));
				}
			}
			// Update the parent URL of the subEvaluator, if necessary.
			subEvaluator.setParentUrl(WebStringUtils.determineBaseUrl(webPage.getPageUrl()));
			
			if (!subEvaluator.evaluate(element)) {
				// The sub evaluator had an error. Get the result from the failed 
				// evaluator, then return false so an error message can be built.
				result = subEvaluator.getResult();
				return false;
			}
			// Add the company data that the sub evaluator gathered to the company list.
			for (Company company : subEvaluator.getCompanies()) {
				// Ignore companies that we already collected.
				if (!companies.contains(company))
					companies.add(company);
			}
		}
		return true;
	}

	
	private Document fetchPage(Element selectedElement) {
		
		String pageUrl;
		
		if (shouldFetchTextInLink)
			pageUrl = selectedElement.text();
		else
			pageUrl = selectedElement.attr("href");
		
		return fetchPage(pageUrl);
	}
	
	/**
	 * Fetches the page for the given URL. 
	 */
	private Document fetchPage(String pageUrl) {
		// If the URL is relative, resolve against parent.
		pageUrl = WebStringUtils.resolveAgainstParent(pageUrl, parentUrl);
		
		if (!webPageFetcher.fetchWebPage(pageUrl)) {
			handleSelectorError("ElementEvaluator.fetchPage() - the request for the web page " 
					+ pageUrl + " did not return a response that could be parsed.");
			return null;
		}
		webPage = webPageFetcher.getWebPage();
		
		return webPage.getPageContents();
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
			deconstructedSelector += "<empty>',";
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
	
	public void setShouldFetchTextInLink(boolean shouldFetchTextInLink) {
		this.shouldFetchTextInLink = shouldFetchTextInLink;
	}

	public void setShouldHuntForLogo(boolean shouldHuntForLogo) {
		this.shouldHuntForLogo = shouldHuntForLogo;
	}

	public List<Company> getCompanies() {
		return companies;
	}
	
	/** 
	 * Sets the flag {@link #lastInChain} for this or, if it has a sub, for that evaluator to true. 
	 */
	public void setLastInChain() {
		if (subEvaluator == null)
			this.lastInChain = true;
		else
			subEvaluator.setLastInChain();
	}

	public String getWebSiteName() {
		String ueberWebSiteUrl = webPageFetcher.getGrandParentUrl();
		return WebStringUtils.determineWebSiteNameFromUrl(ueberWebSiteUrl);
	}

	public void setSelectOnlyOne(boolean selectOnlyOne) {
		this.selectOnlyOne = selectOnlyOne;
		if (subEvaluator != null)
			subEvaluator.setSelectOnlyOne(selectOnlyOne);
	}
	
	/**
	 * Recursively collects all steps that were executed by this evaluator and all its 
	 * sub-evaluators.
	 * @return a list of the crawl steps taken in this chain, in reverse order of execution. 
	 */
	public List<CrawlStep> getFirstRoundCrawlPath() {
		
		List<CrawlStep> crawlPath = new ArrayList<>();
		// Get the crawl steps of lower evaluators in the chain.
		if (subEvaluator != null)
			crawlPath.addAll(subEvaluator.getFirstRoundCrawlPath());
		
		// Check if this evaluator selected any elements.
		if (firstRoundSelectedElements != null) {
			// Construct this evaluator's crawl step.
			CrawlStep crawlStep = new CrawlStep();
			crawlStep.setSelector(selector);
			crawlStep.setSelectedElements(firstRoundSelectedElements.first());
			
			if (shouldFetchWebPage)
				crawlStep.setFetchedLink(webPage.getPageUrl());
			
			crawlPath.add(crawlStep);
		}
		
		return crawlPath;
	}
}
