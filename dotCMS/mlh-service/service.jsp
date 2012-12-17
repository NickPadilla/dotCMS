<%@ page import="com.dotmarketing.beans.Identifier,
com.dotmarketing.beans.Inode,
com.dotmarketing.beans.MultiTree,
com.dotmarketing.business.APILocator,
com.dotmarketing.cache.FieldsCache,
com.dotmarketing.business.IdentifierCache,
com.dotmarketing.cache.StructureCache,
com.dotmarketing.db.HibernateUtil,
com.dotmarketing.exception.DotHibernateException,
com.dotmarketing.factories.InodeFactory,
com.dotmarketing.factories.MultiTreeFactory,
com.dotmarketing.factories.WebAssetFactory,
com.dotmarketing.portlets.containers.model.Container,
com.dotmarketing.portlets.containers.business.ContainerAPIImpl,
com.dotmarketing.portlets.contentlet.model.Contentlet,
com.dotmarketing.portlets.contentlet.business.ContentletAPI,
com.dotcms.content.elasticsearch.business.ESContentletAPIImpl,
com.dotmarketing.portlets.folders.model.Folder,
com.dotmarketing.portlets.folders.business.FolderAPI,
com.dotmarketing.portlets.htmlpages.model.HTMLPage,
com.dotmarketing.portlets.htmlpages.business.HTMLPageAPI,
com.dotmarketing.portlets.htmlpages.business.HTMLPageAPIImpl,
com.dotmarketing.portlets.languagesmanager.business.LanguageAPI,
com.dotmarketing.portlets.structure.model.Field,
com.dotmarketing.portlets.structure.model.Structure,
com.dotmarketing.portlets.templates.model.Template,
com.dotmarketing.portlets.templates.business.TemplateAPIImpl,
com.dotmarketing.util.InodeUtils,
com.dotmarketing.util.Logger,
com.dotmarketing.business.UserAPIImpl,
com.liferay.portal.model.User,
com.dotmarketing.exception.DotDataException,
com.dotmarketing.exception.DotSecurityException,
com.dotmarketing.business.UserFactoryLiferayImpl,
org.mlh.dotcms.util.*,
org.quartz.Job,
org.quartz.JobExecutionContext,
org.quartz.JobExecutionException,
java.util.*,
javax.sql.*,
org.apache.commons.lang.*
"%>

<%!
public void updateContentletStringFieldByIdentifier(String structurename, String identifier, String userEmail, String velocityVarName, String fieldvalue){
	try
	{
		HibernateUtil.startTransaction();
		
		// get the structure
		Structure structure = StructureCache.getStructureByType(structurename);
		
		// the field to update
		Field field = structure.getFieldVar(velocityVarName);
		
		// setup conApi
		ESContentletAPIImpl conAPI = new ESContentletAPIImpl();
		
		// find content by identifier
		Contentlet contentlet = conAPI.findContentletByIdentifier(identifier,true,1,UserUtil.getUserByEmail(userEmail),true);
		
		// check it out
		Contentlet checkedOutContentlet = conAPI.checkout(contentlet.getInode(),UserUtil.getUserByEmail(userEmail), false);
		
		// update the field value
		conAPI.setContentletProperty(checkedOutContentlet, field, fieldvalue);
		
		// set it to live
		APILocator.getVersionableAPI().setLive(checkedOutContentlet);
		
		// check it in.
		try 
		{
			conAPI.checkin(checkedOutContentlet, UserUtil.getUserByEmail(userEmail), false);
		} 
		catch (Exception e) 
		{
			Logger.error(this, "Unable checkin the Contentlet. " + e.getMessage(), e);
		}
	}
	catch(Exception e)
	{
		Logger.error("updatedContent"," updatedContent failed = "+e);
	}
	finally
	{
		try 
		{
			HibernateUtil.commitTransaction();
		} catch (DotHibernateException e) {
			Logger.error("", e.getMessage());
		}
	}
}
%>

<%
String securityCode="shiboleet";
if(StringUtils.isNotEmpty(request.getParameter("securityCode")))
{
	if(request.getParameter("securityCode").equals(securityCode))
	{
		if(StringUtils.isNotEmpty(request.getParameter("classRegNewSize")))
		{
			updateContentletStringFieldByIdentifier("CourseSession", request.getParameter("identifier") , "gregory.jordan@mlh.org", "currentRegistrantTotal", request.getParameter("classRegNewSize"));
		}
	}
}
%>


