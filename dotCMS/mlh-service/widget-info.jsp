<%@ page import="java.util.HashMap,
java.util.Map,
java.util.List,
com.dotmarketing.business.APILocator,
com.dotmarketing.business.UserFactoryLiferayImpl,
com.dotmarketing.portlets.contentlet.model.Contentlet,
com.dotmarketing.portlets.structure.model.Structure,
com.dotmarketing.portlets.structure.model.Field,
com.dotmarketing.cache.FieldsCache,
com.dotmarketing.util.Logger,
com.dotmarketing.util.json.JSONObject,
com.liferay.portal.model.User
"%>

<%
	
	/***************************************************************
	* Required Parameters: "widgetInode" and "canHazAccess"
	*		
	* Returns: json string of the widget's contentlet map 
	*					and the widget's structure map
	****************************************************************/
	
	String widgetInode = request.getParameter("widgetInode");
	// value should be "d1b55059d902df3b88f6f0bcf03602f9" - the md5 hash of the text "canHazAccess"
	String canHazAccess = request.getParameter("canHazAccess");
	boolean noError = false;
	JSONObject jsonObj = null;
	
	if ("d1b55059d902df3b88f6f0bcf03602f9".equals(canHazAccess)) {
		if (widgetInode != null) {
			String userName = "stephen.blalock@mlh.org";
			User user = null;
			try {
				UserFactoryLiferayImpl userFactory = new UserFactoryLiferayImpl();
				user = userFactory.loadByUserByEmail(userName);
				if (user != null) {
					Contentlet contentlet = APILocator.getContentletAPI().find(widgetInode, user, true);
					if (contentlet != null) {
						String stInode = contentlet.getStructureInode();
						//Structure structure = StructureCache.getStructureByInode(stInode);
						Structure structure = contentlet.getStructure();
						if (structure != null) {
							Map<String, Object> structureMap = structure.getMap();
							Map<String, Object> contentletMap = contentlet.getMap();
							Map<String, Object> fieldsMap = new HashMap<String, Object>();
							
							List<Field> structureFields = FieldsCache.getFieldsByStructureInode(structure.getInode());
							for (Field field : structureFields) {
								String fieldVarName = field.getVelocityVarName();
								Map<String, Object> fieldMap = field.getMap();
								fieldsMap.put(fieldVarName, fieldMap);
							}
							
							Map<String, Object> widgetInfoMap = new HashMap<String, Object>();
							widgetInfoMap.put("structureMap", structureMap);
							widgetInfoMap.put("contentletMap", contentletMap);
							widgetInfoMap.put("fieldsMap", fieldsMap);
							
							jsonObj = new JSONObject(widgetInfoMap);
							noError = true;
						} else {
							Logger.error(this.getClass(), "No Structure returned using stInode[\"" + stInode + "\"]");
						}
					} else {
						Logger.error(this.getClass(), "No Contentlet returned using widgetInode[\"" + widgetInode + "\"], userName[\"" + userName + "\"]");
					}
				} else {
					Logger.error(this.getClass(), "No User returned using \"" + userName + "\"");
				}
				
			} catch (Exception e) {
				Logger.error(this.getClass(), "Caught Exception", e);
			}
		} else {
			Logger.error(this.getClass(), "No \"widgetInode\" parameter specified");
		}
	} else {
		Logger.error(this.getClass(), "The \"canHazAccess\" parameter is either missing or incorrect");
	}
	
	if (!noError) {
		Map<String, String> errorsFound = new HashMap<String, String>();
		errorsFound.put("error", "Error occured: see logs for details");
		jsonObj = new JSONObject(errorsFound);
	}
%>

<%=jsonObj.toString()%>