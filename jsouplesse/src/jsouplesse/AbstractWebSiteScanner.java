package jsouplesse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.WebSiteSaveBuffer;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;
import jsouplesse.scraping.WebPageInitializer;
import jsouplesse.util.WebStringUtils;

/**
 * Scans an entire web site for company data.
 * 
 * Deprecated, and doesn't actually do anything anymore.
 */
@Deprecated
public abstract class AbstractWebSiteScanner implements Runnable {

	// Processing
	protected SqlHelper sqlHelper;
	
	protected WebSite webSite;
	
	protected WebPageInitializer webPageContentsHelper = new WebPageInitializer();
	
	protected FailedScanBuilder failBuilder;
	
	protected int randomInterval;

	// Output
	/** Holds all scraped company data. */
	protected List<Company> companyList = new ArrayList<>();

	/**
	 * Constructor which initializes the {@link #webSite} based on the given URL of a home page.
	 * @param String homePageUrl - the URL of the home page of the web site.
	 */
	public AbstractWebSiteScanner(SqlHelper sqlHelper, String homePageUrl) {
		this.sqlHelper = sqlHelper;
		// Initialize a web site object for this home page.
		webSite = new WebSite(sqlHelper, homePageUrl);
		webSite.setName(WebStringUtils.determineWebSiteNameFromUrl(homePageUrl));
		failBuilder = new FailedScanBuilder(sqlHelper);
	}

	@Override
	public void run() {
		
		if (!determineListPages()
				|| !determineDetailPages()
				|| ! scanDetailPages()) {
			// If any of the scanners returned false, that means an unrecoverable error occurred.
			// Save the results that were scraped so far and kill the process.
			saveResults();
			return;
		}
		
		saveResults();
	}
		
	/**
	 * Determines all list pages that can be scanned. The resulting URLs are added to the 
	 * {@link #webSite#listPages}.
	 */
	protected abstract boolean determineListPages();
	
	/**
	 * Using the list of list pages resulting from calling {@link #determineListPages()},
	 * determines all detail pages on the web site that can be scanned. The resulting URLs
	 * are added to the {@link #detailPageList}.
	 */
	private boolean determineDetailPages() {
		
		// Set the web page type for the WebPageInitializer.
		webPageContentsHelper.setWebPageTypeId(WebPage.TYPE_LIST);
		
		for (WebPage listPage : webSite.getListPages()) {
			
			randomInterval = (int) Math.random() * 8000 + 2000;
			// If the last request was sent less than 10 seconds ago, wait before sending the next.
			while (!theTimeIsRight(webSite.getTsLastRequest())) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Enough time has passed since previous request. Time to get to work.
			// Try to get a response that can be parsed from the web site.
			if (!webPageContentsHelper.initializeWebPage(listPage)) {
				// Something went wrong. Save the failed scan for later analysis.
				
				// TODO: finish FailedScan and create one here
				
				// Check if we should proceed to the next list page.
				if (webPageContentsHelper.getShouldResumeScanning()) {
					// No fatal error, we can move on to the next list page.
					continue;
				}	
				else
					// The web site blocked us. Admit defeat - for now.
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Attempts to scan all detail pages in {@link #detailPageList} for company data.
	 * Results are stored in {@link #companyList}.
	 * @return {@code true} if all detail pages were scanned successfully, {@code false}
	 * if an error occurred.
	 */
	private boolean scanDetailPages() {
		// Set the web page type for the WebPageInitializer.
		webPageContentsHelper.setWebPageTypeId(WebPage.TYPE_DETAIL);
		
		// Because some list pages will link directly to the home page of an actual web shop,
		// call a separate method, that sub classes can override, to determine whether enough 
		// time has passed since last request.
		for (WebPage detailPage : webSite.getDetailPages()) {
			// If the last request was sent less than 10 seconds ago, wait before sending the next.
			while (!theTimeIsRightForDetailPage()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Enough time has passed since previous request. Time to get to work.
			// Try to get a response that can be parsed from the web site.
			if (!webPageContentsHelper.initializeWebPage(detailPage)) {
				// Failed to scan the detail page. Save the failed scan, for later analysis.
				// TODO: finish FailedScan and use it here.

				// Something went wrong. Check if we should proceed to the next list page.
				if (webPageContentsHelper.getShouldResumeScanning()) 
					continue;
				else
					// The web site blocked us. Admit defeat - for now.
					return false;
			}
		}
		return true;
	}
		
	/**
	 * Attempts to find the next list page. In default implementation, this is done by 
	 * scanning for a link tag that encompasses the word 'next' or 'volgende'.
	 */
	protected String scanForNextListPage() {
		// Select all hyper links on the list page.
//		Elements links = currentListPage.getPageContents().select("a");
		
		// Scan for a link which encompasses the word 'next' or 'volgende'.
//		List<Element> possibleNextListPages = links.stream()
//			.filter(link -> link.text().matches("[vV]olgende|[nN]ext"))
//			.collect(Collectors.toList());
		
		// TODO: finish this generic method.
		
		return "";
	}
	
	/**
	 * Writes all company data the {@link #listPageScanner} currently holds to a file.
	 * This functionality is included on web site level so a separate file can be used
	 * for each web site, thereby preventing I/O conflicts.
	 * 
	 * Also saves all failed scans and pages that have not yet been scanned to internal
	 * database.
	 */
	public void saveResults() {
		// Determine the name of the file to which the results will be written.
		String fileName = "webshopsOn" + WebStringUtils.capitalize(webSite.getName()) + ".txt";
		Path filePath = Paths.get("C:", "Temp", fileName);
		
		// Use a writer with append = true.
		try (FileWriter fileWriter = new FileWriter(filePath.toFile(), true);
				BufferedWriter writer = new BufferedWriter(fileWriter)) {
			
			// Write the data of each company to a separate line in the file.
			for (Company company : companyList) {
				writer.write(company.toString());
				writer.newLine();
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		
		WebSiteSaveBuffer saveBuffer = new WebSiteSaveBuffer(sqlHelper);
		saveBuffer.addWebSite(webSite);
		
		try {
			saveBuffer.saveWebSite();
		} catch (SQLException e) {
			System.out.println("Failed when saving unscanned web pages to database.");
			e.printStackTrace();
		}
	}

	
	// TODO: these two methods should be handled by a TimeRequestHelper, which also sets the tsLastRequest
	// TODO: to the current time whenever it returns true to theTimeIsRight().
	/**
	 * Determines whether a next request can be sent, without instantly coming across as
	 * non-/in-/super-human. Checks the current time against {@link WebSite#getTsLastRequest()}.
	 * 
	 * @param Calendar tsLastRequest - the time stamp of when the last request was made.
	 * @return {@code true} if the last request was made more than 10 seconds ago, {@code false} otherwise.
	 */
	protected boolean theTimeIsRight(Calendar tsLastRequest) {
		Calendar now = Calendar.getInstance();
		// TODO: add this logic to RequestTimer.
		return tsLastRequest == null || now.getTimeInMillis() - tsLastRequest.getTimeInMillis() > randomInterval;
	}
	
	/**
	 * Determines whether a next request for a detail page can be sent. In this default implementation,
	 * equal to calling {@link #theTimeIsRight(Calendar)} with the tsLastRequest of {#webSite} as parameter.
	 */
	protected boolean theTimeIsRightForDetailPage() {
		return theTimeIsRight(webSite.getTsLastRequest());
	}
}
