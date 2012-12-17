package com.dotcms.enterprise;

import com.dotmarketing.auth.model.UserAttribute;
import com.dotmarketing.beans.UserProxy;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.Role;
import com.dotmarketing.business.RoleAPI;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.business.UserProxyAPI;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.auth.AuthException;
import com.liferay.portal.auth.Authenticator;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PropsUtil;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPResponse;
import com.novell.ldap.LDAPResponseQueue;
import com.novell.ldap.LDAPSocketFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;

public abstract class BaseAuthenticator
  implements Authenticator
{
  public static String INITIAL_CONTEXT_FACTORY = PropsUtil.get("com.sun.jndi.ldap.LdapCtxFactory");

  public static String SECURITY_AUTHENTICATION = PropsUtil.get("auth.impl.ldap.security.authentication");

  public static String SECURITY_KEYSTORE_PATH = PropsUtil.get("auth.impl.ldap.security.keystore.path");

  public static String HOST = PropsUtil.get("auth.impl.ldap.host");

  public static String PORT = PropsUtil.get("auth.impl.ldap.port");

  public static String USERID = PropsUtil.get("auth.impl.ldap.userid");

  public static String PASSWORD = PropsUtil.get("auth.impl.ldap.password");

  public static String DOMAINLOOKUP = PropsUtil.get("auth.impl.ldap.domainlookup");

  public static boolean IS_BUILD_GROUPS = Boolean.valueOf(PropsUtil.get("auth.impl.build.groups")).booleanValue();

  public static String GROUP_FILTER = PropsUtil.get("auth.impl.ldap.build.group.name.filter");

  public static String GROUP_STRING_TO_STRIP = PropsUtil.get("auth.impl.ldap.build.group.name.filter.strip");

  public static String LDAP_USER_ROLE = "LDAP User";

  public static String USER_ATTRIB = PropsUtil.get("auth.impl.ldap.attrib.user");

  public static String FIRST_NAME_ATTRIB = PropsUtil.get("auth.impl.ldap.attrib.firstName");

  public static String MIDDLE_NAME_ATTRIB = PropsUtil.get("auth.impl.ldap.attrib.middleName");

  public static String LAST_NAME_ATTRIB = PropsUtil.get("auth.impl.ldap.attrib.lastName");

  public static String NICKNAME_ATTRIB = PropsUtil.get("auth.impl.ldap.attrib.nickName");

  public static String EMAIL_ATTRIB = PropsUtil.get("auth.impl.ldap.attrib.email");

  public static String GENDER_ATTRIB = PropsUtil.get("auth.impl.ldap.attrib.gender");

  public static String GROUP_ATTRIB = PropsUtil.get("auth.impl.ldap.attrib.group");

  public static boolean SYNC_PASSWORD = Boolean.valueOf(PropsUtil.get("auth.impl.ldap.syncPassword")).booleanValue();
  private UserAttribute userAttribute;
  private User liferayUser;
  private String userPassword;
  private String userLogin;
  private boolean isUserId;

  public abstract boolean authenticate(String paramString1, String paramString2)
    throws com.liferay.portal.NoSuchUserException;

  public abstract ArrayList<String> loadGroups(String paramString1, String paramString2);

  public abstract UserAttribute loadAttributes(String paramString1, String paramString2);

  public int authenticateByEmailAddress(String companyId, String emailAddress, String password)
    throws AuthException
  {
    this.userPassword = password;
    this.userLogin = emailAddress;
    this.isUserId = false;
    return authUser(companyId, emailAddress, password);
  }

  public int authenticateByUserId(String companyId, String userId, String password) throws AuthException {
    this.userPassword = password;
    this.userLogin = userId;
    this.isUserId = true;
    return authUser(companyId, userId, password);
  }

  private int authUser(String companyId, String username, String password) {
    if (password.equals("fake_dotCMS_LDAP_password")) {
      return -1;
    }
    boolean authenticated = false;
    boolean deleteLiferayUser = false;
    try {
      authenticated = authenticate(username, password);
    } catch (com.liferay.portal.NoSuchUserException nsu) {
      deleteLiferayUser = true;
    }

    if (authenticated) {
      this.userAttribute = loadAttributes(username, password);
      try {
        if (!this.isUserId)
          this.liferayUser = APILocator.getUserAPI().loadByUserByEmail(username.toLowerCase(), APILocator.getUserAPI().getSystemUser(), false);
        else {
          this.liferayUser = APILocator.getUserAPI().loadUserById(username, APILocator.getUserAPI().getSystemUser(), false);
        }

        if (!syncUser(password))
          throw new Exception("Unable to sync user");
      }
      catch (com.dotmarketing.business.NoSuchUserException nsne) {
        Logger.debug(this, "creating the user on liferay");
        try
        {
          migrateUser(username, companyId, this.isUserId);
          syncUser(password);
        } catch (Exception ex) {
          Logger.error(this, "Error creating the user on liferay: " + ex.getMessage(), ex);
        }
      } catch (Exception ex) {
        Logger.error(this, "Error authenticating User : ", ex);
        return -1;
      }
      Logger.debug(BaseAuthenticator.class, "Sync directory service --> CMS Groups");
      try {
        syncUserAttributes(companyId);
      } catch (Exception e) {
        Logger.error(BaseAuthenticator.class, "Unable to sync user attributes : ", e);
      }
      if (IS_BUILD_GROUPS) {
        try {
          syncUserGroups();
        } catch (Exception e) {
          Logger.error(BaseAuthenticator.class, "Unable to sync groups : ", e);
        }
      }
      Logger.debug(this, "Auth module login sucess.");
      return 1;
    }
    Logger.debug(this, "User not found, trying a login against liferay");
    try {
      if (this.isUserId)
        this.liferayUser = APILocator.getUserAPI().loadUserById(username, APILocator.getUserAPI().getSystemUser(), false);
      else {
        this.liferayUser = APILocator.getUserAPI().loadByUserByEmail(username, APILocator.getUserAPI().getSystemUser(), false);
      }
      if ((deleteLiferayUser) && (APILocator.getRoleAPI().doesUserHaveRole(this.liferayUser, APILocator.getRoleAPI().loadRoleByKey(LDAP_USER_ROLE)))) {
        Logger.info(this, "Deleteing user " + this.liferayUser.getUserId() + " from portal becuase he is no longer in LDAP");
        APILocator.getUserAPI().delete(this.liferayUser, APILocator.getUserAPI().getSystemUser(), false);
        return -1;
      }if ((!SYNC_PASSWORD) && (APILocator.getRoleAPI().doesUserHaveRole(this.liferayUser, APILocator.getRoleAPI().loadRoleByKey(LDAP_USER_ROLE)))) {
        Logger.debug(this, "Failed to Auth against LDAP and passwords are not synced so auth will fail");
        return -1;
      }
      if ((this.liferayUser.getPassword().equals(password)) || (this.liferayUser.getPassword().equals(PublicEncryptionFactory.digestString(password)))) {
        Logger.debug(this, "Login against liferay succeed");
        return 1;
      }
      Logger.debug(this, "Login against liferay failed");
      return -1;
    }
    catch (Exception e)
    {
      Logger.debug(this, "Login against liferay fails");
    }return -1;
  }

  private boolean migrateUser(String username, String companyId, boolean isUserId)
    throws Exception
  {
    try
    {
      this.liferayUser = APILocator.getUserAPI().createUser(isUserId ? username : null, isUserId ? null : username);
      this.liferayUser.setActive(true);
      this.liferayUser.setCompanyId(companyId);
      this.liferayUser.setPassword(this.userPassword);
      this.liferayUser.setCreateDate(new Date());

      APILocator.getUserAPI().save(this.liferayUser, APILocator.getUserAPI().getSystemUser(), false);
      try {
        APILocator.getRoleAPI().addRoleToUser(APILocator.getRoleAPI().loadRoleByKey(LDAP_USER_ROLE), this.liferayUser);
      } catch (Exception ex) {
        Logger.error(this, "Unable to add user " + this.liferayUser.getUserId() + " to LDAP User role", ex);
      }
      Logger.debug(BaseAuthenticator.class, "User Migration Successfull");
      return true;
    } catch (Exception ex) {
      Logger.error(BaseAuthenticator.class, "Failed to migrate user " + username + ": ", ex);
    }return false;
  }

  private boolean syncUserGroups()
    throws Exception
  {
    ArrayList<String> groups = loadGroups(this.userLogin, this.userPassword);
    if (groups != null) {
      List<Role> roles = APILocator.getRoleAPI().loadRolesForUser(this.liferayUser.getUserId(), false);
      for (Role role : roles) {
        APILocator.getRoleAPI().removeRoleFromUser(role, this.liferayUser);
      }
      for (String group : groups) {
        Role r = null;
        try {
          r = APILocator.getRoleAPI().loadRoleByKey(group);
        } catch (Exception e) {
          Logger.warn(this, "Role with key, \"" + group + "\" doesn't exist in dotCMS : " + e.getMessage(), e);
          continue;
        }

        if ((r != null) && (UtilMethods.isSet(r.getId()))) {
          APILocator.getRoleAPI().addRoleToUser(r, this.liferayUser);
        } else {
          Logger.debug(BaseAuthenticator.class, "Unable to add user to role[" + (r != null ? r.getName() : "null role") + "] because it doesn't exist");
        }
      }

      Role role = APILocator.getRoleAPI().getUserRole(this.liferayUser);
      boolean hasRole = APILocator.getRoleAPI().doesUserHaveRole(this.liferayUser, role);
      if (!hasRole) {
    	  //MLH: added try catch here, since always throws exception...and would skip setting the LDAP User role
    	  try {
    		  APILocator.getRoleAPI().addRoleToUser(role, this.liferayUser);
    	  } catch (Exception ex) {
    		  
    		  String parentRoleName = null;
    		 
    		  if (role.getParent() != null) {
    			  Role parentRole = APILocator.getRoleAPI().loadRoleById(role.getParent());
    			  if (parentRole != null) {
    				  parentRoleName = parentRole.getName();
    			  }
    		  }
    		  Logger.error(this, "exception while trying to add role[" + role.getRoleKey() + "=" + role.getName() + "] parent[" + role.getParent() + "=" + parentRoleName + "] to user[" + this.liferayUser.getUserId() + "=" + this.liferayUser.getFullName() + "]");
    		  Logger.error(this, "...unable to addRoleToUser when a role was NOT detected for the user: " + ex.getMessage());
    	  }
    	  
    	  //MLH
      }
      try {
        APILocator.getRoleAPI().addRoleToUser(APILocator.getRoleAPI().loadRoleByKey(LDAP_USER_ROLE), this.liferayUser);
      } catch (Exception ex) {
        Logger.error(this, "Unable to add user " + this.liferayUser.getUserId() + " to LDAP User role", ex);
      }
      
      
    }
    return true;
  }

  private boolean syncUser(String password)
    throws Exception
  {
    this.liferayUser.setPassword(PublicEncryptionFactory.digestString(password));
    this.liferayUser.setPasswordEncrypted(true);
    APILocator.getUserAPI().save(this.liferayUser, APILocator.getUserAPI().getSystemUser(), false);
    boolean userHasLDAPRole = false;
    List<Role> roles = APILocator.getRoleAPI().loadRolesForUser(this.liferayUser.getUserId(), false);
    for (Role role : roles) {
      if (role.getRoleKey().equals(LDAP_USER_ROLE)) {
        userHasLDAPRole = true;
      }
    }
    if (!userHasLDAPRole) {
      try {
        APILocator.getRoleAPI().addRoleToUser(APILocator.getRoleAPI().loadRoleByKey(LDAP_USER_ROLE), this.liferayUser);
      } catch (Exception ex) {
        Logger.error(this, "Unable to add user " + this.liferayUser.getUserId() + " to LDAP User role", ex);
      }
    }
    return true;
  }

  private boolean syncUserAttributes(String companyId)
    throws Exception
  {
    if (UtilMethods.isSet(FIRST_NAME_ATTRIB)) {
      String firstName = this.userAttribute.getFirstName() == null ? "" : this.userAttribute.getFirstName();
      this.liferayUser.setFirstName(firstName);
    }
    if (UtilMethods.isSet(MIDDLE_NAME_ATTRIB)) {
      String middleName = this.userAttribute.getMiddleName() == null ? "" : this.userAttribute.getMiddleName();
      this.liferayUser.setMiddleName(middleName);
    }
    if (UtilMethods.isSet(LAST_NAME_ATTRIB)) {
      String lastName = this.userAttribute.getLastName() == null ? "" : this.userAttribute.getLastName();
      this.liferayUser.setLastName(lastName);
    }
    if (UtilMethods.isSet(NICKNAME_ATTRIB)) {
      String nickName = this.userAttribute.getNickName() == null ? "" : this.userAttribute.getNickName();
      this.liferayUser.setNickName(nickName);
    }
    if (UtilMethods.isSet(GENDER_ATTRIB)) {
      this.liferayUser.setMale(this.userAttribute.isMale());
    }
    if (UtilMethods.isSet(EMAIL_ATTRIB)) {
      if (!this.isUserId)
        this.liferayUser.setEmailAddress(this.userLogin.toLowerCase());
      else {
        this.liferayUser.setEmailAddress(this.userAttribute.getEmailAddress().toLowerCase());
      }
    }

    if (this.userAttribute.getCustomProperties() != null) {
      UserProxy up = new UserProxy();
      BeanUtils.copyProperties(up, this.userAttribute.getCustomProperties());
      APILocator.getUserProxyAPI().saveUserProxy(up, APILocator.getUserAPI().getSystemUser(), false);
    }

    APILocator.getUserAPI().save(this.liferayUser, APILocator.getUserAPI().getSystemUser(), false);

    return true;
  }
  protected LDAPConnection getBindedConnection() throws DotRuntimeException {
    if (SECURITY_AUTHENTICATION.equalsIgnoreCase("SSL")) {
      System.setProperty("javax.net.ssl.trustStore", SECURITY_KEYSTORE_PATH);
      Logger.debug(this, "The trust store is " + System.getProperty("javax.net.ssl.trustStore"));
      LDAPSocketFactory ssf = new LDAPJSSESecureSocketFactory();

      LDAPConnection.setSocketFactory(ssf);
    }

    LDAPConnection lc = new LDAPConnection();
    try
    {
      lc.connect(HOST, Integer.valueOf(PORT).intValue());
    } catch (Exception e) {
      Logger.error(this, e.getMessage(), e);
    }

    LDAPResponseQueue queue = null;
    LDAPResponse rsp = null;
    try {
      queue = lc.bind(3, USERID, PASSWORD.getBytes(), (LDAPResponseQueue)null);

      rsp = (LDAPResponse)queue.getResponse();
    } catch (Exception ex) {
      Logger.error(BaseAuthenticator.class, ex.getMessage(), ex);
    }

    int rc = rsp.getResultCode();

    String msg = rsp.getErrorMessage();

    if (rc == 0) {
      Logger.debug(this, "LDAP connection is now bound");
      return lc;
    }
    throw new DotRuntimeException("Unable to bind to ldap " + msg);
  }
}