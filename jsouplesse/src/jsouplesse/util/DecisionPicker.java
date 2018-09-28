package jsouplesse.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * I don't know what this class does yet, but it's gonna be mighty useful.
 */
public class DecisionPicker {

	public static void checkUrls(String filePath) {
	
		Path file = Paths.get(filePath);
		
		// Modify the original file name.
		Path outputFile = Paths.get(filePath.replace(".", "Confirmed."));

		int passedTest = 0;
		int failedTest = 0;
		ArrayList<String> failedUrls = new ArrayList<>();
		
		try (BufferedReader reader = Files.newBufferedReader(file);
				BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
			
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				
				if (line.startsWith("Bedrijf")) {
					// For now, just discard the lines on which the company name is written.
					continue;
				}

				Matcher matcher = Pattern.compile(WebStringUtils.INCLUSIVE_URL_REGEX).matcher(line);
				
//				Matcher matcher = Pattern.compile(WebStringUtils.URL_WITH_PARAM_REGEX).matcher(line);
				
				if (matcher.matches()) {
					passedTest++;
					writer.write(line);
					writer.newLine();
				} else {
					failedTest++;
					failedUrls.add(line);
				}
			}
			System.out.println("Number of urls that passed the test: " + passedTest);
			System.out.println("Number of urls that failed the test: " + failedTest);
			System.out.println("Urls that failed the test: ");
			
			for (String s : failedUrls) {
				System.out.println(s);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args ) {
		checkUrls("C:\\Temp\\webshopsOnGastvrij-rotterdam.txt");
	}
}
