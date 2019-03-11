package jsouplesse.scraping;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jsouplesse.util.CrappyLogger;
import jsouplesse.util.IOUtils;

/**
 * Hunts down any logo's (i.e. images that could very well be logo's) it encounters in the given
 * HTML element. If any are encountered, these are saved to a folder specific to the web site
 * they are found on.
 */
public class LogoHunter {
	
	private CrappyLogger logger;
	
	private Path outputFolder;
	
	private ContentFetcher fetcher;
	
	public LogoHunter(CrappyLogger logger, ContentFetcher fetcher) {
		this.logger = logger;
		this.fetcher = fetcher;
	}
	
	public boolean hunt(Element huntingGrounds, String webSiteName) {
		// Select all images.
		Elements images = huntingGrounds.select("img");
		
		// Of all images, select those with a class attribute containing 'logo'.
		Elements classLogoImages = images.select("[class~=.*logo.*]");
		if (classLogoImages.size() > 0) {
			// Found at least one possible logo image. Fetch the images and return.
			return fetchAndWriteImages(classLogoImages, webSiteName);
		}
		
		// Of all images, select those with a class attribute containing 'logo'.
		Elements sourceLogoImages = images.select("[src~=.*logo.*]");
		if (sourceLogoImages.size() > 0)
			return fetchAndWriteImages(sourceLogoImages, webSiteName);
		
		return true;
	}
	
	private boolean fetchAndWriteImages(Elements images, String webSiteName) {
		// Set the path to the output folder, check if it exists and create if it doesn't yet.
		outputFolder = Paths.get("C:", "jsouplesse", "logos", webSiteName);
		if (IOUtils.fileNotExistsAndIsWritable(outputFolder)) {
			try {
				Files.createDirectories(outputFolder);
			} catch (IOException e) {
				e.printStackTrace();
				logger.log("Failed to create logo output directory: " + outputFolder);
			}
		}
		
		if (images.size() >= 10) {
			Elements filteredImages = images.select("[src~=.*" + webSiteName + ".*]");
			if (filteredImages.size() > 0)
				// The selective search yielded results. Swap the wider select for this one.
				images = filteredImages;
		}
		
		int i = 0;
		
		for (Element image: images) {
			
			String imageUrl = image.attr("src");
			
			if (!fetcher.fetchImage(image.attr("src"))) {
				logger.log("fetchAndWriteImages - failed to fetch image at: " + imageUrl);
				return false;
			}
			// Construct the file name.
			String fileName = webSiteName;
			if (i++ > 0)
				fileName += i;
			
			String fileExtension = IOUtils.determineFileExtension(imageUrl);
			if (fileExtension == null)
				// Could not determine the file extension.
				continue;
			
			fileName += "." + fileExtension;
			Path imageFile = outputFolder.resolve(fileName);
			
			try (FileOutputStream out = new FileOutputStream(imageFile.toFile());) {
				out.write(fetcher.getFetchedImage());
			} catch (IOException e) {
				logger.log("fetchAndWriteImages - failed to write image " + fileName + " to file");
				e.printStackTrace();
				continue;
			}
		}
		
		return true;
	}
}
