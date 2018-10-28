package jsouplesse.util;

import java.util.Optional;
import java.util.regex.Pattern;

public class WebStringUtils {

	public final static String PATH_TO_APP_FILE_DIRECTORY = "C:/jsouplesse";
	
	/** Regular expression for a URL. */
	public final static String INCLUSIVE_URL_REGEX = "(http[s]?://)?(www\\.)?([a-zA-Z\\d\\-]+\\.)*([a-zA-Z\\d\\-]+/)*([a-zA-Z\\d\\-]+/?)";
	
	public final static String URL_IN_TEXT_REGEX = "(http[s]?://)?www\\.[a-zA-Z\\d\\-]*\\.[a-zA-Z\\d\\-]{2,3}";
	
	public final static String URL_WITH_PARAM_REGEX = "(http[s]?://)?(www\\.)?[a-zA-Z\\d\\-]+\\.[a-zA-Z]{2,3}(/[\\-a-zA-Z\\.\\?]*)*";
	
	public final static String URL_STRING_REGEX = "(http[s]?://)?(www\\.)?[a-zA-Z\\d-]+\\.[a-zA-Z]{2,3}(/[a-zA-Z\\.]*)*";
	
	public final static String[] SUPPORTED_COUNTRY_CODES = new String[] {"nl", "be", "de", "com", "co.uk"}; 
	
	
	/** Regular expression for a Dutch (mobile) phone number. */
	public static Pattern PHONE_NR_REGEX = Pattern.compile(
			"((31\\s?\\d\\s?\\d{0,2})|" // Starts with a country code followed by area code...
			+ "\\d{2,3})" // ...or starts immediately with the area code...
			+ "(\\d{2,3}\\s?){2,3}"); // ...followed by 6-8 numbers, which might be separated by whitespace.
	
	/** Regular expression for a link that opens a mail service with a certain mail address. */
	public static Pattern EMAIL_URI = Pattern.compile("mailto:\\w*@\\w*\\.[a-zA-Z]{2,3}");
	
	/** Regular expression for an email address. */
	public static Pattern EMAIL_ADDRESS = Pattern.compile("\\w*@\\w*\\.[a-zA-Z]{2,3}");
	
	
	// You know the drill.
	private WebStringUtils() {}
	
	/**
	 * Treee-de-tee ta taa-de-ta... (Curtis Mayfield 4evah)
	 * Returns a {@link String} that represents the parent directory of the given URL.
	 * If the given URL is already that of home page, the returned {@link Optional} is empty.
	 */
	public static String moveOnUp(String url) {
		
		if (url.endsWith("/"))
			url = url.substring(0, url.lastIndexOf("/"));
		
		String parentUrl = url.substring(0, url.lastIndexOf("/"));
		
		if (parentUrl.matches(INCLUSIVE_URL_REGEX))
			return parentUrl;
		else
			return url;
	}
	
	/**
	 * Removes a search filter and any of its arguments from the given URL. If the given URL does
	 * not contains such a clause, the URL is returned unchanged. 
	 */
	public static String removeFilterFromUrlIfPresent(String url) {
		
		String afterLastSlash = url.substring(url.lastIndexOf("/") + 1);
		
		if (afterLastSlash.contains("filter"))
			url = url.substring(0, url.lastIndexOf("/"));
	
		return url;
	}
	
	/**
	 * Attempts to extract the external link from the given page URL that was found on
	 * the web site represented by the parent URL.
	 * 
	 * @return and {@link Optional} containing the absolute external link, or an empty 
	 * {@link Optional} if no viable external link could be determined.
	 */
	public static Optional<String> extractExternalLink(String pageUrl, String parentUrl) {
		
		Optional<String> externalLink = Optional.empty();
		
		String parentBase = determineBaseUrl(parentUrl);
		
		if (!pageUrl.contains(parentBase)) {
			return externalLink;
		}
		
		int indexOfRelativity = pageUrl.indexOf(parentBase) + parentBase.length();
		
		String relativeUrl = pageUrl.substring(indexOfRelativity);
		
		for (;;) {
			// Strip away the path parts until encountering 'www.' or 'http'.
			int indexPathEntry = relativeUrl.indexOf("/");
			
			if (indexPathEntry == -1) {
				// No more path parts could be found, meaning no viable URL
				// could be determined. Break and return false.
				break;
			}
			
			relativeUrl = relativeUrl.substring(indexPathEntry + 1);
			
			if (relativeUrl.startsWith("www") || relativeUrl.startsWith("http")) {
				externalLink = Optional.of(determineBaseUrl(relativeUrl));
				break;
			}
		}
		
		return externalLink;
	}
	
	/**
	 * Resolves the given URL against the URL of the parent page.
	 * If the page URL is not relative, it is returned without modifications.
	 * @param pageUrl - the possibly relative URL.
	 * @param parentUrl - the URL of the page the URL was found on.
	 */
	public static String resolveAgainstParent(String pageUrl, String parentUrl) {
		if (pageUrl.startsWith("/"))
			pageUrl = parentUrl + pageUrl;
		return pageUrl;
	}
	
	public static String formatPhoneNumber(String phoneNumber) {
		// Remove all whitespace, plus signs and hyphens.
		String formattedPhoneNumber = phoneNumber.replaceAll("[\\+\\s-]", "");
		
		// If the original phone number started with double zero, remove these zeroes.
		if (formattedPhoneNumber.startsWith("00"))
			formattedPhoneNumber = formattedPhoneNumber.substring(2);
		
		// If the remaining number contains only 9 numbers, prefix a zero.
		if (formattedPhoneNumber.length() == 9)
			formattedPhoneNumber = "0" + formattedPhoneNumber;
		
		if (formattedPhoneNumber.charAt(2) == '6') {
			
		}
		
		return formattedPhoneNumber;
	}

	/** 
	 * Determines and returns the web site name from the URL of one of its pages. 
	 * In 'https://www.google.com', the name of the web site would be 'google'. 
	 * 
	 * @param url - a valid URL.
	 * @return the name of the web site the URL points to.
	 */
	public static String determineWebSiteNameFromUrl(String url) {
		
		if (url.startsWith("http"))
			// Remove everything left of the first slash.
			url = url.substring(url.indexOf("/"));
		
		if (url.contains("www."))
			// Remove the "www." part.
			url = url.substring(url.indexOf(".") + 1);
		
		int index = url.indexOf(".");

		if (index != -1)
			url = url.substring(0, url.indexOf("."));

		String webSiteName = url.replace("/", "");
		
		if (isEmpty(webSiteName))
			return "unknown";
		else
			return webSiteName;
	}
	
	/**
	 * Determines the base URL from the given URL. 
	 */
	public static String determineBaseUrl(String url) {
		// Determine the index of the first slash after the first period.
		int firstSub = url.indexOf("/", url.indexOf("."));
		
		if (firstSub == -1)
			// There were no sub directories included in the URL.
			return url;
		else
			return url.substring(0, firstSub);
	}
	
	/** Appends a slash to the given String, unless it already ends with one. */
	public static String appendSlash(String url) {
		if (url.endsWith("/"))
			return url;
		else
			return url + "/";
	}
	
	/**
	 * Checks whether a String is either null or empty.
	 * @return {@code true} if the String is null or empty, {@code false} otherwise.
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	/**
	 * Null-safe method that capitalizes the first letter of the given String.
	 * @return the String with the first letter capitalized.
	 */
	public static String capitalize(String str) {
		// Check for null values.
		if (isEmpty(str))
			return str;
		else
			return str.substring(0, 1).toUpperCase() + str.substring(1);		
	}
}
