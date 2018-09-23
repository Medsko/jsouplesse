package jsouplesse;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.processing.FailedScanBuilder;

/**
 * Scans a web page containing a list of items (e.g. web shops).
 */
public abstract class AbstractListPageScanner {
	
	protected SqlHelper sqlHelper;
	
	/** The data object representing this list web page. */
	protected WebPage listPage;
	
	protected FailedScanBuilder failBuilder;
	
	// Output
	/** Holds the detail pages that were scraped from the list page. */
	protected List<WebPage> detailPages = new ArrayList<>();
	
	public AbstractListPageScanner(SqlHelper sqlHelper, WebPage listPage) {
		this.sqlHelper = sqlHelper;
		this.listPage = listPage;
	}
	
	/**
	 * Main processing method of this class. Scans the {@link #listPage} that was passed to
	 * the constructor.
	 */
	public void scanForDetailPages() {
		// Some requests may yield a response that can not be parsed as HTML by Jsoup.
		// In those cases, try to scan the response as raw text.
		if (listPage.getPageContents() != null) {
			scanHtmlDocument(listPage.getPageContents());
		} else if (listPage.getRawPageContents() != null) {
			scanRawHtml(listPage.getRawPageContents());
		}
	} 
	
	protected abstract void scanRawHtml(String rawHtml);
	
	protected abstract void scanHtmlDocument(Document html);
	
	public WebPage getListPage() {
		return listPage;
	}
	
	public void setFailBuilder(FailedScanBuilder failBuilder) {
		this.failBuilder = failBuilder;
	}

	public List<WebPage> getDetailPages() {
		return detailPages;
	}
}
