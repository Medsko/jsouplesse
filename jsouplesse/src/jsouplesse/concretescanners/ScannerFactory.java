package jsouplesse.concretescanners;

import jsouplesse.AbstractDetailPageScanner;
import jsouplesse.AbstractListPageScanner;
import jsouplesse.AbstractScanner;
import jsouplesse.RequestTimer;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;

public class ScannerFactory {

	// Constants for the web site names.
	public final static String BEDRIJVENPAGINA = "bedrijvenpagina";
	public final static String ECOGOODIES = "ecogoodies";
	public final static String FLAVOURITES = "flavourites";
	public final static String GASTVRIJ_ROTTERDAM = "gastvrij-rotterdam";
	public final static String ALTRUISTO = "altruisto";

	
	// TODO: public static AbstractWebPageScanner createWebPageScanner()
	
	public static AbstractListPageScanner createListPageScanner(SqlHelper sqlHelper, String webSiteName, WebPage listPage) {
		switch (webSiteName) {
			case BEDRIJVENPAGINA:
				return new BedrijvenpaginaListPageScanner(sqlHelper, listPage);
			case ECOGOODIES:
				return new EcoGoodiesListPageScanner(sqlHelper, listPage);
			case FLAVOURITES:
				return new FlavouritesListPageScanner(sqlHelper, listPage);
			default:
				return new GenericListPageScanner(sqlHelper, listPage);
		}
	}
	
	public static AbstractDetailPageScanner createDetailPageScanner(SqlHelper sqlHelper, String webSiteName, WebPage detailPage) {
		switch (webSiteName) {
			case BEDRIJVENPAGINA:
				return new BedrijvenPaginaDetailPageScanner(sqlHelper, detailPage);
			case ECOGOODIES:
				return new EcoGoodiesDetailPageScanner(sqlHelper, detailPage);
			case FLAVOURITES:
				// Links directly to listed web shops, so use default.
			default:
				return new WebShopHomePageScanner(sqlHelper, detailPage);
		}
	}
	
	public static AbstractScanner createScanner(RequestTimer timer, WebSite webSite, WebPage webPage) {
		// TODO: use the field WebPage.typeId to determine the type of scanner after the web site has been determined.
		switch (webSite.getName()) {
			case GASTVRIJ_ROTTERDAM:
				return new GastvrijRotterdamScanner(timer, webSite, webPage);
			case ALTRUISTO:
				return new AltruistoScanner(timer, webSite, webPage);
			default:
				return new GastvrijRotterdamScanner(timer, webSite, webPage);
		}
	}
	
}
