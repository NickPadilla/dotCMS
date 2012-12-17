package org.mlh.dotcms.util;

import java.util.ArrayList;
import java.util.List;


import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.client.CrowdClient;
import com.dotcms.enterprise.BaseAuthenticator;
import com.dotmarketing.auth.model.UserAttribute;
import com.dotmarketing.util.Logger;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.util.PropsUtil;

import com.atlassian.crowd.service.client.ClientResourceLocator;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.factory.CrowdClientFactory;

public class MLHAuth extends BaseAuthenticator {

	public static String CROWD_ATTR_FIRSTNAME = PropsUtil.get("auth.impl.crowd.attrib.firstName");
	public static String CROWD_ATTR_LASTNAME = PropsUtil.get("auth.impl.crowd.attrib.lastName");
	public static String CROWD_ATTR_EMAIL = PropsUtil.get("auth.impl.crowd.attrib.email");
	
	CrowdClient crowdClient = null;
	//CrowdClient crowdClient = new RestCrowdClientFactory().newInstance("http://crowd.methodisthealth.org/crowd", "dotcms", "dotcms");
	
	
	//com.liferay.portal.model.User user;
	com.atlassian.crowd.model.user.User user;
	
	static org.apache.log4j.Logger log = Logger.getLogger(MLHAuth.class);
	
	@Override
	public boolean authenticate(String username, String password)throws NoSuchUserException {
	
		if (crowdClient == null) {
			ClientResourceLocator clientResourceLocator = new ClientResourceLocator("crowd.properties");
		    ClientProperties clientProperties = ClientPropertiesImpl.newInstanceFromResourceLocator(clientResourceLocator);
		    CrowdClientFactory clientFactory = new RestCrowdClientFactory();
		    crowdClient = clientFactory.newInstance(clientProperties);
		}
		
		try {
			if (log.isDebugEnabled()) {
				log.debug("authenticating: username[" + username + "]");
			}
			user = crowdClient.authenticateUser(username, password);
			return true;
		} catch (Exception e) {
			log.error("authenticate failed" +e.toString());
			return false;
		}

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ArrayList loadGroups(String username, String password) {
		try {
			user = crowdClient.authenticateUser(username, password);
			List<Group> groups = crowdClient.getGroupsForUser(username, 0, -1);
			
			ArrayList<String> groupsArr = new ArrayList<String>();
			
			if (log.isDebugEnabled()) {
				log.debug("loadGroups method: username[" + username + "]");
			}
			
			for(Group group : groups) {
				
				String groupName = group.getName();				  
				if (groupName.matches(GROUP_FILTER)) {
					if (log.isDebugEnabled()) {
						log.debug("loadGroups: applying GROUP_FILTER[" + GROUP_FILTER + "] to group[" + groupName + "]");
					}
					
					if ((GROUP_STRING_TO_STRIP != null) && (!"".equals(GROUP_STRING_TO_STRIP))) {
						if (log.isDebugEnabled()) {
							log.debug("loadGroups: applying GROUP_STRING_TO_STRIP[" + GROUP_STRING_TO_STRIP + "]");
						}
						groupName = groupName.replaceFirst(GROUP_STRING_TO_STRIP, "");
				    }
					groupsArr.add(groupName);
					if (log.isDebugEnabled()) {
						log.debug("loadGroups: group[" + groupName + "]");
					}
				}
				
			}
			if (log.isDebugEnabled()) {
				log.debug("*** MLHAuth.loadGroups return for user[" + username + " ****");
				for (String groupName : groupsArr) {
					log.debug("***** " + groupName);
				}
				log.debug("*******************************************************");
			}
			return groupsArr;
		} catch (Exception e) {
			log.error("loadGroups failed: " + e.toString());
			return null;
		}
	}

	@Override
	public UserAttribute loadAttributes(String username, String password) {
		
		try {
			if (log.isDebugEnabled()) {
				log.debug("loadAttributes : username[" + username + "]");
			}
			
			UserAttribute userAttribute = new UserAttribute();
			user = crowdClient.authenticateUser(username, password);
			
			if (log.isDebugEnabled()) {
				log.debug("loadAttributes : firstName[" + user.getFirstName() + "]");
			}
			userAttribute.setFirstName(user.getFirstName());
			
			if (log.isDebugEnabled()) {
				log.debug("loadAttributes : lastName[" + user.getLastName() + "]");
			}
	    	userAttribute.setLastName(user.getLastName());
		 
			if (user.getEmailAddress() != null) {
				String emailAddress = user.getEmailAddress() == null ? username + "@fakedotcms.org" : user.getEmailAddress();
				if (log.isDebugEnabled()) {
					log.debug("loadAttributes : emailAddress[" + emailAddress + "]");
				}
		    	userAttribute.setEmailAddress(emailAddress);
		    } else {
		    	throw new Exception("Exception in loadAttributes setEmailAddress: username[" + username + "] does NOT have an emailAddress[" + user.getEmailAddress() + "]");
		    }
			return userAttribute;
		} catch (Exception e) {
			log.error("Exception in loadAttributes:", e);
			return null;
		}
		
	}

	

}

