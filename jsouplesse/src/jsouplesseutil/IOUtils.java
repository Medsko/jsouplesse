package jsouplesseutil;

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
	
	public static boolean fileNotExistsAndIsWritable(Path filePath) {
		// Determine whether the given path is valid and if the file is writable.
		if (!Files.notExists(filePath) && !Files.exists(filePath)) {
			return false;
		}
		return true;
	}
}
