package jsouplesse.dataaccess.processing;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sqlite.SQLiteException;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.dao.FailedScan;
import jsouplesse.dataaccess.dao.WebPage;
import jsouplesse.dataaccess.dao.WebSite;

public class WebSiteSaveBuffer {
	
	private WebSite webSite;
	
	private List<WebPage> webPageList;
	
	private List<FailedScan> failedScanList;
	
	private SqlHelper sqlHelper;
	
	private String resultMessage;
	
	public WebSiteSaveBuffer(SqlHelper sqlHelper) {
		this.sqlHelper = sqlHelper;
		webPageList = new ArrayList<>();
		failedScanList = new ArrayList<>();
	}
	
	public boolean saveWebSite() throws SQLException {
		
		sqlHelper.setAutoCommit(false);
		
		try {
			createIds();
		} catch (SQLiteException sqlex) {
			resultMessage = "Failed when creating id's for webSite: " + webSite.getName();
			System.out.println(resultMessage);
			sqlex.printStackTrace();
			return false;
		}
		
		try {
			webSite.insert();
		} catch (SQLiteException sqlex) {
			resultMessage = "Failed when saving web site: " + webSite.getName();
			System.out.println(resultMessage);
			sqlex.printStackTrace();
			return false;
		}
		
		for (WebPage webPage : webPageList) {
			try {
				webPage.insert();
			} catch (SQLiteException sqlex) {
				resultMessage = "Failed when saving web page: " + webPage.getPageUrl();
				System.out.println(resultMessage);
				sqlex.printStackTrace();
				return false;
			}
		}
		
		for (FailedScan failedScan : failedScanList) {
			try {
				failedScan.insert();
			} catch (SQLiteException sqlex) {
				resultMessage = "Failed when saving failed scan: " + failedScan.getFailedScanId();
				System.out.println(resultMessage);
				sqlex.printStackTrace();
				return false;
			}
		}
		// Everything inserted, without exception. Commit and log success message.
		sqlHelper.commit();
		resultMessage = "Successfully inserted data for web site: " + webSite.getName();
		System.out.println(resultMessage);
		
		return true;
	}
	
	private void createIds() throws SQLException {
		
		Integer availableWebSiteId = webSite.determineNewId();
		webSite.setWebSiteId(availableWebSiteId);
		Integer availableWebPageId = null;
		Integer availableFailedScanId = null;
		
		if (webPageList.size() != 0) {
			availableWebPageId = webPageList.get(0).determineNewId();
			availableFailedScanId = webPageList.get(0).getFailedScan().determineNewId();
		}
		
		for (WebPage webPage : webPageList) {
			
			webPage.setWebSiteId(availableWebSiteId);
			webPage.setWebPageId(availableWebPageId);
			// Add the FailedScan to the save list.
			FailedScan failedScan = webPage.getFailedScan();
			failedScan.setFailedScanId(availableFailedScanId++);
			failedScan.setWebPageId(availableWebPageId++);
			failedScanList.add(failedScan);
		}
	}
	
	public void addWebSite(WebSite webSite) {		
		addWebPages(webSite.getWebPages());
		this.webSite = webSite;
	}
	
	private void addWebPages(List<WebPage> webPages) {

		for (WebPage webPage: webPages) {
			if (webPage.getWasScanSuccessful())
				// We only want to save web pages that have not yet been successfully 
				// scanned. Results of successful scans are written to file.
				continue;			
			webPageList.add(webPage);
		}
	}

	public String getResultMessage() {
		return resultMessage;
	}
}
