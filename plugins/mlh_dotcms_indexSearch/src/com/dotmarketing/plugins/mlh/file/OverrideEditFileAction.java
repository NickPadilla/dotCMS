/**
 * 
 */
package com.dotmarketing.plugins.mlh.file;

import java.util.Collections;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.dotmarketing.factories.InodeFactory;
import com.dotmarketing.plugins.mlh.esindex.core.ESFileIndexAPI;
import com.dotmarketing.portlets.files.action.EditFileAction;
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.util.Logger;
import com.liferay.portal.util.Constants;

/**
 * This class extends the {@link EditFileAction} so we can add our own logic to update/delete to/from the ES Index.
 * We first let the super class perform its logic, in case we error out we don't need to perform our logic.
 * This class only looks for DELETE's and ADD's. There are no publish stuff going on here. 
 * @author Nicholas Padilla
 *
 */
public class OverrideEditFileAction extends EditFileAction {

	@Override
	public void processAction(ActionMapping mapping, ActionForm form, PortletConfig config, ActionRequest req, ActionResponse res) throws Exception {
		// first let super process
		super.processAction(mapping, form, config, req, res);
		
		String cmd = req.getParameter(Constants.CMD);
		
		String [] identifiers = null;
		
		if ((cmd != null) && (cmd.equals(Constants.DELETE) 
								|| cmd.equals(com.dotmarketing.util.Constants.FULL_DELETE)
								|| cmd.equals(com.dotmarketing.util.Constants.UNPUBLISH))) {			
			File file = (File) InodeFactory.getInode(req.getParameter("inode"), File.class);
			identifiers = Collections.singleton(file.getIdentifier()).toArray(new String[0]);			
		} else if(cmd.equals(com.dotmarketing.util.Constants.FULL_DELETE_LIST)){
			String[] inodes = req.getParameterValues("publishInode");
			if(inodes != null && inodes.length > 0){
				identifiers = new String[inodes.length];
				for(int i = 0; i < inodes.length; i++){
					File file = (File) InodeFactory.getInode(req.getParameter("inode"), File.class);
					identifiers[i] = file.getIdentifier();
				}
			}
		}else if(cmd.equals(Constants.ADD)){// need to ensure we capture the save and publish items here
			String subcmd = req.getParameter("subcmd");
			// only add them if they were also published
			if((subcmd != null) && subcmd.equals(com.dotmarketing.util.Constants.PUBLISH)){
				File file = (File) InodeFactory.getInode(req.getParameter("inode"), File.class);
				ESFileIndexAPI.addFileToIndex(file);
				Logger.debug(this, "File Indexed to ES Successfully");
			}
		}

		if(identifiers != null){
			for(String id : identifiers){
				ESFileIndexAPI.removeIdentifierFromIndex(id);
			}
			Logger.debug(this, "File(s) Deleted From ES Successfully");
		}
	}


}
