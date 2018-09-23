package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.AbstractListPageScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;

public class BedrijvenpaginaListPageScanner extends AbstractListPageScanner {

	public BedrijvenpaginaListPageScanner(SqlHelper sqlHelper, WebPage listPage) {
		super(sqlHelper, listPage);
	}

	@Override
	protected void scanRawHtml(String rawHtml) {
		// Should never happen for this web site (famous last words).
	}

	@Override
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
			// Create a new WebPage using the resulting URL and add it to the list.
			WebPage detailPage = new WebPage(sqlHelper, detailPageUrl);
			detailPage.setWebShopName(webShopName);
			detailPages.add(detailPage);
		}
	}
}
