package jsouplesse.concretescanners;

import jsouplesse.AbstractWebSiteScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

/**
 * Custom-made scanner, able to scan the web site flavourites.nl.
 */
public class FlavouritesWebSiteScanner extends AbstractWebSiteScanner {

	public FlavouritesWebSiteScanner(SqlHelper sqlHelper, String homePageUrl) {
		super(sqlHelper, homePageUrl);
	}
	
	@Override
	protected boolean determineListPages() {
		// This web site has an endless scroll. To get all detail pages, a new LPScanner
		// will have to be created for each next retrieved batch of detail page links.
		// Each of these batches can be retrieved by using this pattern:
		// https://www.flavourites.nl/Shop/GetShops?parentGuid=ECO&sliderPosition=48&subItemGuid=
		// in which the sliderPosition parameter starts at 0 and should be incremented by 16 
		// to retrieve the next batch.
		for (int i=0; i<=64; i+=16) {
			String listPageUrl = "https://www.flavourites.nl/Shop/GetShops?parentGuid=ECO&sliderPosition=";
			listPageUrl += i + "&subItemGuid=";
			WebPage listPage = new WebPage(sqlHelper, listPageUrl);
			webSite.getListPages().add(listPage);
		}
		return true;
	}
	
	@Override
	protected boolean theTimeIsRightForDetailPage() {
		// Since all detail pages for this web site are home pages of separate web shops,
		// the risk of overloading a server with requests is minimal.
		return true;
	}
}
