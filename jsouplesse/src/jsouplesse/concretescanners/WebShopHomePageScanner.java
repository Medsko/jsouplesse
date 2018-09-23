package jsouplesse.concretescanners;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.AbstractDetailPageScanner;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;

/**
 * Scans the home page of a web shop for company details (just contact information).
 */
public class WebShopHomePageScanner extends AbstractDetailPageScanner {
	
	public WebShopHomePageScanner(SqlHelper sqlHelper, WebPage detailPage) {
		super(sqlHelper, detailPage);
	}

	// Writing this class is going to be a challenge, since it will have to be pretty
	// generic. Luckily, most web sites are built by humans, so we should be okay.
	
	@Override
	protected void scanRawHtml(String rawHtml) {
		// TODO Auto-generated method stub
		
		// NB free tip from Milo: other possible formats include (0{0,2}31\\)?\\s?) - country code (just Dutch one in this regex)
		
		// 2) As an alternative, scan for phone numbers directly: match "\\d{2,3}-\\d{7,8}".
		// If two matches or more are found: check if one is mobile ("\\d{2}-\\d{8}" and one
		// land-line ("\\d{3}-\\d{7}") ? two probable direct numbers (so, fine): too many
		// numbers, further analysis needed -> select elements in which numbers are contained
		// and save all contained text.
		
		// Scan for email addresses (easy regex).
		// Try to filter the email addresses by matching them with the web site/company name.
		// If the filtered result is empty, store all found email addresses.
		
		
		String name = detailPage.getWebShopName();
		String url = detailPage.getPageUrl();
		company = new Company(name);
		company.setHomePageUrl(url);
		
	}

	@Override
	protected void scanHtml(Document html) {
		// Select all links.
		Elements links = html.select("a");
		
		for (Element link : links) {
			// Scan for a link with text().equalsIgnoreCase("contact").
			if (link.text().matches("Contact|contact")) {
				
			}
		}
		
		// 1) Scan the resulting page for an element with a text matching "[Tt]el\\.?.{0,14}\\d"
	
		// If preceding methods fail:
		// Scan for a header with text().equalsIgnoreCase("contact") (or use a 
		// Pattern.matcher, if you want to get fancy).
		// OR directly scan the raw HTML for phone numbers, re-using the logic specified in 2).
		
		String name = detailPage.getWebShopName();
		String url = detailPage.getPageUrl();
		company = new Company(name);
		company.setHomePageUrl(url);
	}
	
}
