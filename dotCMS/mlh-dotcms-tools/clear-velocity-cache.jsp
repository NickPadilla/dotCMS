<%--
Run from http://[env_sub_domain].[site_name].org/mlh-dotcms-tools/clear-velocity-cache.jsp
 --%>

<%@ page import="java.util.*,
com.dotmarketing.business.CacheLocator,
com.dotmarketing.velocity.DotResourceCache
"%>

<%
	String accessKey = request.getParameter("accessKey");
	//should be the md5 hash of the text "MayIHazAccessPleases?"
	if (accessKey != null && "e050c92748cb12a067c3c2b4fcbbe445".equals(accessKey)) {
		CacheLocator.getVeloctyResourceCache().clearCache();
		%>
			Velocity Resource Cache clear action called.
		<%
	} else {
		%>
			<span style="color: #C24646; font-weight: bold;">Incorrect parameter name and/or value. NO cache cleared.</span>
		<%
	}	
%>

