package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.AbstractListPageScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

public class EcoGoodiesHomePageScanner extends AbstractListPageScanner {

	public EcoGoodiesHomePageScanner(SqlHelper sqlHelper, WebPage listPage) {
		super(sqlHelper, listPage);
	}

	// This method does not scan for detail pages: in truth, it scans for list pages on 
	// www.ecogoodies.com. Therefore, it should start by throwing an ExceptionException.
	@Override
	public void scanForDetailPages() {
		if (listPage.getPageContents() != null)
			scanHtmlDocument(listPage.getPageContents());
		else if (listPage.getRawPageContents() != null)
			scanRawHtml(listPage.getRawPageContents());
	}

	@Override
	protected void scanRawHtml(String rawHtml) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void scanHtmlDocument(Document html) {
		// Select all links.
		Elements links = html.select("a");
		// Loop through all links.
		for (Element link : links) {
			// If the link follows the pattern 'ecogoodies.nl/categories/', it is a link
			// to a list page of a category. 
			String href = link.attr("href");
			if (href.contains("ecogoodies.nl/categorie")) {
				// Save the URL to instantiate a new ListPageScanner later on, with the name
				// of the category (the part after 'categories/') as categoryName.
				WebPage categoryPage = new WebPage(sqlHelper, href);
				detailPages.add(categoryPage);
			}
		}
	}
}
