package com.dotmarketing.viewtools;

import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.view.tools.ViewTool;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Inode;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotIdentifierStateException;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotHibernateException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.folders.business.FolderAPI;
import com.dotmarketing.portlets.folders.model.Folder;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;

public class FolderWebAPI implements ViewTool{

	private FolderAPI folderAPI;
	private UserAPI userAPI;
	public void init(Object arg0) {
		folderAPI = APILocator.getFolderAPI();
		userAPI = APILocator.getUserAPI();
	}
	
	public List<Inode> findMenuItems(String path, HttpServletRequest req) throws PortalException, SystemException, DotDataException, DotSecurityException{
		Host host = WebAPILocator.getHostWebAPI().getCurrentHost(req);
		return findMenuItems(folderAPI.findFolderByPath(path, host, userAPI.getSystemUser(), true));
	}
	
	public List<Inode> findMenuItems(String folderInode) throws DotStateException, DotHibernateException, DotDataException, DotSecurityException{
		return findMenuItems(folderAPI.find(folderInode, userAPI.getSystemUser(), true));
	}
	
	@Deprecated
	public List<Inode> findMenuItems(long folderInode) throws DotStateException, DotHibernateException, DotDataException, DotSecurityException{
		return findMenuItems(String.valueOf(folderInode));
	}
	
	public List<Inode> findMenuItems(Folder folder) throws DotStateException, DotDataException{
		return folderAPI.findMenuItems(folder, userAPI.getSystemUser(), true);
	}
	
	public Folder findCurrentFolder(String path, Host host) throws DotStateException, DotDataException, DotSecurityException{
		return folderAPI.findFolderByPath(path, host.getIdentifier(), userAPI.getSystemUser(), true);
	}
	
	/** MLH Stuff */
	
	public Folder findParentFolder(String folderInode) throws DotIdentifierStateException, DotHibernateException, DotDataException, DotSecurityException {
		return folderAPI.findParentFolder(folderAPI.find(folderInode, userAPI.getSystemUser(), false), userAPI.getSystemUser(), false);
	}
	
	public String findURLPath(String folderInode) throws DotStateException, DotDataException, DotSecurityException {
		Folder folderFromInode = folderAPI.find(folderInode, userAPI.getSystemUser(), false);
		return (folderFromInode == null ? "" : APILocator.getIdentifierAPI().find(folderFromInode).getPath());
	}
	
	public List<Folder> findSubFolders(Folder folder) throws DotStateException, DotDataException, DotSecurityException {
	    List folders = folderAPI.findSubFolders(folder, userAPI.getSystemUser(), false);
	    return folders;
	}
	
	public List<Folder> findSubFolders(Host host) throws DotStateException, DotDataException, DotSecurityException {
	    List folders = folderAPI.findSubFolders(host, userAPI.getSystemUser(), false);
	    return folders;
	}
	
	public List<Folder> findSubFoldersShownOnMenu(Folder folder) throws DotStateException, DotDataException, DotSecurityException {
	    List<Folder> folders = folderAPI.findSubFolders(folder, userAPI.getSystemUser(), false);
	
	    List<Folder> foldersShownOnMenu = new ArrayList<Folder>();
	    for (Folder foldertocheck : folders)
	    {
	      if (!foldertocheck.isShowOnMenu())
	        continue;
	      foldersShownOnMenu.add(foldertocheck);
	    }
	
	    return foldersShownOnMenu;
	}
	
	public List<Folder> findSubFoldersShownOnMenu(Host host) throws DotStateException, DotDataException, DotSecurityException {
	    List<Folder> folders = folderAPI.findSubFolders(host, userAPI.getSystemUser(), false);
	
	    List<Folder> foldersShownOnMenu = new ArrayList<Folder>();
	    for (Folder foldertocheck : folders)
	    {
	      if (!foldertocheck.isShowOnMenu())
	        continue;
	      foldersShownOnMenu.add(foldertocheck);
	    }
	
	    return foldersShownOnMenu;
	}
}
