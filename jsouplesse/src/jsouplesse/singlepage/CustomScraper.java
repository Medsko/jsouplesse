package jsouplesse.singlepage;

import java.util.Optional;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector.SelectorParseException;

import jsouplesse.AbstractScanner;
import jsouplesse.RequestTimer;
import jsouplesse.Result;
import jsouplesse.SelectedElementEvaluator;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;
import jsouplesseutil.WebStringUtils;

public class CustomScraper extends AbstractScanner {

	private String selector;
	
	private Result result;
	
	private SelectedElementEvaluator evaluator;
	
	public CustomScraper(RequestTimer timer, WebSite webSite, WebPage webPage) {
		super(timer, webSite, webPage);
		evaluator = new SelectedElementEvaluator(webPage.getPageUrl());
	}

	@Override
	protected boolean scanRawHtml(String rawHtml) {
		// Since this operation will wreck performance if not carefully planned out,
		// throw an Exception until said planning has been worked out.
		throw new UnsupportedOperationException("Aw hell no!");
	}

	@Override
	protected boolean scanHtml(Document html) {
		
		Elements elements = null;
		
		try {
		
			elements = html.select(selector);
			
		} catch (SelectorParseException spex) {
			// The input provided did not result in a valid selector.
			result = new Result();
			result.setTitle("Selector fail!");
			result.setMessage("The input provided did not result in a valid selector. "
					+ "Please try again, with different values for 'tag' and/or 'css class'.");
			return false;
		}
		
		for (Element element : elements) {
			
			Optional<String> maybeWebShopUrl = evaluator.evaluate(element);
			
			if (!maybeWebShopUrl.isPresent()) {
				// The evaluator failed to determine the web shop URL.
				// TODO: construct a FailedScan here and fill it sensibly.
				continue;
			}
			
			String webShopUrl = maybeWebShopUrl.get();
			String companyName = WebStringUtils.determineWebSiteNameFromUrl(webShopUrl);
			Company company = new Company(companyName);
			company.setHomePageUrl(webShopUrl);
			companies.add(company);
			
			System.out.println("Successfully scraped: " + company.toString());
		}

		return true;
	}
	
	public Result getResult() {
		return result;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}
}
