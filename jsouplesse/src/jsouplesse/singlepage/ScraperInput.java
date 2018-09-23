package jsouplesse.singlepage;

/**
 * Simple value object to transfer the input necessary to create a
 * {@link CustomScraper} from the user to the {@link ScrapeService}. 
 */
public class ScraperInput {

	public String pageUrl;
	
	public String tagName;
	
	public String attribute;
}
