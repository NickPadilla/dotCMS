package org.mlh.dotcms.util;

import com.dotmarketing.business.UserFactoryLiferayImpl;
import com.liferay.portal.model.User;

public class UserUtil {

	public static User getUserByEmail(String email)  throws Exception{
		
		UserFactoryLiferayImpl userFactory = new UserFactoryLiferayImpl();
		
		com.liferay.portal.model.User user1 = userFactory.loadByUserByEmail(email);
		
		return user1;
	}
}
