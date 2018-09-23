package jsouplesse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.processing.WebPageInitializer;

/**
 * Super for all detail page scanners. Based on the URL passed to the constructor,
 * a {@link WebPage} object is built, which can then be used to scan the detail page
 * in {@link #scanDetailPage()}. Subs should implement this method in such a way
 * that after calling it, {@link #getCompany()} can be used to retrieve the scraped
 * company data.
 */
public abstract class AbstractDetailPageScanner {

	// Input
	/** Representation of the detail page. */
	protected WebPage detailPage;
	
	// Processing
	/** Helper for constructing and executing SQL statements. */
	protected SqlHelper sqlHelper;
	
	// Output
	/** The company data that was scraped from the detail page. */
	protected Company company;
	
	/**
	 * Constructor that takes the URL of the detail page as parameter. This String
	 * is passed to a {@link WebPageInitializer} to construct the {@link #detailPage}.
	 * @param String url - the URL of the detail page.
	 */
	public AbstractDetailPageScanner(SqlHelper sqlHelper, WebPage detailPage) {
		this.sqlHelper = sqlHelper;
		this.detailPage = detailPage;
	}
	
	public void scanDetailPage() {
		if (detailPage.getPageContents() != null)
			scanHtml(detailPage.getPageContents());
		else if (detailPage.getRawPageContents() != null)
			scanRawHtml(detailPage.getRawPageContents());
	}
	
	protected abstract void scanRawHtml(String rawHtml);
	
	protected abstract void scanHtml(Document html);
	
	protected String nullSafeGetText(Element element) {
		if (element != null)
			return element.text();
		else
			return null;
	}
	
	public Company getCompany() {
		return company;
	}
}
