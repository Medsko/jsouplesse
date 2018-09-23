package jsouplesse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jsouplesse.concretescanners.ScannerFactory;
import jsouplesse.dataaccess.Connector;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.Company;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;
import jsouplesse.dataaccess.processing.WebSiteSaveBuffer;
import jsouplesseutil.IOUtils;
import jsouplesseutil.WebStringUtils;

/**
 * Think of this baby as an executor, that fires off a new scanner for each
 * web site that we want to scan. When a chain of scanners and scrapers either
 * fails or finishes, SearchCoordinater writes the resulting {@link Company} 
 * and {@link FailedScan} data to respectively a file or the database.
 */
public class SearchCoordinator {
	
	// TODO: humanize this bot, in a humane way:
	/* 1) don't bombard a web site with requests:
	 *  Multi-threaded: visit different sites of interest sequentially (since waiting time is used, more sites = more results in same time)
	 * 2) be a good bot, and identify yourself as such:
	 * 	a) provide a name (how?) and a web site where purpose of the bot is explained
	 * 	b) check and follow the robots.txt file
	 * 
	 * NICE TO HAVE:
	 * - scan parsed sites for links to comparable sites
	 */
	/**
	 * The helper for constructing and executing SQL statements.
	 * NB: in multi-threaded implementation, this (single) connection has to be handled carefully. 
	 */
	private SqlHelper sqlHelper;
	
	/**
	 * In the future, this list should be the pool of scanners that can be run concurrently.
	 */
	private List<AbstractScanner> scanners = new ArrayList<>();
	
	private boolean shouldConvertToCsv;

	
	public SearchCoordinator() {
		Connector connector = new Connector();
		sqlHelper = new SqlHelper(connector);
	}

	public void scanWebSites(List<String> urls) {
		
		for (String url : urls)
			if (url != null)
				scanners.add(createScanner(url));
		
		try {
			
			Thread thread = null;
			
			for (AbstractScanner webSiteScanner : scanners) {
				// TODO: define a thread pool, make list of scanners a Queue, make data objects and database methods synchronized etc.
				thread = new Thread(webSiteScanner);
				thread.start();
			}
			if (thread != null)
				thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// Always save results.
			for (AbstractScanner webSiteScanner : scanners) {
				saveResults(webSiteScanner);
			}
		}
	}
	
	public AbstractScanner createScanner(String url) {
		// Create the objects necessary to instantiate a scanner.
		RequestTimer timer = new RequestTimer();
		WebSite webSite = new WebSite(sqlHelper, url);
		webSite.setName(WebStringUtils.determineWebSiteNameFromUrl(url));
		WebPage homePage = webSite.createWebPage(url, WebPage.TYPE_AGGREGATE);
		// Create the scanner.
		AbstractScanner webSiteScanner = ScannerFactory.createScanner(timer, webSite, homePage); 
		
		return webSiteScanner;
	}
	
	
	/**
	 * Writes all company data that the given {@link #AbstractScanner} currently holds 
	 * to a file. 
	 * 
	 * Also saves all failed scans and pages that have not yet been scanned to internal
	 * database.
	 * 
	 * @param scanner - the {@link AbstractScanner} of which the results should be saved.
	 */
	public void saveResults(AbstractScanner scanner) {
		// Get the data objects from the scanner.
		WebSite webSite = scanner.getWebSite();
		List<Company> companies = scanner.getCompanies();
		// Determine the name of the file to which the results will be written.
		String fileName = "webshopsOn" + WebStringUtils.capitalize(webSite.getName()) + ".txt";
		Path filePath = Paths.get("C:", "Temp", fileName);
		
		// Use a writer with append = true, so previous results will not be overwritten.
		try (FileWriter fileWriter = new FileWriter(filePath.toFile(), true);
				BufferedWriter writer = new BufferedWriter(fileWriter)) {
			
			// Write the data of each company to a separate line in the file.
			for (Company company : companies) {
				writer.write(company.toString());
				writer.newLine();
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		
		if (shouldConvertToCsv) {
			IOUtils.convertResultFileToCsv(filePath.toString());
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

	public void setShouldConvertToCsv(boolean shouldConvertToCsv) {
		this.shouldConvertToCsv = shouldConvertToCsv;
	}

	public static void main(String[] args) {
		
		SearchCoordinator coordinator = new SearchCoordinator();

		
//		AbstractWebSiteScanner scanner = new FlavouritesWebSiteScanner(coordinator.sqlHelper, "https://www.flavourites.nl/");
//		scanner.run();
		
//		AbstractWebSiteScanner ecoScanner = new EcoGoodiesWebSiteScanner(connector, "https://ecogoodies.nl/");
//		AbstractWebSiteScanner bedrijvenScanner = new BedrijvenPaginaWebSiteScanner(connector, "https://www.bedrijvenpagina.nl/");

//		Concurrency is evil...as long as Connector is not thread-safe.
//		Thread threadEcoGoodies = new 
//		ecoScanner.run();
		
//		bedrijvenScanner.run();

//		SearchCoordinator coordinator = new SearchCoordinator();
//		List<String> webSites = new ArrayList<>();
//		webSites.add("https://www.gastvrij-rotterdam.nl/exposanten/exposanten");
//		coordinator.scanWebSites(webSites);

		coordinator.setShouldConvertToCsv(true);
		List<String> webSites = new ArrayList<>();
		webSites.add("https://altruisto.com/partners/");
		coordinator.scanWebSites(webSites);
	}
	
}
