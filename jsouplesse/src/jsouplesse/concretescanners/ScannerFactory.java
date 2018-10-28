package jsouplesse.concretescanners;

import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;
import jsouplesse.scraping.AbstractScanner;
import jsouplesse.scraping.WebSiteRequestConscience;

public class ScannerFactory {

	// Constants for the web site names.
	public final static String BEDRIJVENPAGINA = "bedrijvenpagina";
	public final static String ECOGOODIES = "ecogoodies";
	public final static String FLAVOURITES = "flavourites";
	public final static String GASTVRIJ_ROTTERDAM = "gastvrij-rotterdam";
	public final static String ALTRUISTO = "altruisto";
	
	public static AbstractScanner createScanner(WebSiteRequestConscience timer, WebSite webSite, WebPage webPage) {
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
