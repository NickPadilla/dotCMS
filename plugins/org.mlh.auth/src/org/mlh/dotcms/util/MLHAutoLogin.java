package org.mlh.dotcms.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.integration.model.user.User;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;

import com.atlassian.crowd.service.client.CrowdClient;
import com.dotmarketing.util.Logger;
import com.liferay.portal.auth.AutoLogin;
import com.liferay.portal.auth.AutoLoginException;

public class MLHAutoLogin implements AutoLogin {
	
	CrowdClient crowdClient = new RestCrowdClientFactory().newInstance("crowd.methodisthealth.org/crowd/services/", "dotcms", "dotcms");
	User user;

	public String[] login(HttpServletRequest req, HttpServletResponse res) throws AutoLoginException {

		try {
		      String[] credentials = null;
		      
		      
		      Enumeration paramNames = req.getParameterNames();
		      
		      while(paramNames.hasMoreElements()) {
		          String paramName = (String)paramNames.nextElement();
		          Logger.info("MLHAuth.class", paramName);
		      }
		      
		      user = (User) crowdClient.authenticateUser(req.getParameter("loginTextBox"), req.getParameter("loginPasswordTextBox"));


		      if (user != null) {
		        //User user = UserLocalManagerUtil.getUserById(userId);

		        credentials = new String[3];

		        credentials[0] = user.getName();
		        credentials[1] =req.getParameter("loginPasswordTextBox");
		        credentials[2] = Boolean.FALSE.toString();
		      }

		      return credentials;
		    }
		    catch (Exception e) {
		      throw new AutoLoginException(e);
		    }
	}

}
