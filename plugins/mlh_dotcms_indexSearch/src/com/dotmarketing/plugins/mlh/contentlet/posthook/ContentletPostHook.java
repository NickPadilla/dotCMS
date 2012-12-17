package com.dotmarketing.plugins.mlh.contentlet.posthook;

import java.util.List;

import com.dotmarketing.beans.Permission;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.plugins.mlh.esindex.core.ESContentletIndexAPI;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;


/**
 * This class manages the publish/unpublish of Contentlets.  Currently will only publish if a Contentlet is not published. 
 * All work here is delegated to the {@link ESContentletIndexAPI}.
 * @author Nicholas Padilla
 *
 */
public class ContentletPostHook extends ContentletAPIPostHookAbstractImp {

	
	/* (non-Javadoc)
	 * @see com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp#publish(com.dotmarketing.portlets.contentlet.model.Contentlet, com.liferay.portal.model.User, boolean)
	 */
	@Override
	public void publish(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		// this is the method called when publishing a single piece of content
		super.publish(contentlet, user, respectFrontendRoles);
		try {
			Logger.debug(ContentletPostHook.class, "-|-|--|-|--|-|- trying to publish ");
			ESContentletIndexAPI.addContentToIndex(contentlet);
		} catch (DotDataException e) {
			e.printStackTrace();
		} catch (DotSecurityException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp#publish(java.util.List, com.liferay.portal.model.User, boolean)
	 */
	@Override
	public void publish(List<Contentlet> contentlets, User user, boolean respectFrontendRoles) {
		// this is called when publishing many pieces of content
		super.publish(contentlets, user, respectFrontendRoles);
		Logger.debug(ContentletPostHook.class, "-|-|--|-|--|-|- trying to publish many items ");
		try {
			for(Contentlet contentlet : contentlets){
				ESContentletIndexAPI.addContentToIndex(contentlet);
			}
		} catch (DotDataException e) {
			e.printStackTrace();
		} catch (DotSecurityException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp#unpublish(com.dotmarketing.portlets.contentlet.model.Contentlet, com.liferay.portal.model.User, boolean)
	 */
	@Override
	public void unpublish(Contentlet contentlet, User user, boolean respectFrontendRoles) {
		// this is where we unpublish a single piece of content
		super.unpublish(contentlet, user, respectFrontendRoles);
		Logger.debug(ContentletPostHook.class, "-|-|--|-|--|-|- trying to UNpublish ");
		try {
			ESContentletIndexAPI.removeContentFromIndex(contentlet);
		} catch (DotDataException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp#checkin(com.dotmarketing.portlets.contentlet.model.Contentlet, com.dotmarketing.portlets.structure.model.ContentletRelationships, java.util.List, java.util.List, com.liferay.portal.model.User, boolean, com.dotmarketing.portlets.contentlet.model.Contentlet)
	 */
	@Override
	public void checkin(Contentlet currentContentlet, ContentletRelationships relationshipsData, List<Category> cats, List<Permission> selectedPermissions, User user,	boolean respectFrontendRoles,Contentlet returnValue) {
		// this method gets called when content/save&publish is called.
		// also called when save is used - this doesn't actually allow the content 
		// to be shown so it is considered - working - do nothing when "save" is called
		//   we will need to ensure we only update/add data that is being published. The idea
		//   of a "working" index makes sense but isn't really what this plugin is abouts
		//   we will only be worring about published/unpublished/deleted data 
		super.checkin(currentContentlet, relationshipsData, cats, selectedPermissions, user, respectFrontendRoles, returnValue);
		Logger.debug(ContentletPostHook.class, "-|-|--|-|--|-|- trying to publish via save &publish ");
		try {
			if(returnValue.isLive()){
				ESContentletIndexAPI.addContentToIndex(returnValue);
			}
		} catch (DotDataException e) {
			e.printStackTrace();
		} catch (DotSecurityException e) {
			e.printStackTrace();
		}
	}
}