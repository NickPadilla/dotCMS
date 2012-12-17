/**
 * 
 */
package com.dotmarketing.plugins.mlh.htmlpage;

import java.util.Collections;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.dotmarketing.factories.InodeFactory;
import com.dotmarketing.plugins.mlh.esindex.core.ESHTMLPageIndexAPI;
import com.dotmarketing.portlets.htmlpages.action.EditHTMLPageAction;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.util.Logger;
import com.liferay.portal.util.Constants;

/**
 * This class extends the {@link EditHTMLPageAction} so we can add our own logic to update the ES Index.
 * We first let the super class perform its logic, in case we error out we don't need to perform our logic. 
 * This class only looks for the DELETE and ADD functionality.
 * @author Nicholas Padilla
 *
 */
public class OverrideEditHTMLPageAction extends EditHTMLPageAction {

	@Override
	public void processAction(ActionMapping mapping, ActionForm form, PortletConfig config, ActionRequest req, ActionResponse res) throws Exception {
		// first let super process
		super.processAction(mapping, form, config, req, res);
		
		// perform work before calling the super method
		String cmd = req.getParameter(Constants.CMD);
		String [] identifiers = null;
		
		if ((cmd != null) && (cmd.equals(Constants.DELETE) 
								|| cmd.equals(com.dotmarketing.util.Constants.FULL_DELETE)
								|| cmd.equals(com.dotmarketing.util.Constants.UNPUBLISH))) {			
			HTMLPage htmlpage = (HTMLPage) InodeFactory.getInode(req.getParameter("inode"),HTMLPage.class);
			identifiers = Collections.singleton(htmlpage.getIdentifier()).toArray(new String[0]);			
		} else if(cmd.equals(com.dotmarketing.util.Constants.FULL_DELETE_LIST)){
			String[] inodes = req.getParameterValues("publishInode");
			if(inodes != null && inodes.length > 0){
				identifiers = new String[inodes.length];
				for(int i = 0; i < inodes.length; i++){
					HTMLPage webAsset = (HTMLPage) InodeFactory.getInode(inodes[i],HTMLPage.class);
					identifiers[i] = webAsset.getIdentifier();
				}
			}
		}else if(cmd.equals(Constants.ADD)){// need to ensure we capture the save and publish items here
			String subcmd = req.getParameter("subcmd");
			// only add them if they were also published
			if((subcmd != null) && subcmd.equals(com.dotmarketing.util.Constants.PUBLISH)){
				HTMLPage htmlpage = (HTMLPage) InodeFactory.getInode(req.getParameter("inode"),HTMLPage.class);
				ESHTMLPageIndexAPI.addHTMLPageToIndex(htmlpage);
				Logger.debug(this, "HTMLPage Indexed to ES Successfully");
			}
		}

		if(identifiers != null){
			for(String id : identifiers){
				ESHTMLPageIndexAPI.removeIdentifierFromIndex(id);
			}
			Logger.debug(this, "HTMLPage(s) Deleted From ES Successfully");
		}
	}

}
