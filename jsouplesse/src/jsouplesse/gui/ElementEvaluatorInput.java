package jsouplesse.gui;

public class ElementEvaluatorInput {

	private String tag;
	
	private String attribute;
	
	private boolean shouldFetchWebPage;
	
	private boolean shouldFetchTextInLink;

	private boolean shouldHuntForLogo;
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public boolean getShouldFetchWebPage() {
		return shouldFetchWebPage;
	}

	public void setShouldFetchWebPage(boolean shouldFetchWebPage) {
		this.shouldFetchWebPage = shouldFetchWebPage;
	}

	public boolean getShouldFetchTextInLink() {
		return shouldFetchTextInLink;
	}

	public void setShouldFetchTextInLink(boolean shouldFetchTextInLink) {
		this.shouldFetchTextInLink = shouldFetchTextInLink;
	}

	public boolean getShouldHuntForLogo() {
		return shouldHuntForLogo;
	}

	public void setShouldHuntForLogo(boolean shouldHuntForLogo) {
		this.shouldHuntForLogo = shouldHuntForLogo;
	}
}
