package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;

/**
 * Scans the detail page of a web shop.
 * Deprecated, but kept for future reference.
 */
@Deprecated
public class BedrijvenPaginaDetailPageScanner {
	
	private WebPage detailPage;
	
	public BedrijvenPaginaDetailPageScanner(SqlHelper sqlHelper, WebPage detailPage) {
	}

	protected void scanHtml(Document html) {
		// Get the div with class="box bedrijf".
		Element bedrijfBox = detailPage.getPageContents().selectFirst(".box.bedrijf");
		
		// Get the div with class="card".
		Element bedrijfCard = bedrijfBox.selectFirst(".card");
		// Get the div with class="tel phone" and, if it exists, get the text it contains. 
		Element phoneNumberDiv = bedrijfCard.selectFirst(".tel.phone");
		String phoneNumber = nullSafeGetText(phoneNumberDiv);
		// Get the div with class="tel mobile" and, if it exists, get the text it contains.
		Element mobileNumberDiv = bedrijfCard.selectFirst(".tel.mobile");
		String mobileNumber = nullSafeGetText(mobileNumberDiv);
		// Get the div with class="url" and, if it exists, get the text it contains.
		Element webShopLinkDiv = bedrijfCard.selectFirst(".url");
		String webShopLink = nullSafeGetText(webShopLinkDiv);
		// Get the div with class="mail" and, if it exists, get the email address from it.
		Element emailAddressDiv = bedrijfCard.selectFirst(".mail");
		String emailAddress = emailAddressDiv.text();
		
		// Construct a new CompanyDAO and set the retrieved values.
		Company company = new Company(detailPage.getWebShopName());
		company.setPhoneNumber(phoneNumber);
		company.setMobileNumber(mobileNumber);
		company.setHomePageUrl(webShopLink);
		company.addEmailAddress(emailAddress);
	}
	
	private String nullSafeGetText(Element element) {
		return "";
	}
}
