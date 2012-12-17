
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
org.apache.commons.lang.StringUtils
"%>




<%

try {
	
	//String pageURI = "/home/examples/articles/article-detail.dot";
	String pageURI = request.getParameter("page-uri");
	
	
	//String targetHostLike = "gomolli";
	String targetHostLike = request.getParameter("host-name");
	
	
	%>
		<form method="GET" action="">
			Page URI : <input type="text" name="page-uri" value="<%= (pageURI != null ? pageURI : "") %>" size="100"/><br/><br/>
			Host : <input type="text" name="host-name" value="<%= (targetHostLike != null ? targetHostLike : "") %>"/><br/><br/>
			<input type="submit" name="submit-page-and-host" value="Show Pages"/><br/><br/><br/><br/>
	<%
	
	
	if (pageURI != null && targetHostLike != null) {
	
		HostWebAPI hostApi = WebAPILocator.getHostWebAPI();
		HTMLPageAPI htmlPageApi = new HTMLPageAPIImpl();
		ContentletAPI contentletApi = new ESContentletAPIImpl();
		ContainerAPI containerApi = new ContainerAPIImpl();
		TemplateAPI templateApi = new TemplateAPIImpl();
		
		UserFactoryLiferayImpl userFactory = new UserFactoryLiferayImpl();
		User user = userFactory.loadByUserByEmail("stephen.blalock@mlh.org");
		
		List<Host> hosts = hostApi.findAll(user, false);
		
		for (Host host : hosts) {
			String hostname = host.getHostname();
			if (hostname.indexOf(targetHostLike) > -1) {
				HTMLPage htmlPage = HTMLPageFactory.getWorkingHTMLPageByPath(pageURI, host);
				
				%>
				Host name like <b><%=targetHostLike %></b> and pageURI[<%=pageURI %>]<br/><br/>
				<%
				
				if (htmlPage != null) {
					Template template = htmlPageApi.getTemplateForWorkingHTMLPage(htmlPage);
					if (template != null) {
						List<Container> containers = templateApi.getContainersInTemplate(template, user, true);
						if (containers != null && containers.size() > 0) {
							
							for (Container container : containers) {
								List<Contentlet> contentlets = contentletApi.findPageContentlets(htmlPage.getIdentifier(), container.getIdentifier(), null, true, -1, user, true);
								if (contentlets != null) {
									
									for (Contentlet ct : contentlets) {											
										
										%>
											[<%=ct.getIdentifier()%>] - <%=ct.getTitle() %> <br/>
										<%
										
									}
								} else {
									Logger.error(this.getClass(), "contentlets are empty/null for pageURI[" + pageURI + "]");
								}
				
							}
						}
					}
				}
			}
		}
	
	} //end if not null parameters
	
	%>
		</form>
	<%
} catch (Exception e) {
	
}



%>
