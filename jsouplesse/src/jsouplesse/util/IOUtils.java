package jsouplesse.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Because apparently CSV format is required.
 */
public class IOUtils {
	
	public static boolean convertResultFileToCsv(String filePath) {
		
//		int columnIndex = 0;
		
		Path file = Paths.get(filePath);
		
		// Replace the file extension of the original file with one fitting a CSV file.
		Path csvFile = Paths.get(filePath.substring(0, filePath.indexOf(".")) + ".csv");
		
		try (BufferedReader reader = Files.newBufferedReader(file);
				BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
			
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				
				if (line.startsWith("Bedrijf")) {
					// For now, just discard the lines on which the company name is written.
					continue;
				}
				
				writer.write(line + ";");
				writer.newLine();
				
//				columnIndex++;
				// Limit to five columns per line.
//				if (columnIndex == 5) {
//					writer.newLine();
//					columnIndex = 0;
//				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * To fully understand this method, read javadoc for the methods 
	 * {@link Files#notExists(Path, java.nio.file.LinkOption...)}
	 * and {@link Files#exists(Path, java.nio.file.LinkOption...)}.
	 */
	public static boolean fileNotExistsAndIsWritable(Path filePath) {
		// Determine whether the given path is valid and if the file is writable.
		if (!Files.notExists(filePath) && !Files.exists(filePath)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Determines whether the link is a relative link, and if so, attempts to reconstruct a 
	 * direct URL.
	 */
	public static String determineDirectUrlFromRelative(String relativeUrl, String parentUrl) {
		
		if (relativeUrl.contains(parentUrl)) {
			// The URL is relative. Strip away the base URL of the aggregate web site.
			// Find the first slash after the base URL.
			int index = relativeUrl.indexOf("/", parentUrl.length() - 1);
			// Select everything to the right of the found slash. 
			String directUrl = relativeUrl.substring(index + 1);
			
			int indexOfWWW = directUrl.indexOf("www.");
			int indexOfHttp = directUrl.indexOf("http");

			if (indexOfWWW == -1 && indexOfHttp == -1)
				// Return the full relative URL to avoid out-of-bounds exception.
				return relativeUrl;
			else if (indexOfWWW != -1)
				directUrl = directUrl.substring(indexOfWWW);
			else
				directUrl = directUrl.substring(indexOfHttp);
		}
		
		return relativeUrl;
	}
	
	public static String determineFileExtension(String file) {
		int lastPeriod = file.lastIndexOf(".");
		if (lastPeriod > -1)
			return file.substring(lastPeriod + 1);
		else
			return file;
	}

}
