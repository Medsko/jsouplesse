package jsouplesse.scraping;

import org.jsoup.nodes.Element;

/**
 * Represents a step made in the crawling process.
 */
public class CrawlStep {

	private Element firstSelectedElement;
	
	private String selector;
	
	private String fetchedLink;

	@Override
	public String toString() {
		String toString = "";
		if (selector != null) {
			toString += "Selector '" + selector + "' ";
			if (firstSelectedElement != null)
				toString += "selected element: " + System.lineSeparator() 
					+ firstSelectedElement.toString();
			else
				toString += "did not find a matching element!";
			toString += System.lineSeparator();
		}
		if (fetchedLink != null)
			toString += "The web page at '" + fetchedLink + "' were successfully fetched."
				+ System.lineSeparator();
		return toString;
	}
	
	public Element getSelectedElements() {
		return firstSelectedElement;
	}

	public void setSelectedElements(Element selectedElements) {
		this.firstSelectedElement = selectedElements;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getFetchedLink() {
		return fetchedLink;
	}

	public void setFetchedLink(String fetchedLink) {
		this.fetchedLink = fetchedLink;
	}
}
