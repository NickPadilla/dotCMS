package org.mlh.dotcms.util;

import org.apache.commons.lang.StringUtils;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.portlets.folders.business.FolderAPI;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.portlets.htmlpages.business.HTMLPageAPI;
import com.dotmarketing.portlets.htmlpages.business.HTMLPageAPIImpl;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.util.Logger;

public class PageCreatorUtil {
	
	private static HTMLPageAPIImpl htmlPageAPIImpl = (HTMLPageAPIImpl) APILocator.getHTMLPageAPI();
	private static FolderAPI folderApi =(FolderAPI) APILocator.getFolderAPI();
	
	
	
	static org.apache.log4j.Logger log = Logger.getLogger(PageCreatorUtil.class);

	public PageCreatorUtil(){
		
	}
	
	public static HTMLPage createPage(String hostId, String folderContainingHTMLPageToCopy, String dotCMSLineage, String urlOfHTMLPageToCopy, String email, String isActive, String publishStatus){
		
		
		HTMLPage newHTMLPage=null;
		try 
		{
			Folder folderContainingPage = folderApi.findFolderByPath(folderContainingHTMLPageToCopy,hostId, UserUtil.getUserByEmail(email), true);
			Folder folderWherePageIsGoing= folderApi.findFolderByPath(dotCMSLineage,hostId, UserUtil.getUserByEmail(email), true);
			HTMLPage htmlPageBeingCopied = (HTMLPage) htmlPageAPIImpl.getWorkingHTMLPageByPageURL(urlOfHTMLPageToCopy,folderContainingPage);
			HTMLPageAPI.CopyMode copyMode = HTMLPageAPI.CopyMode.USE_SOURCE_CONTENT;
			newHTMLPage = htmlPageAPIImpl.copy(htmlPageBeingCopied,folderWherePageIsGoing,true,false,copyMode,UserUtil.getUserByEmail(email),true);		
		} 
		catch (Exception e) {
			log.info(e.getMessage());
		}
		return newHTMLPage;
		
	}
	
	
}
