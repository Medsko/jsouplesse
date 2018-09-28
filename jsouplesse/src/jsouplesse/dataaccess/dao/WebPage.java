package jsouplesse.dataaccess.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.SuperDao;

/**
 * Represents a page on a web site.
 */
public class WebPage extends SuperDao {

	// Initialization of the values used for read/write actions.
	{
		tableName = "WebPage";
	}
	
	public static final int TYPE_AGGREGATE = 1;
	public static final int TYPE_LIST = 2;
	public static final int TYPE_DETAIL = 3;
	public static final int TYPE_WEB_SHOP = 4;
	public static final int TYPE_OTHER = 5;
	
	/** FK to the web site from which this web page was found. */
	private Integer webSiteId;
	
	/** The unique identifier for this web page. */
	private Integer webPageId;
	
	/** The type of web page: list, detail or web shop. */
	private Integer webPageTypeId;
	
	/** The name of the web shop. */
	private String webShopName;
	
	/** URL of this page. */
	private String pageUrl;
	
	/** The URL of the page on which the link to this page was found. */
	private String parentUrl;
	
	/** The contents of the web page as an HTML document. */
	private Document pageContents;
	
	/** The contents of the web page as plain text. */
	private String rawPageContents;
		
	/** Holds information pertaining to a failed scan of the web page. */
	private FailedScan failedScan;
	
	private List<FailedScan> failedScans;
	
	public WebPage(SqlHelper sqlHelper, String pageUrl) {
		super(sqlHelper);
		this.pageUrl = pageUrl;
		failedScans = new ArrayList<>();
	}
	
	@Override
	protected void fillValues() {
		sqlHelper.setInt("webPageId", webPageId);
		sqlHelper.setInt("webSiteId", webSiteId);
		sqlHelper.setInt("webPageTypeId", webPageTypeId);
		sqlHelper.setString("pageUrl", pageUrl);
	}
	
	@Override
	protected void fillPrimaryKey() {
		sqlHelper.setPrimaryKeyInt("webPageId", webPageId);
	}
	
	@Override
	protected void fillFields() throws SQLException {
		webPageId = sqlHelper.getInt("webPageId");
		webSiteId = sqlHelper.getInt("webSiteId");
		webPageTypeId = sqlHelper.getInt("webPageTypeId");
		pageUrl = sqlHelper.getString("pageUrl");
	}
	
	/**
	 * Checks whether the contents of the web page have been retrieved.
	 * @return {@code true} if the web page contents are present, either as 
	 * {@link Document} or as raw text, {@code false} otherwise.
	 */
	public boolean haveContentsBeenRetrieved() {
		return pageContents != null || rawPageContents != null;
	}
	
	/**
	 * Indicates whether the scan of this web page was successful.
	 */
	public Boolean getWasScanSuccessful() {
		return failedScan == null && failedScans.isEmpty();
	}
	
	public Integer getWebSiteId() {
		return webSiteId;
	}

	public void setWebSiteId(Integer webSiteId) {
		this.webSiteId = webSiteId;
	}
	
	public Integer getWebPageId() {
		return webPageId;
	}

	public void setWebPageId(Integer webPageId) {
		this.webPageId = webPageId;
	}

	public Document getPageContents() {
		return pageContents;
	}

	public void setPageContents(Document pageContents) {
		this.pageContents = pageContents;
	}

	public String getRawPageContents() {
		return rawPageContents;
	}

	public void setRawPageContents(String rawPageContents) {
		this.rawPageContents = rawPageContents;
	}
	
	public Integer getWebPageTypeId() {
		return webPageTypeId;
	}

	public void setWebPageTypeId(Integer webPageTypeId) {
		this.webPageTypeId = webPageTypeId;
	}

	public String getWebShopName() {
		return webShopName;
	}

	public void setWebShopName(String webShopName) {
		this.webShopName = webShopName;
	}

	public String getPageUrl() {
		return pageUrl;
	}
	
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public FailedScan getFailedScan() {
		return failedScan;
	}

	public void setFailedScan(FailedScan failedScan) {
		this.failedScan = failedScan;
	}
}
