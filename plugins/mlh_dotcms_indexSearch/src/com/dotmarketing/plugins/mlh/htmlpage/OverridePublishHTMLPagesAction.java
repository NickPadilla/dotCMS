/**
 * 
 */
package com.dotmarketing.plugins.mlh.htmlpage;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.dotmarketing.factories.InodeFactory;
import com.dotmarketing.plugins.mlh.esindex.core.ESHTMLPageIndexAPI;
import com.dotmarketing.portlets.htmlpages.action.PublishHTMLPagesAction;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.util.Logger;
import com.liferay.portal.util.Constants;

/**
 * This class extends the {@link PublishHTMLPagesAction} so we can add our own logic to update the ES Index.
 * We first let the super class perform its logic, in case we error out we don't need to perform our logic. 
 * This class only looks for the PUBLISH functionality.
 * @author Nicholas Padilla
 *
 */
public class OverridePublishHTMLPagesAction extends PublishHTMLPagesAction {

	@Override
	public void processAction(ActionMapping mapping, ActionForm form, PortletConfig config, ActionRequest req, ActionResponse res) throws Exception {
		// first let super process
		super.processAction(mapping, form, config, req, res);
		
		// perform work before calling the super method
		String cmd = req.getParameter(Constants.CMD);
		
		if ((cmd != null) && (cmd.equals(com.dotmarketing.util.Constants.PUBLISH)
								|| cmd.equals(com.dotmarketing.util.Constants.PREPUBLISH))) {
			String[] publishInode = req.getParameterValues("publishInode");				
			if(publishInode != null && publishInode.length > 0){
				for(int i = 0; i < publishInode.length; i++){
					HTMLPage htmlpage = (HTMLPage) InodeFactory.getInode(publishInode[i],HTMLPage.class);
					ESHTMLPageIndexAPI.addHTMLPageToIndex(htmlpage);
				}
				Logger.debug(OverridePublishHTMLPagesAction.class, "HTMLPage(s) Indexed to ES Successfully");
			}	
		}			
	}

}
