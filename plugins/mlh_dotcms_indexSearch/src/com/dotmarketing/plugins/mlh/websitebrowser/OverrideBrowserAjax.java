/**
 * 
 */
package com.dotmarketing.plugins.mlh.websitebrowser;

import com.dotmarketing.beans.Inode;
import com.dotmarketing.exception.WebAssetException;
import com.dotmarketing.factories.InodeFactory;
import com.dotmarketing.plugins.mlh.esindex.core.ESFileIndexAPI;
import com.dotmarketing.plugins.mlh.esindex.core.ESHTMLPageIndexAPI;
import com.dotmarketing.portlets.browser.ajax.BrowserAjax;
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.util.Logger;

/**
 * @author Nicholas Padilla
 *
 */
public class OverrideBrowserAjax extends BrowserAjax {

	@Override
	public boolean publishAsset(String inode) throws WebAssetException, Exception {    
		// let the super do its work first    
		boolean ret = super.publishAsset(inode);
		
		Inode inodeObj = InodeFactory.getInode(inode,Inode.class);
        if(inodeObj instanceof HTMLPage){
        	ESHTMLPageIndexAPI.addHTMLPageToIndex((HTMLPage)inodeObj);
        	if(Logger.isInfoEnabled(OverrideBrowserAjax.class)){
        		Logger.info(this, "We have successfully indexed a HTMLPage to ES!");
        	}
        }else if(inodeObj instanceof File){
			ESFileIndexAPI.addFileToIndex((File)inodeObj);
        	if(Logger.isInfoEnabled(OverrideBrowserAjax.class)){
        		Logger.info(this, "We have successfully indexed a File to ES!");
        	}
        }
		return ret;
	}

	@Override
	public boolean unPublishAsset(String inode) throws Exception {
		// let the super do its work first
		boolean ret = super.unPublishAsset(inode);
		
		Inode inodeObj = InodeFactory.getInode(inode,Inode.class);
        if(inodeObj instanceof HTMLPage){
        	ESHTMLPageIndexAPI.removeIdentifierFromIndex(inodeObj.getIdentifier());
        	if(Logger.isInfoEnabled(OverrideBrowserAjax.class)){
        		Logger.info(this, "We have successfully deleted a HTMLPage from ES!");
        	}
        }else if(inodeObj instanceof File){
			ESFileIndexAPI.removeIdentifierFromIndex(inodeObj.getIdentifier());
        	if(Logger.isInfoEnabled(OverrideBrowserAjax.class)){
        		Logger.info(this, "We have successfully deleted a File from ES!");
        	}
        }
		return ret;
	}

}
