
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
com.dotmarketing.beans.Host,
com.dotmarketing.business.UserFactoryLiferayImpl,
com.dotmarketing.business.web.HostWebAPI,
com.dotmarketing.business.web.WebAPILocator,
com.dotmarketing.portlets.folders.business.FolderAPI,
com.dotmarketing.business.UserFactoryLiferayImpl,
com.dotmarketing.portlets.htmlpages.business.HTMLPageAPI,
com.dotmarketing.portlets.htmlpages.business.HTMLPageAPIImpl,
com.dotmarketing.portlets.htmlpages.model.HTMLPage,
com.liferay.portal.model.User,
com.dotmarketing.portlets.folders.model.Folder,
com.dotmarketing.portlets.containers.model.Container,
com.dotmarketing.portlets.containers.business.ContainerAPI,
com.dotmarketing.portlets.containers.business.ContainerAPIImpl,
com.dotmarketing.portlets.contentlet.model.Contentlet,
com.dotmarketing.portlets.contentlet.business.ContentletAPI,
com.dotcms.content.elasticsearch.business.ESContentletAPIImpl,
com.dotmarketing.portlets.templates.model.Template,
com.dotmarketing.portlets.templates.business.TemplateAPI,
com.dotmarketing.portlets.templates.business.TemplateAPIImpl,
com.dotmarketing.portlets.htmlpages.factories.HTMLPageFactory,
com.dotmarketing.factories.InodeFactory,
com.dotmarketing.business.APILocator,
com.dotmarketing.factories.PublishFactory,
com.dotmarketing.exception.WebAssetException,
com.dotmarketing.util.InodeUtils,
org.apache.commons.lang.StringUtils,
com.dotmarketing.portlets.structure.model.Structure
"%>

<%!

String PROD_DOT_CMS_DB = "jdbc/dotCMSPool";


%>

<form action="" method="GET">

	<%
		String searchValues = request.getParameter("searchValues");
		String startValue = "wsdev.,wsqa.,wsprod.";
		if (searchValues != null) {
			startValue = searchValues;
		} 
	%>
	<ul>
		<li style="list-style: none">WYSIWYG contains : <input type="text" name="searchValues" value="<%=startValue%>"> <span style="font-size: 12px; color: #7D7C7C;">comma separated values</span></li>
		<li style="list-style: none; padding: 10px 0px 0px 140px;"><input type="submit" value="Find"/></li>
	</ul>
<%

boolean on = true;
if (on) {
	
	
	
	if (searchValues != null) {
	
		String[] searchValueArray = searchValues.split(",");
		
		Context initCtx = null;
		Context envCtx = null;
		DataSource dotcmsProdDB = null;
		Connection prodConnection = null;
		Statement selectStmt = null;
		
		ContentletAPI conApi = APILocator.getContentletAPI();
		UserFactoryLiferayImpl userFactory = new UserFactoryLiferayImpl();
		User user = userFactory.loadByUserByEmail("stephen.blalock@mlh.org");
		
		List<String> columnNames = new ArrayList<String>();
		
		int textAreaColumnCountStart = 1;
		int textAreaColumnCountEnd = 25;
		//max end of 25
		int safetyCount = 0;
		
		
		for (int i=textAreaColumnCountStart; i<=textAreaColumnCountEnd; i++) {
			columnNames.add("text_area" + i);
		}
		
		//String[] absoluteSubDomains = {"wsdev.", "wsqa.", "wsprod."};
		
		try {
			initCtx = new InitialContext();
			envCtx = (Context) initCtx.lookup("java:comp/env");
			
			dotcmsProdDB = (DataSource) envCtx.lookup(PROD_DOT_CMS_DB);
			prodConnection = dotcmsProdDB.getConnection();
			selectStmt = prodConnection.createStatement();
			
			Map<String, Integer> subDomainMapCount = new HashMap<String, Integer>();
			Map<String, Integer> contentletMapCount = new HashMap<String, Integer>();
			List<String> pagesToFix = new ArrayList<String>();		
			
			%>
			<ul>
			<%
			
			String backGroundColor = "#333333";
			boolean backGroundAlternate = false;
			
			for (String findStr : searchValueArray) {
				
				subDomainMapCount.put(findStr, new Integer(0));
				contentletMapCount.put(findStr, new Integer(0));
				
				
				for (String colName : columnNames) {
					
					String contentletSql = "SELECT inode, " + colName + " FROM contentlet WHERE " + colName + " LIKE '%" + findStr + "%' AND live=1 AND working=1";
					
					ResultSet rs = selectStmt.executeQuery(contentletSql);
					
					while (rs.next()) {
						
						contentletMapCount.put(findStr, contentletMapCount.get(findStr)+1);
						
						String contentletInode = rs.getString("inode");
						//String originalText = rs.getString(colName);
						
						
						
						
						Contentlet contentlet = conApi.find(contentletInode, user, false);
						List<Map<String, Object>> references = conApi.getContentletReferences(contentlet, user, false);
						
						String title = contentlet.getTitle();
						Structure contStructure = contentlet.getStructure();
						String structName = contStructure.getName();
						//String strucVarName = contStructure.getVelocityVarName();
						if (backGroundAlternate) {
							backGroundColor = "#FFFFFF";
							backGroundAlternate = false;
							
						} else {
							backGroundColor = "#BAB4B4";
							backGroundAlternate = true;
						}
						%>
							<li style="background-color: <%=backGroundColor%>; padding: 5px;">
								<%=title%> &nbsp;&nbsp;&nbsp; : <span style="font-size: 12px; color: #7D7C7C;">(<%=structName%>  - <%= contentletInode%>)</span> contains <span style="font-size: 12px; font-weight: bold; color: #515000;"><%=findStr%></span><br/>
						<%
						
						for (Map<String, Object> ref : references) {
							try {
								HTMLPage htmlPage = (HTMLPage)ref.get("page");					
								%>
									&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b><%= htmlPage.getURI()%></b><br/>
								<%
							
							} catch (Exception ex) {
								Logger.debug(this.getClass(), "the reference has a null page");
							}
						}
						%>
							</li>
						<%
						
					}
				}
			}
			%>
			</ul>
			<%
			
			%>
				<br/>--------------------------------------------------------------------<br/>
				Contentlets with absolute references to other environments
				<br/>--------------------------------------------------------------------<br/>
			<%
			for (String key : contentletMapCount.keySet()) {
				%>
					<%=key %>[<%=contentletMapCount.get(key)%>]<br/>
				<%	
			}
			
			
		} catch (Exception e){
			Logger.error(this.getClass(), "Caught Exception while trying to map pages identifier: " + e.getMessage(), e);
		} finally {
			try {
				if (selectStmt != null) {
					selectStmt.close();
				}
			} catch (SQLException sqle) {
				
			}
			try {
				if (prodConnection != null) {
					prodConnection.close();
				}
			} catch (SQLException sqle) {
				
			}
		}
	}
} else {
	Logger.warn(this.getClass(), "set on variable to true to enable script");
	%>
		set <b>on</b> variable to true to enable script
	<%
}

%>
</form>

