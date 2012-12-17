<%--
Run from http://[env_sub_domain].lebonheur.org/mlh-migration/dot-cache-controller.jsp
 --%>

<%@ page import="java.util.*,
javax.sql.*,
java.sql.Statement,
java.sql.ResultSet,
java.sql.PreparedStatement,
java.sql.Connection,
java.sql.SQLException,
javax.naming.Context,
javax.naming.InitialContext,
com.dotmarketing.util.Logger,
java.sql.Connection,
java.sql.PreparedStatement,
java.sql.Statement,
javax.naming.Context,
javax.sql.DataSource,
com.dotmarketing.business.BlockDirectiveCacheObject,
com.dotmarketing.business.CacheLocator,
com.dotmarketing.business.DotCacheAdministrator,
com.dotmarketing.business.BlockDirectiveCache,
com.dotmarketing.business.DotJBCacheAdministratorImpl,
org.apache.commons.lang.StringUtils,
com.dotmarketing.business.BlockDirectiveCache,
com.dotmarketing.business.BlockDirectiveCacheImpl,
com.dotmarketing.business.BlockDirectiveCacheObject,
com.dotmarketing.business.CacheLocator,
com.dotmarketing.velocity.DotResourceCache,
com.dotmarketing.portlets.htmlpages.business.HTMLPageCache,
com.dotmarketing.business.UserFactoryLiferayImpl,
com.liferay.portal.model.User,
com.dotmarketing.portlets.htmlpages.model.HTMLPage,
com.dotmarketing.portlets.htmlpages.business.HTMLPageAPI,
com.dotmarketing.portlets.htmlpages.business.HTMLPageAPIImpl,
com.dotmarketing.business.IdentifierAPI,
com.dotmarketing.business.IdentifierAPIImpl,
com.dotmarketing.beans.Identifier,
com.dotmarketing.portlets.folders.business.FolderFactory,
com.dotmarketing.portlets.folders.model.Folder,
com.dotmarketing.business.APILocator,
com.dotmarketing.util.json.*
"%>

<html>
<head>
	
</head>
<body>

<%
String serverName = request.getServerName();

String acceptableServerRegex = "(cms\\.).*|(wsprod\\.).*|(wsqa\\.).*|(wsdev\\.).*|(nwsdev).*|(nwsdev2).*|(nwsqa).*|(nwsqa2).*|(swsapp1).*|(swsapp2).*|(nwsapp1).*|(nwsapp2).*";

if (serverName.matches(acceptableServerRegex)) {
	
	DotJBCacheAdministratorImpl dotCacheAdmin = (DotJBCacheAdministratorImpl)CacheLocator.getCacheAdministrator();
	
	HTMLPageCache htmlPageCache = CacheLocator.getHTMLPageCache();
	
	String cacheKeyToClear = request.getParameter("cache-key-to-clear");
	
	UserFactoryLiferayImpl userFactory = new UserFactoryLiferayImpl();
	User user = userFactory.loadByUserByEmail("stephen.blalock@mlh.org");
	
	HTMLPageAPI htmlPageApi = new HTMLPageAPIImpl();
	
	IdentifierAPI identApi = new IdentifierAPIImpl();

	
	BlockDirectiveCacheObject blockObjToClear = null;
	Object blockHTMLObj = null;
	if (cacheKeyToClear != null) {
	        
		//CacheLocator.getBlockDirectiveCache().remove(cacheKeyToClear);
		try {
			blockObjToClear = (BlockDirectiveCacheObject)CacheLocator.getBlockDirectiveCache().get(cacheKeyToClear);
			if (cacheKeyToClear != null && blockObjToClear != null) {
				CacheLocator.getBlockDirectiveCache().remove(cacheKeyToClear);
			}
		} catch (Exception e) {
			%>
			Caught Exception during dotCacheAdmin.get() <br/><%=e.getMessage()%><br/>
			<%
		}
	}
	
	
%>

<%

	//CacheLocator.getCacheAdministrator().put("first-name", "Stephen", "mlh-test-group");
	//CacheLocator.getCacheAdministrator().put("last-name", "Blalock", "mlh-test-group");
	String[] groupKeyNames = {"BlockDirectiveCache", "BlockDirectiveHTMLPageCache", "mlh-test-group"};
	int groupKeyCount = 0;
	for (String groupKeyName : groupKeyNames) {
		
		Map<String, List<String>> siteCacheKeys = new HashMap<String, List<String>>();
		
		Set<String> cacheKeys = dotCacheAdmin.getKeys(groupKeyName);
		
		if (cacheKeys != null && cacheKeys.size() > 0) {
			groupKeyCount++;
			/***********************************************************
			* Filter and separate keys by host into "siteCacheKeys"
			************************************************************/
			for (String key : cacheKeys) {
				if (key != null && key.length() > 0 && !"REGION NOT FOUND !!".equals(key)) {
					String hostKey = null;
					if (key.indexOf("gomolli") > -1) {
						hostKey = "gomolli";
					} else if (key.indexOf("lebonheur") > -1) {
						hostKey = "lebonheur";
					} else if (key.indexOf("methodistmd") > -1 || key.indexOf("methmd") > -1) {
						hostKey = "methodistmd";
					} else if (key.indexOf("methodisthealth") > -1) {
						hostKey = "methodisthealth";
					} else if (key.indexOf("coursesession") > -1) {
						hostKey = "course session";
					} else {
						hostKey = "otherKeys_" + groupKeyCount;
					}
					
					if (!siteCacheKeys.containsKey(hostKey)) {
						siteCacheKeys.put(hostKey, new ArrayList<String>());
					}
					siteCacheKeys.get(hostKey).add(key);
				}
			}
			%>-------------------------------- cache keys for <b><%=groupKeyName%></b> ---------------------------------------------------<br/>
			
			<%
			
			for (String key : siteCacheKeys.keySet()) {
				
				%>
				<h3 style="cursor:pointer;" class="site-key" id="header-<%=key%>"><%=key %> [<%=siteCacheKeys.get(key).size()%>]</h3> 
				<div id="wrapper-<%=key%>" style="display: none;">
					<table id="table-<%=key%>" style="border-collapse:collapse; margin-left: 10px; background-color: #B7B7B7;">
						<thead>
							<tr>
								<th style="border: 1px solid #000000;"><b>Key</b></th>
								<th style="border: 1px solid #000000;"><b>Created date</b></th>
								<th style="border: 1px solid #000000;"><b>TTL</b></th>
								<th style="border: 1px solid #000000;"><b>scheduled to de-cache date</b></th>
								<th style="border: 1px solid #000000;"></th>
							</tr>
						</thead>
						<tbody>
							<%
							/*********************************************
							* Iterate over the keys by hostKey
							**********************************************/
							for (String cacheKey : siteCacheKeys.get(key)) {
								BlockDirectiveCacheObject blockDirectiveObj = null;
								
								Date createDate = null;
								int ttl = 0;
								Date deCacheDate = null;
								String htmlInode = "";
								Identifier pageId = null;
								Identifier pageId2 = null;
								String pageIdStr = null;
								String pageUrl = "";
								Folder folder = null;
								boolean pastTimeToDeCache = false;
								try {
									blockDirectiveObj = (BlockDirectiveCacheObject)CacheLocator.getBlockDirectiveCache().get(cacheKey);
								
									if (blockDirectiveObj != null) {
										createDate = new Date(blockDirectiveObj.getCreated());
										ttl = blockDirectiveObj.getTtl();
										long deCacheMilli = blockDirectiveObj.getCreated() + (((long)ttl) * 1000);
										deCacheDate = new Date(deCacheMilli);
										
										Date now = new Date();
										
										pastTimeToDeCache = now.getTime() > deCacheDate.getTime();
										
										//String[] keyArray = cacheKey.split("-");
										//String[] htmlInodeArray = new String[5];
										
										//int j=4;
										//for (int i=keyArray.length-1; i>=(keyArray.length-5); i--) {
										//	htmlInodeArray[j] = keyArray[i];
										//	j--;
										//}
										//for (String blah : htmlInodeArray) {
										//	htmlInode += blah + "-";
										//}
										//htmlInode = htmlInode.substring(0, htmlInode.lastIndexOf("-"));
										//pageId = APILocator.getIdentifierAPI().findFromInode(htmlInode);
										//pageIdStr = APILocator.getIdentifierAPI().findFromInode(htmlInode).getInode();
					
										//if (pageId != null) {
											//pageIdStr = pageId.getIdentifier();
											//pageIdStr = pageId.toString();
											//HTMLPage htmlPage = htmlPageApi.loadWorkingPageById(pageIdStr, user, false);
											//HTMLPage htmlPage = htmlPageApi.loadWorkingPageById(htmlInode, user, true);
										//	HTMLPage htmlPage = htmlPageApi.loadLivePageById(pageIdStr, APILocator.getUserAPI().getSystemUser(), false);
											//folder = FolderFactory.getFolderByInode(htmlInode);
										//	if (htmlPage != null) {
												//pageUrl = htmlPage.getURI();
												
												
										//		Folder parentFolder = htmlPageApi.getParentFolder(htmlPage);
										//		pageUrl = (parentFolder == null ? "null parentFolder" : parentFolder.getPath());
												
												
										//	} else {
										//		pageUrl = "htmlPage is null";
										//	}
										//	pageIdStr += "...";
										//}
										
										
									}
								} catch (Exception e) {
									
								}
								String rowBackgroundColor = pastTimeToDeCache ? "#D38888" : "auto";
								%>
								<%
									//if (blockDirectiveObj != null) {
								%>
								<tr style="background-color: <%=rowBackgroundColor%>">
									<td style="border: 1px solid #000000; padding: 5px;"><%= cacheKey%> </td>
									<td style="border: 1px solid #000000; padding: 5px;"><%= (createDate != null ? createDate.toString() : "no createDate")%></td>
									<td style="border: 1px solid #000000; padding: 5px;"><%=(ttl > 0 ? ttl : "no ttl")%></td>
									<td style="border: 1px solid #000000; padding: 5px;"><%= (deCacheDate != null ? deCacheDate.toString() : "no deCacheDate")%></td>
									<td style="border: 1px solid #000000; padding: 5px;">
										<%
											if (blockDirectiveObj != null) {
										%>
												<a href="?cache-key-to-clear=<%=cacheKey%>">de-cache</a><%-- | <%=htmlInode%>
												 | pageId.getURI[<%=(pageId != null ? pageId.getURI() : "pageId null")%>] | pageIdStr[<%=pageIdStr%>] 
												 | folder[<%=(folder != null ? folder.getPath() : "pageId null") %>] | pageUrl[<%=pageUrl%>]
												 | pageId.getInode[<%=(pageId != null ? pageId.getInode() : "pageId null") %>]
												 | pageId.getIdentifier[<%=(pageId != null ? pageId.getIdentifier() : "pageId null") %>]
												  --%>
										<%
											}
										%>
									
									</td>
								</tr>
								<%
									//}
								%>
								<%
							}
							%>
						</tbody>
					</table>
				</div>
				<%
			}
		}
	}
	
%>

<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
	<script type="text/javascript" src=" https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js"></script>
	<script type="text/javascript">
		$(document).ready(function(){
			
			$('.site-key').click(function(){
				$('#wrapper-' + $(this).attr('id').split("-")[1]).slideToggle();
			});
			
		});
	</script>


<%
} else { //end of acceptableServerRegex check
%>

<h1><font face="arial">:P</font></h1>

<%
}
%>
</body>
</html>
