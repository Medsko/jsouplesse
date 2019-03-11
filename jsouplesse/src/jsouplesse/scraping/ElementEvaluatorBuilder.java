package jsouplesse.scraping;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.gui.ElementEvaluatorInput;
import jsouplesse.gui.SpiderInput;
import jsouplesse.util.CrappyLogger;

/**
 * Builds a new {@link ElementEvaluator} based on {@link ElementEvaluatorInput}.
 * 
 * The resulting {@link ElementEvaluator} could have-a {@link ElementEvaluator}
 * of its own, which could have-a evaluator of its own and so on, depending
 * on the depth of the search implied by the user's input.
 */
public class ElementEvaluatorBuilder {

	private CrappyLogger logger;
	
	private SqlHelper sqlHelper;
	
	private ContentFetcher webPageFetcher;
	
	private ElementEvaluator elementEvaluator;
	
	public ElementEvaluatorBuilder(CrappyLogger logger, SqlHelper sqlHelper) {
		this.logger = logger;
		this.sqlHelper = sqlHelper;
	}
	
	/**
	 * Builds an {@link ElementEvaluator} that goes as deep as the provided list
	 * of input suggests.
	 * 
	 * @param inputList - a list of commands, in desired order of execution.
	 * @return the {@link ElementEvaluator}, potentially containing a chain of 
	 * sub evaluators.
	 */
	public ElementEvaluator build(SpiderInput input) {
		
		webPageFetcher = new ContentFetcher(logger, sqlHelper, input.pageUrl);
		
		ElementEvaluator subEvaluator = null;
		
		for (ElementEvaluatorInput eeInput : input.inputList) {
			if (elementEvaluator == null) {
				// Build the primary ElementEvaluator.
				elementEvaluator = build(eeInput, input.pageUrl);
			} else {
				// Build a new ElementEvaluator and set it on the primary evaluator.
				subEvaluator = build(eeInput, input.pageUrl);
				elementEvaluator.setSubElementEvaluator(subEvaluator);
			}
		}
		// Set a flag on the last evaluator to signify it is the last in the chain.
		elementEvaluator.setLastInChain();
		// For testing purposes: only execute the crawl path once.
		elementEvaluator.setSelectOnlyOne(input.isTestRun);
		
		return elementEvaluator;
	}
	
	/**
	 * Builds a single shallow ElementEvaluator based on the provided
	 * {@link ElementEvaluatorInput}. 
	 */
	public ElementEvaluator build(ElementEvaluatorInput input, String grandParentUrl) {
		
		String selector = constructSelector(input.getTag(), input.getAttribute());
		
		ElementEvaluator evaluator = new ElementEvaluator(logger, sqlHelper, webPageFetcher, selector);
		evaluator.setShouldFetchWebPage(input.getShouldFetchWebPage());
		evaluator.setShouldFetchTextInLink(input.getShouldFetchTextInLink());
		evaluator.setParentUrl(grandParentUrl);
		evaluator.setShouldHuntForLogo(input.getShouldHuntForLogo());
				
		return evaluator;
	}
	
	/**
	 * Constructs a (hopefully valid) selector from the input from the user.
	 */
	private String constructSelector(String tag, String attribute) {
		
		String selector = "";
		if (tag.isEmpty())
			// No tag provided. Make selector match all tags.
			selector += "*";
		else
			selector += tag;
		
		if (!attribute.isEmpty()) {
			// Replace all double quotes with single.
			attribute = attribute.replace("\"", "'");
			// Wrap the String in brackets, to signify it's an attribute.
			selector += "[" + attribute + "]";
		}
		return selector;
	}
}
