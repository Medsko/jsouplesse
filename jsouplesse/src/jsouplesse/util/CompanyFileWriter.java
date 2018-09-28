package jsouplesse.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import jsouplesse.dataaccess.dao.Company;
import jsouplesse.scraping.AbstractScanner;
import jsouplesse.scraping.ElementEvaluator;

public class CompanyFileWriter {

	private String resultMessage;
	
	private String fileName;
	
	/**
	 * Writes all company data that the given {@link #AbstractScanner} currently holds 
	 * to a file. 
	 * 
	 * Also saves all failed scans and pages that have not yet been scanned to internal
	 * database.
	 * 
	 * @param scanner - the {@link AbstractScanner} of which the results should be saved.
	 */
	public boolean saveResults(ElementEvaluator scraper, boolean shouldConvertToCsv) {
		// Get the data objects from the scanner.
		List<Company> companies = scraper.getCompanies();
		// Determine the name of the file to which the results will be written.
		fileName = "webshopsOn" 
				+ WebStringUtils.capitalize(scraper.getWebSiteName()) + ".txt";
		
		Path outputDirectory = Paths.get("C:", "jsouplesse", "companyData");
		
		try {
			if (IOUtils.fileNotExistsAndIsWritable(outputDirectory)) {
				Files.createDirectories(outputDirectory);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		
		Path filePath = outputDirectory.resolve(fileName);
		
		// Use a writer with append = true, so previous results will not be overwritten.
		try (BufferedWriter writer = Files.newBufferedWriter(filePath, 
				StandardOpenOption.APPEND, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			
			// Write the data of each company to a separate line in the file.
			for (Company company : companies) {
				writer.write(company.toString());
				writer.newLine();
			}
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
			resultMessage = "Failed while writing results to file.";
			return false;
		}
		
		if (shouldConvertToCsv
				&& !IOUtils.convertResultFileToCsv(filePath.toString())) {
			// Creating the CSV file failed. Set result message and return false.
			resultMessage = "Failed while converting results to CSV.";
			return false;
		}
		// Everything went smoothly. Set result message and return true;
		resultMessage = "Results were successfully written to file!";
		return true;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public String getFileName() {
		return fileName;
	}
}
