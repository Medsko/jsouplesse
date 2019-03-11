package jsouplesse.scraping;

import java.sql.SQLException;
import java.util.List;

import jsouplesse.Result;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.WebSiteSaveBuffer;
import jsouplesse.dataaccess.dao.WebSite;
import jsouplesse.gui.AlertBuilder;
import jsouplesse.gui.SpiderInput;
import jsouplesse.util.CompanyFileWriter;
import jsouplesse.util.CrappyLogger;
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
	
	private ElementEvaluator scraper;
	
	private Alert alert;
	
	private AlertBuilder alertBuilder;
	
	public ScrapeService(SqlHelper sqlHelper, CrappyLogger logger) {
		this.sqlHelper = sqlHelper;
		this.logger = logger;
		alertBuilder = new AlertBuilder();
	}
	
	/**
	 * Central method to this service. Constructs a {@link ElementEvaluator}
	 * based on the provide {@link ScraperInput}, scrapes the target page
	 * for company data
	 */
	public boolean scrape(SpiderInput spiderInput) {

		// Use the builder to create an ElementSelector using the selector input.
		ElementEvaluatorBuilder builder = new ElementEvaluatorBuilder(logger, sqlHelper);
		scraper = builder.build(spiderInput);
		
		try {
			if (!scraper.evaluate(spiderInput.pageUrl)) {
				// Error while scanning.
				Result result = scraper.getResult();
				buildErrorAlert(result.getTitle(), result.getMessage());
			}
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
	
	/**
	 * Writes the company data that was successfully scraped from the page to one or two files.
	 * 
	 * @param scanner - the scanner for which the results should be written to file.
	 * @param shouldConvertToCsv - determines whether the results should also be written to CSV.
	 */
	public boolean writeCompaniesToFile(ElementEvaluator scraper, boolean shouldConvertToCsv) {
		// Initialize the writer and pass the flag.
		CompanyFileWriter writer = new CompanyFileWriter();
		
		if (scraper.getCompanies().size() == 0) {
			// Get the first sequence of executed crawl steps from the evaluator and use it to 
			// construct helpful feedback for the user. 
			List<CrawlStep> failedCrawlSteps = scraper.getFirstRoundCrawlPath();
			alert = alertBuilder.buildResultFailErrorAlert("The scan yielded no results! Are you "
					+ "sure you entered all steps correctly?", failedCrawlSteps);
			return false;
		}

		if (!writer.saveResults(scraper, shouldConvertToCsv)) {
			String errorMessage = writer.getResultMessage();
			buildErrorAlert("File write fail!", errorMessage);
			logger.log(errorMessage);
		}
		// Everything went smoothly. Create a success alert and return true.
		buildSuccessAlert("Write success!", "The web shop urls were successfully written "
				+ "to file: " + writer.getFileName());

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
	
	public ElementEvaluator getScraper() {
		return scraper;
	}

	public Alert getAlert() {
		return alert;
	}
}
