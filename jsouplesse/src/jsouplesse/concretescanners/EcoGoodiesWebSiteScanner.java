package jsouplesse.concretescanners;

import java.util.Calendar;

import jsouplesse.AbstractWebSiteScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

public class EcoGoodiesWebSiteScanner extends AbstractWebSiteScanner {

	public EcoGoodiesWebSiteScanner(SqlHelper sqlHelper, String homePageUrl) {
		super(sqlHelper, homePageUrl);
	}

	@Override
	protected boolean determineListPages() {
		
		WebPage homePage = new WebPage(sqlHelper, webSite.getHomePageUrl());
		
		// TODO: create a generic AbstractWebPageScanner that handles its own initialization.
		// This is a fucking disgrace. Fuck you Eclipse, fucking is a fucking word. Fuck.
		webPageContentsHelper.setWebPageTypeId(WebPage.TYPE_LIST);
		
		if (!webPageContentsHelper.initializeWebPage(homePage)) {
			System.out.println("determineListPages() - failed to load the home page");
		}
		webSite.setTsLastRequest(Calendar.getInstance());
		
		EcoGoodiesHomePageScanner scanner = new EcoGoodiesHomePageScanner(sqlHelper, homePage);
		scanner.scanForDetailPages();
		
		for (int i=1; i<scanner.getDetailPages().size(); i++) {
			webSite.getListPages().add(scanner.getDetailPages().get(i));
		}
		
//		webSite.getListPages().addAll(scanner.getDetailPages());
		
		return true;
	}
}
