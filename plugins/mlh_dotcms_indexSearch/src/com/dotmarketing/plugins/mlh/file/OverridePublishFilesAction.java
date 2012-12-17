/**
 * 
 */
package com.dotmarketing.plugins.mlh.file;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.dotmarketing.factories.InodeFactory;
import com.dotmarketing.plugins.mlh.esindex.core.ESFileIndexAPI;
import com.dotmarketing.portlets.files.action.PublishFilesAction;
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.util.InodeUtils;
import com.dotmarketing.util.Logger;

/**
 * This class extends the {@link PublishFilesAction} so we can add our own logic to update the ES Index.
 * We first let the super class perform its logic, in case we error out we don't need to perform our logic. 
 * This class only looks for the Publish functionality.
 * @author Nicholas Padilla
 *
 */
public class OverridePublishFilesAction extends PublishFilesAction {

	@Override
	public void processAction(ActionMapping mapping, ActionForm form, PortletConfig config, ActionRequest req, ActionResponse res) throws Exception {
		// first let super process
		super.processAction(mapping, form, config, req, res);
		
		String[] publishInode = req.getParameterValues("publishInode");
		if (publishInode == null) return;
		
		for (int i=0;i<publishInode.length;i++) {
			File file = (File) InodeFactory.getInode(publishInode[i],File.class);
			if (InodeUtils.isSet(file.getInode())) {
				// we know we have a valid file - index it
				ESFileIndexAPI.addFileToIndex(file);
			}			
			Logger.debug(this, "File(s) Indexed to ES Successfully");
		}				
	}

}
