package jsouplesse.scraping;

import java.util.List;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.gui.ElementEvaluatorInput;
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
	
	private WebPageFetcher webPageFetcher;
	
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
	public ElementEvaluator build(List<ElementEvaluatorInput> inputList, String grandParentUrl) {
		
		webPageFetcher = new WebPageFetcher(logger, sqlHelper, grandParentUrl);
		
		for (ElementEvaluatorInput input : inputList) {
			
			if (elementEvaluator == null) {
				// Build the primary ElementEvaluator.
				elementEvaluator = build(input);
			} else {
				// Build a new ElementEvaluator and set it on the primary evaluator.
				ElementEvaluator subEvaluator = build(input);
				elementEvaluator.setSubElementEvaluator(subEvaluator);
			}
		}
		// Construct a standard evaluator that will fetch the anchor tag in the deepest selection.
		ElementEvaluator subElementEvaluator = new ElementEvaluator(logger, sqlHelper, null, null);
		elementEvaluator.setSubElementEvaluator(subElementEvaluator);
		
		return elementEvaluator;
	}
	
	/**
	 * Builds a single shallow ElementEvaluator based on the provided
	 * {@link ElementEvaluatorInput}. 
	 */
	private ElementEvaluator build(ElementEvaluatorInput input) {
		
		String selector = constructSelector(input.getTag(), input.getAttribute());
		
		ElementEvaluator evaluator = new ElementEvaluator(logger, sqlHelper, webPageFetcher, selector);
		evaluator.setShouldFetchWebPage(input.getShouldFetchWebPage());
				
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
