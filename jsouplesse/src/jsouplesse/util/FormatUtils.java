package jsouplesse.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FormatUtils {

	public static SimpleDateFormat dateTimeSecondSpecFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	
	// Classic utility class constructor, as unavailable as your dad is emotionally.
	private FormatUtils() {};

	public static String calendarToDateTime(Calendar calendar) {
		Date date = calendar.getTime();
		return dateTimeSecondSpecFormat.format(date);
	}
	
	/**
	 * Formats the given element so it will be better suited for print (i.e. for logging).
	 * 
	 * If the provided element is null, an empty String is returned. 
	 * 
	 * @return the formatted element.
	 */
	public static String formatElementForPrint(Element element) {
		// Initialize to empty String and perform null check. 
		String toString = "";
		if (element == null)
			return toString;
		
		HashSet<String> tagsSet = new HashSet<>();
		
		// For each level, test each child node for uniqueness.
		for (Element child : element.children()) {
			
		}
		
		
		return toString;
	}
	
	
	public static StringBuilder formatElements(Elements elements) {
		StringBuilder formatted = new StringBuilder();
		
		
		return formatted;
	}
	
	
}
