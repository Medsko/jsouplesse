package jsouplesse.scraping;

import org.jsoup.nodes.Document;

import jsouplesse.Result;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;

/**
 * Fetches the first page and fires off its {@link ElementEvaluator} to dig as
 * deep as necessary to get the desired company data. 
 */
@Deprecated
public class CustomScraper extends AbstractScanner {

	private Result result;
	
	private ElementEvaluator elementEvaluator;
	
	public CustomScraper(RequestTimer timer, WebSite webSite, WebPage webPage) {
		super(timer, webSite, webPage);
		// Initialize the web page.
		WebPageInitializer initializer = new WebPageInitializer(failBuilder);
		if (!initializer.initializeWebPage(webPage)) {
			System.out.println("Failed to load the web page: " + webPage.getPageUrl());
		}
	}

	@Override
	protected boolean scanRawHtml(String rawHtml) {
		// Since this operation will wreck performance if not carefully planned out,
		// throw an Exception until said planning has been worked out.
		throw new UnsupportedOperationException("Aw hell no!");
	}

	@Override
	protected boolean scanHtml(Document html) {
		
		if (!elementEvaluator.evaluate(html)) {
			// Somewhere along the evaluator line, an evaluator produced an error.
			result = elementEvaluator.getResult();
			return false;
		}
		
		companies.addAll(elementEvaluator.getCompanies());
		return true;
	}
		
	public Result getResult() {
		return result;
	}

	public Document getWebPageDocument() {
		return webPage.getPageContents();
	}
	
	public void setElementEvaluator(ElementEvaluator elementEvaluator) {
		this.elementEvaluator = elementEvaluator;
	}
}
