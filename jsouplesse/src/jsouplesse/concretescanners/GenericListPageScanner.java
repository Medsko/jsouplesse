package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;

import jsouplesse.AbstractListPageScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

public class GenericListPageScanner extends AbstractListPageScanner {

	public GenericListPageScanner(SqlHelper sqlHelper, WebPage listPage) {
		super(sqlHelper, listPage);
	}

	@Override
	public void scanForDetailPages() {
		// TODO Dud for now, since a generic way to scan web pages for required info is gonna be a challenge.
	}

	@Override
	protected void scanRawHtml(String rawHtml) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void scanHtmlDocument(Document html) {
		// TODO Auto-generated method stub
		
	}

	
	
}
