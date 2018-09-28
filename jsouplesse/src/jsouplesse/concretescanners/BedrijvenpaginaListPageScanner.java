package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

// Only kept for reference.
@Deprecated
public class BedrijvenpaginaListPageScanner {

	public BedrijvenpaginaListPageScanner(SqlHelper sqlHelper, WebPage listPage) {
	}


	protected void scanRawHtml(String rawHtml) {
		// Should never happen for this web site (famous last words).
	}

	protected void scanHtmlDocument(Document html) {
		// Select all div's with class="bedrijf"
		Elements companyCards = html.select(".bedrijf");
		
		for (Element entry : companyCards) {
			// For each div, select the h3 element.
			Element header = entry.select("h3").first();
			// In the h3, select the link.
			Element link = header.selectFirst("a");
			// The text of the link should be the web shop name.
			String webShopName = link.text();
			// Now retrieve the absolute link to the detail page.
			String detailPageUrl = link.attr("abs:href");
		}
	}
}
