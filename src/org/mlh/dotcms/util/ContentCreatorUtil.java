package org.mlh.dotcms.util;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.dotmarketing.beans.Identifier;
import com.dotmarketing.beans.MultiTree;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierCache;
import com.dotmarketing.db.HibernateUtil;
import com.dotmarketing.exception.DotHibernateException;
import com.dotmarketing.factories.InodeFactory;
import com.dotmarketing.factories.MultiTreeFactory;
import com.dotmarketing.portlets.containers.business.ContainerAPIImpl;
import com.dotmarketing.portlets.containers.model.Container;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.folders.business.FolderAPI;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.portlets.htmlpages.business.HTMLPageAPIImpl;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.util.InodeUtils;
import com.dotmarketing.util.Logger;

public class ContentCreatorUtil implements Job {
	
	
	private static ContentletAPI conAPI = APILocator.getContentletAPI();
	private static HTMLPageAPIImpl htmlPageAPIImpl = (HTMLPageAPIImpl) APILocator.getHTMLPageAPI();
	private static FolderAPI folderApi =(FolderAPI) APILocator.getFolderAPI();
	private static ContainerAPIImpl containerAPIImpl= (ContainerAPIImpl) APILocator.getContainerAPI();
	
	static org.apache.log4j.Logger log = Logger.getLogger(ContentCreatorUtil.class);
	public ContentCreatorUtil(){
		
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
	}
	
	public static Contentlet getContentletByQueryOnStructureNameAndVignetteId(String structureName, String vignetteId, String userEmail) throws Exception{
		
		Contentlet contentlet = null;
		try{
			
			String query = "+structureName:"+structureName+" +"+structureName+".vignetteId:"+vignetteId;
			List<Contentlet> contentletList = conAPI.search(query,1,-1,"host1",UserUtil.getUserByEmail(userEmail),false);
			
			
			if(contentletList.size()>0)	
			{	
				contentlet=contentletList.get(0);
			}
			
			return contentlet;
			
		}
		catch(Exception e){
			Logger.error(ContentCreatorUtil.class, e.getMessage());
			return contentlet;
		}
	}

	public static String getContentletInodeByQueryOnStructureNameAndVignetteId(String structureName, String vignetteId, String userEmail) throws Exception{
		
		
		String inode=null;
		try
		{	
			inode=getContentletByQueryOnStructureNameAndVignetteId(structureName,vignetteId,userEmail).getInode();
			return inode;

		}
		catch(Exception e)
		{
			Logger.error(ContentCreatorUtil.class, e.getMessage());
			return inode;
		}
		
	}
	
	public static Contentlet getWidgetByQueryOnStructureNameAndTitle(String structureName, String widgetTitle, String userEmail) throws Exception {
		
		Contentlet contentlet = null;
		try{
			
			String query = "+structureName:"+structureName+" +"+structureName+".widgetTitle:"+widgetTitle;
			
			List<Contentlet> contentletList = conAPI.search(query,1,-1,"host1",UserUtil.getUserByEmail(userEmail),false);
			
			
			if(contentletList.size()>0)	
			{	
				contentlet=contentletList.get(0);
			}
			
			return contentlet;
			
		}
		catch(Exception e){
			Logger.error(ContentCreatorUtil.class, e.getMessage());
			return contentlet;
		}
		
	}
	
	public static String getWidgetInodeByQueryOnStructureNameAndTitle(String structureName, String widgetTitle, String userEmail) throws Exception{
		
		
		String inode=null;
		try
		{	
			inode=getWidgetByQueryOnStructureNameAndTitle(structureName,widgetTitle,userEmail).getInode();
			return inode;

		}
		catch(Exception e)
		{
			Logger.error(ContentCreatorUtil.class, e.getMessage());
			return inode;
		}
		
	}
	

}
