package jsouplesse.singlepage;

import java.sql.SQLException;

import jsouplesse.AbstractScanner;
import jsouplesse.RequestTimer;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;
import jsouplesse.dataaccess.processing.WebSiteSaveBuffer;
import jsouplesseutil.CompanyFileWriter;
import jsouplesseutil.CrappyLogger;
import jsouplesseutil.WebStringUtils;
import javafx.scene.control.Alert;

/**
 * NB: cohesion was not taken into account during the design of this class.
 * 
 * This baby does it all: create a custom scanner for a single page based
 * on input from the user, save the results to text and CSV files, save
 * failed scans to the database and creating success and fail messages
 * for the view to display and the user to enjoy or get frustrated at. 
 */
public class ScrapeService {

	private SqlHelper sqlHelper;
	
	private CrappyLogger logger;
	
	private CustomScraper scraper;
	
	private Alert alert;
	
	public ScrapeService(SqlHelper sqlHelper, CrappyLogger logger) {
		this.sqlHelper = sqlHelper;
		this.logger = logger;
	}
	
	/**
	 * Central method to this service. Constructs a {@link CustomScraper}
	 * based on the provide {@link ScraperInput}, scrapes the target page
	 * for company data
	 */
	public boolean scrape(ScraperInput input) {
		
		String selector = constructSelector(input);
		
		String pageUrl = input.pageUrl;
		
		scraper = createScraper(pageUrl, selector);
		
		try {
			
			scraper.run();
			
		} catch (UnsupportedOperationException uoex) {
			// The page could not be parsed as an HTML document.
			String errorMessage = "Failed to parse the page as an html document.";
			buildErrorAlert("Parse fail!", errorMessage);
			return false;
		}
		// Everything went smoothly. Create a success alert and return true.
		buildSuccessAlert("Scrape success!", "The web page was successfully scraped!");
		
		return true;
	}
	
	private CustomScraper createScraper(String url, String selector) {
		// Create the objects necessary to instantiate a scanner.
		// First off, the request timer.
		RequestTimer timer = new RequestTimer();
		
		// Create the web site.
		String webSiteBaseUrl = WebStringUtils.determineBaseUrl(url);
		WebSite webSite = new WebSite(sqlHelper, webSiteBaseUrl);
		webSite.setName(WebStringUtils.determineWebSiteNameFromUrl(url));
		
		// Create the web page. Then, add it to the web site.
		WebPage pageToScrape = new WebPage(sqlHelper, url);
		pageToScrape.setWebPageTypeId(WebPage.TYPE_AGGREGATE);
		webSite.getWebPages().add(pageToScrape);
		
		// Now create the scanner using with the freshly created parameters.
		CustomScraper scraper = new CustomScraper(timer, webSite, pageToScrape);
		// Set the custom made selector on the scraper.
		scraper.setSelector(selector);
		
		return scraper;
	}

	/**
	 * Constructs a (hopefully valid) selector from the input from the user.
	 */
	private String constructSelector(ScraperInput input) {
		
		String selector = "";
		
		if (input.tagName == null)
			selector += "*";
		else
			selector += input.tagName;
		
		if (input.attribute != null) {
			String attribute = input.attribute.replace("\"", "'");
			selector += "[" + attribute + "]";
		}
		
		return selector;
	}
	
	/**
	 * Writes the company data that was successfully scraped from the page to one or two files.
	 * 
	 * @param scanner - the scanner for which the results should be written to file.
	 * @param shouldConvertToCsv - determines whether the results should also be written to CSV.
	 */
	public boolean writeCompaniesToFile(AbstractScanner scanner, boolean shouldConvertToCsv) {
		// Initialize the writer and pass the flag.
		CompanyFileWriter writer = new CompanyFileWriter();

		if (!writer.saveResults(scanner, shouldConvertToCsv)) {
			String errorMessage = writer.getResultMessage();
			buildErrorAlert("File write fail!", errorMessage);
			System.out.println(errorMessage);
		}
		// Everything went smoothly. Create a success alert and return true.
		buildSuccessAlert("Write success!", "The web shop urls were successfully written "
				+ "to file: " + writer.getFileName());

		return true;
	}
	
	public boolean saveFailedScansForWebsite(AbstractScanner scanner) {
		
		WebSite webSite = scanner.getWebSite();
		
		WebSiteSaveBuffer saveBuffer = new WebSiteSaveBuffer(sqlHelper);
		saveBuffer.addWebSite(webSite);
		
		try {
			
			saveBuffer.saveWebSite();
			
		} catch (SQLException e) {
			String errorMessage = saveBuffer.getResultMessage();
			logger.log(errorMessage);
			e.printStackTrace();
			buildErrorAlert("Database fail!", errorMessage);
		}
		
		buildSuccessAlert("Success!", "The web site's failed scans were successfully "
				+ "saved to the database!");
		
		return true;
	}
	
	
	private void buildErrorAlert(String title, String message) {
		alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setContentText(message);		
		// Skip that ugly bull shit.
		alert.setHeaderText(null);
		alert.setGraphic(null);
	}
	
	private void buildSuccessAlert(String title, String message) {
		alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setContentText(message);
		// Skip that ugly bull shit.
		alert.setHeaderText(null);
		alert.setGraphic(null);
	}

	public CustomScraper getScraper() {
		return scraper;
	}

	public Alert getAlert() {
		return alert;
	}
}
