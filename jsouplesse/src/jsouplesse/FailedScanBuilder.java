package jsouplesse;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.FailedScan;
import jsouplesse.dataaccess.dao.WebPage;

/**
 * Builds a {@link FailedScan} for the given web page, which a scanner/scraper
 * was unable to successfully extract data from. 
 */
public class FailedScanBuilder {

	private SqlHelper sqlHelper;
	
	public FailedScanBuilder(SqlHelper sqlHelper) {
		this.sqlHelper = sqlHelper;
	}
	
	public void buildFailedScan(WebPage webPage, FailedScan.Reason reason) {
		buildFailedScan(webPage, reason, null);
	}
	
	public void buildFailedScan(WebPage webPage, FailedScan.Reason reason, Exception exception) {
		
		FailedScan fail = new FailedScan(sqlHelper);
		
		if (reason == null)
			reason = FailedScan.Reason.UNKNOWN;
		
		fail.setReason(reason);
		fail.fillException(exception);
		// TODO: some fancy analytics here.
		webPage.setFailedScan(fail);
	}
	
}
