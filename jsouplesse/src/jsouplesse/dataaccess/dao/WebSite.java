package jsouplesse.dataaccess.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.dataaccess.SuperDao;
import jsouplesse.util.FormatUtils;

/**
 * Represents a web site to which requests are being made. 
 * 
 */
public class WebSite extends SuperDao {

	// Initialization of the values used for read/write actions.
	{
		tableName = "WebSite";
	}
	
	/** Unique identifier i.e. primary key for this web site. */
	private Integer webSiteId;
	
	/** The name of this web site. */
	private String name;
	
	/** The URL of the home page of this web site. */
	private String homePageUrl;
	
	/** The time at which the last request was made to this web site. */
	@Deprecated
	private Calendar tsLastRequest;
	
	private List<WebPage> webPages = new ArrayList<>();
	
	@Deprecated
	private List<WebPage> listPages = new ArrayList<>();
	
	@Deprecated
	private List<WebPage> detailPages = new ArrayList<>();
	

	/** Constructor with mandatory home page URL argument. */
	public WebSite(SqlHelper sqlHelper, String homePageUrl) {
		super(sqlHelper);
		this.homePageUrl = homePageUrl;
	}
	
	@Override
	protected void fillValues() {		
		sqlHelper.setString("name", name);
		sqlHelper.setString("homePageUrl", homePageUrl);
	}
	
	@Override
	protected void fillPrimaryKey() {
		sqlHelper.setPrimaryKeyInt("webSiteId", webSiteId);
	}

	@Override
	protected void fillFields() throws SQLException {
		webSiteId = sqlHelper.getInt("webSiteId");
		name = sqlHelper.getString("name");
		homePageUrl = sqlHelper.getString("homePageUrl");
	}
	
	/**
	 * Creates a new {@link WebPage} which is added to the list of web pages
	 * for this web site, then returning the resulting object.
	 * 
	 * Deprecated, for hopefully obvious reasons.
	 */
	@Deprecated
	public WebPage createWebPage(String pageUrl, int webPageTypeId) {
		WebPage webPage = new WebPage(sqlHelper, pageUrl);
		webPage.setWebPageTypeId(webPageTypeId);
		webPages.add(webPage);
		return webPage;
	}
	
	// Getters and setters
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHomePageUrl() {
		return homePageUrl;
	}

	public void setHomePageUrl(String homePageUrl) {
		this.homePageUrl = homePageUrl;
	}

	public Integer getWebSiteId() {
		return webSiteId;
	}

	public void setWebSiteId(Integer webSiteId) {
		this.webSiteId = webSiteId;
	}

	public Calendar getTsLastRequest() {
		return tsLastRequest;
	}

	public void setTsLastRequest(Calendar tsLastRequest) {
		this.tsLastRequest = tsLastRequest;
	}

	public List<WebPage> getListPages() {
		return listPages;
	}
	
	public List<WebPage> getDetailPages() {
		return detailPages;
	}

	public List<WebPage> getWebPages() {
		return webPages;
	}
	
	public SqlHelper getSqlHelper() {
		return sqlHelper;
	}
	
	@Override
	public String toString() {
		
		String webSiteString = "Web site: " + name + ", url: " + homePageUrl;
		
		if (tsLastRequest != null) {
			webSiteString += ", last request made at: " + FormatUtils.calendarToDateTime(tsLastRequest);
		}
		
		return webSiteString;
	}
}
