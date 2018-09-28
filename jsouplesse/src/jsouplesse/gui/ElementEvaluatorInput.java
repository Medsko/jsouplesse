package jsouplesse.gui;

public class ElementEvaluatorInput {

	private String tag;
	
	private String attribute;
	
	private boolean shouldFetchWebPage;

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
}
