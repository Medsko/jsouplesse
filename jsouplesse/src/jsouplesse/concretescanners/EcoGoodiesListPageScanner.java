package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.AbstractListPageScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

/**
 * Scanner for EcoGoodies.com. This web site is different, and dare I say: better, as
 * it does not list contact information...only (descriptions of and) links to the actual
 * web sites of the listed web shops. Therefore, the risk of overloading the servers of 
 * either this listing site or the web sites it links to can be minimized without much 
 * use of a timer.
 */
public class EcoGoodiesListPageScanner extends AbstractListPageScanner {

	public EcoGoodiesListPageScanner(SqlHelper sqlHelper, WebPage listPage) {
		super(sqlHelper, listPage);
	}

	/** 
	 * This web site orders its listed web shops by category. Respect this order, by
	 * instantiating a new LPScanner for each category.
	 */
	private String categoryName;
	
	@Override
	public void scanForDetailPages() {
		if (listPage.getPageContents() != null)
			scanHtmlDocument(listPage.getPageContents());
		else if (listPage.getRawPageContents() != null)
			scanRawHtml(listPage.getRawPageContents());		
	}

	@Override
	protected void scanRawHtml(String rawHtml) {
		throw new UnsupportedOperationException("Ecogoodies.scanRawHtml() - coming soon!");
	}

	@Override
	protected void scanHtmlDocument(Document html) {
		// Here, scan the actual (category) list pages for detail pages.
		
		// Select all links with '.clearfix'.
		Elements links = html.select("a.clearfix");
		
		for (Element link : links) {
			// Extract the URL: this is the link to the detail page.
			String href = link.attr("href");
			WebPage detailPage = new WebPage(sqlHelper, href);
			detailPages.add(detailPage);
		}
	}

	
}
