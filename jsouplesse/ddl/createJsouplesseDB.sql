-- Creates the tables for the internal database of scans that have 
-- not (yet) been successfully completed.
-- WebSite
create table WebSite (
	webSiteId int not null,
	name varchar (80),
	homePageUrl varchar (80) not null,

	primary key (webSiteId)
);

-- WebPage
create table WebPage (
	webPageId int not null,
	webSiteId int not null,
	webPageTypeId int not null,
	pageUrl varchar (80) not null,
	
	foreign key (webSiteId) references WebSite (webSiteId),
	primary key (webPageId)
);

-- FailedScan
create table FailedScan (
	failedScanId int not null,
	webPageId int not null,
	reasonFail varchar (80),
	exceptionType varchar (80),
	exceptionMessage varchar (120),
	
	foreign key (webPageId) references WebPage (webPageId),
	primary key (failedScanId)
);