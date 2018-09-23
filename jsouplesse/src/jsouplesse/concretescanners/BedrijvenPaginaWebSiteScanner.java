package jsouplesse.concretescanners;

import jsouplesse.AbstractWebSiteScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

public class BedrijvenPaginaWebSiteScanner extends AbstractWebSiteScanner {

	public BedrijvenPaginaWebSiteScanner(SqlHelper sqlHelper, String homePageUrl) {
		super(sqlHelper, homePageUrl);
	}

	@Override
	protected boolean determineListPages() {
		// The first list page is the home page url.
		String homePageUrl = webSite.getHomePageUrl();
		WebPage listPage = new WebPage(sqlHelper, homePageUrl);
		webSite.getListPages().add(listPage);
		
		// The other list pages add a parameter for the page number (80 in total).
		for (int i=2; i<=80; i++) {
			String listPageUrl = homePageUrl + "?p=" + i;
			listPage = new WebPage(sqlHelper, listPageUrl);
			webSite.getListPages().add(listPage);
		}
		return true;
	}
}
