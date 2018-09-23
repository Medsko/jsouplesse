package jsouplesse.dataaccess.dao;

import java.sql.SQLException;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.SuperDao;

/**
 * Failure is not the end. A defeat is only a defeat, if nothing is learned from it 
 * (i.e. if the resulting data is not analyzed to improve the existing algorithm).
 * Did you know that the Chinese character for 'failure' can also be read as
 * 'go fuck yourself, you new-age hippy scum'?
 * 
 * Anyway, this data object is intended to store as much potentially informative
 * data concerning a failed scan as possible.
 */
public class FailedScan extends SuperDao {

	// Initialization of the values used for read/write actions.
	{
		tableName = "FailedScan";
	}

	public enum Reason {
		HTTP_STATUS_403("The server returned a 'Forbidden' response."),
		CONTENT_NOT_RETRIEVED("The content of the page could neither be retrieved "
				+ "as DOM document, nor as raw HTML."),
		BAD_RESULT("The result of the scan was invalid."),
		INVALID_URL("A valid URL could not be determined."),
		NO_HREF_FOUND("No web shop URL found in selected element."),
		UNKNOWN("The cause of the failed scan could not be determined.");
		
		Reason(String description) {
			this.description = description;
		}
		
		public final String description;
	}
	
	private Integer failedScanId;

	private Reason reason;
	
	private String reasonFail;
	
	/** The type/subclass of Exception that was thrown, causing the scan to fail. */
	private String exceptionType;
	
	/** The message that was wrapped in the thrown exception. */
	private String exceptionMessage;
	
	/** The page on which the scan failed. */
	private Integer webPageId;

	public FailedScan(SqlHelper sqlHelper) {
		super(sqlHelper);
	}
		
	@Override
	protected void fillValues() {
		
		if (reasonFail == null && reason != null)
			reasonFail = reason.description;
		
		sqlHelper.setString("reasonFail", reasonFail);
		sqlHelper.setInt("webPageId", webPageId);
		sqlHelper.setString("exceptionType", exceptionType);
		sqlHelper.setString("exceptionMessage", exceptionMessage);
	}
	
	@Override
	protected void fillPrimaryKey() {
		sqlHelper.setPrimaryKeyInt("failedScanId", failedScanId);
	}

	@Override
	protected void fillFields() throws SQLException {
		failedScanId = sqlHelper.getInt("failedScanId");
		webPageId = sqlHelper.getInt("webPageId");
		reasonFail = sqlHelper.getString("reasonFail");
		exceptionType = sqlHelper.getString("exceptionType");
		exceptionMessage = sqlHelper.getString("exceptionMessage");
	}

	public Integer getFailedScanId() {
		return failedScanId;
	}

	public void setFailedScanId(Integer failedScanId) {
		this.failedScanId = failedScanId;
	}
	
	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

	public String getReasonFail() {
		return reasonFail;
	}

	public void setReasonFail(String reasonFail) {
		this.reasonFail = reasonFail;
	}
	
	public String getExceptionType() {
		return exceptionType;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}
	
	public void fillException(Exception exception) {
		
		if (exception == null)
			return;
		
		exceptionMessage = exception.getMessage();
		exceptionType = exception.getClass().getSimpleName();
	}

	public Integer getWebPageId() {
		return webPageId;
	}

	public void setWebPageId(Integer webPageId) {
		this.webPageId = webPageId;
	}
}
