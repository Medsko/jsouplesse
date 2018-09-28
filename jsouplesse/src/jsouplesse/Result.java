package jsouplesse;

/**
 * Holds the result of an operation.
 */
public class Result {

	private String title;
	
	private String message;
	
	public Result() {}
	
	public Result(String message) {
		this.message = message;
	}
	
	public Result(String message, String title) {
		this.message = message;
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
