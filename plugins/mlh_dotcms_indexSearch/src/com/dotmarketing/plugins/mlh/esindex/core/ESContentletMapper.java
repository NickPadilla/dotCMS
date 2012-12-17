package com.dotmarketing.plugins.mlh.esindex.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.cache.FieldsCache;
import com.dotmarketing.cache.StructureCache;
import com.dotmarketing.common.db.DotConnect;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.business.FieldAPI;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Field.FieldType;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;


/**
 * Parses a {@link Contentlet} into a JSON string as dictated by requirements.  We will only provide all fields in the 
 * Type field within the Index.  The Content field will consist of only the specific fields requested by the requirements. 
 * We will only index Structures that have Detail Pages, no Detail Page no index. Here are the currently configured structures
 * and the fields that will make it into the Content field in the Index.
 * <ul>Structure Name <b>-</b> Type <b>-</b> Fields Used
 * 	<li>Article <b>-</b> Content/Structure <b>-</b> Title, Headline, Body Text, Teaser, Tags</li>
 * 	<li>Event (MLH) <b>-</b> Content/Structure <b>-</b> Title, Body Text, Tags</li>
 * 	<li>Facility <b>-</b> Content/Structure <b>-</b> Title, Body Text, Tags</li>
 *	<li>Leadership Bio <b>-</b> Content/Structure <b>-</b> First Name, Last Name, Keywords, Body Text, Tags</li>
 * 	<li>Physician <b>-</b> Content/Structure <b>-</b> First Name, Last Name, Tags</li>
 * </ul>
 * The order specified here must be the order specified in the index. 
 * @author Nicholas Padilla
 */
public class ESContentletMapper {

	/**
	 * Takes in a {@link Contentlet} and parses its fields into a JSON string containing the needed fields for the Content Type. Please see class
	 * level comments for valid structure types and the order in which the Content field will be formatted.  Also we will add all fields found for
	 * the valid content into the Type indexed field.
	 * @param con
	 * @return
	 */
	
	
	
	public static String toJson(Contentlet con) {		

		String x = ""; 
		try {
			Map<String,String> m = new HashMap<String,String>();
			Map<String,String> typeMap = new HashMap<String,String>();
			Map<String,String> loadFieldsMap = new HashMap<String,String>();

			Identifier ident = APILocator.getIdentifierAPI().find(con.getIdentifier());
			Structure st=StructureCache.getStructureByInode(con.getStructureInode());

			// check here for a detail page and only process if this Contentlet has one
			if(isContentValid(st.getName())){// we just check against the configured types.

				List<Field> fields = FieldsCache.getFieldsByStructureInode(con.getStructureInode());

				Host host = APILocator.getHostAPI().find(con.getHost(), APILocator.getUserAPI().getSystemUser(), false);
				
				loadCategories(con, m);

				m.put("identifier", ident.getInode());
				m.put("inode", con.getInode()); 

				// here we will have the structure type and then all fields for the structure
				typeMap.put(st.getName(), ObjectMapperHelper.getInstance().getAsJsonString(loadFields(con, fields)));
				m.put("type","");

				m.put("site", host.getHostname());
				
				loadFieldsMap=loadFields(con, fields);
				
				if(StringUtils.equalsIgnoreCase(st.getVelocityVarName(), "LeadershipBio")){
					String fname="";
					String lname="";
					for(Entry<String,String> entry : loadFieldsMap.entrySet()){
						if(StringUtils.equalsIgnoreCase(entry.getKey(), "firstname")){
							fname=entry.getValue();
						}
						if(StringUtils.equalsIgnoreCase(entry.getKey(), "lastname")){
							lname=entry.getValue();
						}
						
					}
					
					m.put("title", fname + " " +lname);
				}else {
					m.put("title", con.getTitle());
				}
				
				
				m.put("timestamp", ObjectMapperHelper.datetimeFormat.format(con.getModDate()));
				m.put("ttl", "");// for content there is no ttl so just leave blank - more of a file and .dot page related field - just ensure it is here
				// for the contentlets

				String urlMap = null;
				try{
					urlMap = APILocator.getContentletAPI().getUrlMapForContentlet(con, APILocator.getUserAPI().getSystemUser(), true);
					if(urlMap != null){
						m.put("url",urlMap );	
					}
				}
				catch(Exception e){
					Logger.warn(ESContentletMapper.class, "Cannot get URLMap for contentlet.id : " + ((ident != null) ? ident.getInode() : con) + " , reason: "+e.getMessage());
					throw new DotRuntimeException(urlMap, e);
				}

				
				m.put("content", getContent(con.getTitle(), st.getName(), loadFieldsMap));// here we will have only the fields marked to be indexed.            
				m.put("tags", con.get("tags") ==  null ? "" : con.get("tags").toString());// here we need to figure out how we get the tags field
				m.put("shortDescr", st.getDescription());
				if(con.get("tags") !=null && StringUtils.isNotBlank(con.get("tags").toString())){
					if(StringUtils.contains(con.get("tags").toString(), "featuredInSearch"))
					{
						m.put("featured","true");
					}
					else
					{
						m.put("featured", "false");
					}
						     
				}
				else{
					m.put("featured", "false");
				}
				        
				

				

				x = ObjectMapperHelper.getInstance().getAsJsonString(m);
				Logger.debug(ESContentletMapper.class, "*****JSON FOR CONTENTLET : " + x);
			}
		} catch (Exception e) {
			Logger.error(ESContentletMapper.class, e.getMessage(), e);
		}
		return x;
	}

	/**
	 * This method loads the needed categories into the given map by reference. Any category field found for the list of Fields
	 * will be added to this map.
	 * @param con
	 * @param m
	 * @throws DotDataException
	 * @throws DotSecurityException
	 */
	@SuppressWarnings("unchecked")
	private static void loadCategories(Contentlet con, Map<String,String> m) throws DotDataException, DotSecurityException {
		// first we check if there is a category field in the structure. We don't hit db if not needed
		boolean thereiscategory=false;
		List<Field> fields=FieldsCache.getFieldsByStructureInode(con.getStructureInode());
		for(Field f : fields)
			thereiscategory = thereiscategory ||
			f.getFieldType().equals(FieldType.CATEGORY.toString());

		String categoriesString="";

		if(thereiscategory) {
			String categoriesSQL = "select category.category_velocity_var_name as cat_velocity_var "+
					" from  category join tree on (tree.parent = category.inode) join contentlet c on (c.inode = tree.child) " +
					" where c.inode = ?";
			DotConnect db = new DotConnect();
			db.setSQL(categoriesSQL);
			db.addParam(con.getInode());
			ArrayList<String> categories=new ArrayList<String>();
			List<HashMap<String, String>> categoriesResults = db.loadResults();
			for (HashMap<String, String> crow : categoriesResults)
				categories.add(crow.get("cat_velocity_var"));

			categoriesString= join(categories, " ").trim();

			for(Field f : fields) {
				if(f.getFieldType().equals(FieldType.CATEGORY.toString())) {
					String catString="";
					if(!categories.isEmpty()) {
						String catId=f.getValues();

						// we get all subcategories (recursive)
						Category category=APILocator.getCategoryAPI().find(catId, APILocator.getUserAPI().getSystemUser(), false);
						List<Category> childrens=APILocator.getCategoryAPI().getAllChildren(
								category, APILocator.getUserAPI().getSystemUser(), false);

						// we look for categories that match childrens for the
						// categoryId of the field
						ArrayList<String> fieldCategories=new ArrayList<String>();
						for(String catvelvarname : categories)
							for(Category chCat : childrens)
								if(chCat.getCategoryVelocityVarName().equals(catvelvarname))
									fieldCategories.add(catvelvarname);

						// after matching them we create the JSON field
						if(!fieldCategories.isEmpty()){
							catString= join(fieldCategories, " ").trim();
						}
					}
					m.put(f.getVelocityVarName(), catString);
				}
			}
		}

		m.put("categories", categoriesString);
	}

	/**
	 * Loads all Fields that are used by this piece of content into a map of field names as the key and field values as the map values.
	 * This method returns all fields and doesn't do any filtering.
	 * @param con
	 * @param allFields
	 * @return
	 * @throws DotDataException
	 */
	private static Map<String, String> loadFields(Contentlet con, List<Field> fields) throws DotDataException {
		FieldAPI fAPI=APILocator.getFieldAPI();
		Map<String, String> returnMap = new HashMap<String, String>();

		for (Field f : fields) {
			if (f.getFieldType().equals(Field.FieldType.BINARY.toString())
					|| f.getFieldContentlet() != null && f.getFieldContentlet().startsWith("system_field")) {
				continue;
			}

			try {
				if(fAPI.isElementConstant(f)){
					returnMap.put(f.getVelocityVarName(), (f.getValues() == null ? "":f.getValues().toString()));
					continue;
				}

				Object valueObj = con.get(f.getVelocityVarName());
				if(valueObj == null){
					valueObj = "";
				}
				if (f.getFieldContentlet().startsWith("section_divider")) {
					valueObj = "";
				}                

				if(!UtilMethods.isSet(valueObj)) {
					returnMap.put(f.getVelocityVarName(), "");
				}else if(f.getFieldType().equals(FieldType.TIME.toString())) {
					String timeStr=ObjectMapperHelper.timeFormat.format(valueObj);
					returnMap.put(f.getVelocityVarName(), timeStr);
				}else if (f.getFieldType().equals(FieldType.DATE.toString())) {
					try {
						String dateString = ObjectMapperHelper.dateFormat.format(valueObj);
						returnMap.put(f.getVelocityVarName(), dateString);
					}
					catch(Exception ex) {
						returnMap.put(f.getVelocityVarName(),"");
					}
				}else if(f.getFieldType().equals(FieldType.DATE_TIME.toString())) {
					try {
						String datetimeString = ObjectMapperHelper.datetimeFormat.format(valueObj);
						returnMap.put(f.getVelocityVarName(), datetimeString);
					}
					catch(Exception ex) {
						returnMap.put(f.getVelocityVarName(),"");
					}
				} else if (f.getFieldType().equals(FieldType.CHECKBOX.toString()) || f.getFieldType().equals(FieldType.MULTI_SELECT.toString())) {
					if (f.getFieldContentlet().startsWith("bool")) {
						returnMap.put(f.getVelocityVarName(), valueObj.toString());
					} else {
						returnMap.put(f.getVelocityVarName(), UtilMethods.listToString(valueObj.toString()));
					}
				} else if (f.getFieldType().equals("key_value")){
					returnMap.put(f.getVelocityVarName(), (String)valueObj);
					Map<String,Object> keyValueMap = KeyValueFieldUtil.JSONValueToHashMap((String)valueObj);

					if(keyValueMap!=null && !keyValueMap.isEmpty()){
						for(String key : keyValueMap.keySet()){
							returnMap.put(key, (String)keyValueMap.get(key));                			    
						}
					}
				} else {
					if (f.getFieldContentlet().startsWith("bool")) {
						returnMap.put(f.getVelocityVarName(), valueObj.toString());
					} else if (f.getFieldContentlet().startsWith("float") || f.getFieldContentlet().startsWith("integer")) {
						returnMap.put(f.getVelocityVarName(), ObjectMapperHelper.numFormatter.format(valueObj));
					} else {
						returnMap.put(f.getVelocityVarName(), valueObj.toString());
					}
				}
			} catch (Exception e) {
				Logger.warn(ESContentletMapper.class, "Error indexing field: " + f.getFieldName()
						+ " of contentlet: " + con.getInode(), e);
				throw new DotDataException(e.getMessage(),e);
			}
		}
		return returnMap;
	}

	/**
	 * Utility method that will add all String in the <code>strList</code> and separating them using
	 * the specified <code>sprarator</code>.
	 * @param strList
	 * @param separator
	 * @return
	 */
	private static String join(List<String> strList, String separator) {
		StringBuilder strBuff = new StringBuilder();
		for (String str : strList)
			strBuff.append(str).append(separator);
		return strBuff.toString();
	}
	
	/**
	 * Will take in a map of all fields gathered for the given piece of content. Will then create the 
	 * Content Field needed for the ES Index.  This is based on the <code>structureName</code>. A list
	 * of fields used per <code>structureName</code> is provided at the class level comments.
	 * @param title
	 * @param structureName
	 * @param m
	 * @return
	 */
	private static String getContent(String title, String structureName, Map<String, String> m) {
		Map<String, String> orderedFields = new HashMap<String, String>();
		if(structureName.toLowerCase().equals("event (mlh)") 
				|| structureName.toLowerCase().equals("facility")
				|| structureName.toLowerCase().equals("article")){
			orderedFields.put("title", title);
		}
		for(Entry<String,String> entry : m.entrySet()){	
			if(isFieldValidForType(structureName, entry)){
				orderedFields.put(entry.getKey().toLowerCase(), entry.getValue() == null ? "" : entry.getValue().toString());
			}
		}
		// now we gather the string based on specific ordering 
		return getOrderedContentStringFromMapAndStructureName(structureName, orderedFields);
	}

	/**
	 * Makes sure we have the right field for the given content. Please see the list provided at the 
	 * class level to see which fields are used for the ES Content Index field. 
	 * @param name
	 * @param entry
	 * @return
	 */
	private static boolean isFieldValidForType(String name, Entry<String, String> entry) {
		boolean ret = false;
		if(name.toLowerCase().equals("article"))
		{
			if(entry.getKey().toLowerCase().equals("headline")
					|| entry.getKey().toLowerCase().equals("bodytext")
					|| entry.getKey().toLowerCase().equals("teaser")
					|| entry.getKey().toLowerCase().equals("tags")){
				ret = true;
			}
		}
		else if(name.toLowerCase().equals("event (mlh)")){
			if(entry.getKey().toLowerCase().equals("bodytext")
					|| entry.getKey().toLowerCase().equals("tags")){
				ret = true;
			}
		}
		else if(name.toLowerCase().equals("facility")){
			if(entry.getKey().toLowerCase().equals("bodytext")
				|| entry.getKey().toLowerCase().equals("servicesoffered")
				|| entry.getKey().toLowerCase().equals("careerstab")
				|| entry.getKey().toLowerCase().equals("tab2content")
				|| entry.getKey().toLowerCase().equals("tab3content")
				|| entry.getKey().toLowerCase().equals("additionalinformation")
				|| entry.getKey().toLowerCase().equals("tags")
			){
				ret = true;
			}
			
			
			//
		}
		else if(name.toLowerCase().equals("leadership bio")){
			if(entry.getKey().toLowerCase().equals("firstname")
					|| entry.getKey().toLowerCase().equals("lastname")
					|| entry.getKey().toLowerCase().equals("keywords")
					|| entry.getKey().toLowerCase().equals("bodytext")
					|| entry.getKey().toLowerCase().equals("tags")){
				ret = true;
			}
		}else if(name.toLowerCase().equals("physician")){
			if(entry.getKey().toLowerCase().equals("firstname")
					|| entry.getKey().toLowerCase().equals("lastname")
					|| entry.getKey().toLowerCase().equals("tags")){
				ret = true;
			}
		}
		return ret;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	private static boolean isContentValid(String name) {
		boolean ret = false;
		if(name.toLowerCase().equals("article")
				|| name.toLowerCase().equals("event (mlh)") 
				|| name.toLowerCase().equals("facility")
				|| name.toLowerCase().equals("leadership bio")
				|| name.toLowerCase().equals("physician")){
			ret = true;
		}
		return ret;
	}
	
	/**
	 * This method provides the ordering of the fields for the given <code>structureName</code>.  Will return a string in the correct
	 * order for indexing for the given <code>structureName</code>.
	 * @param structureName
	 * @param orderedFields
	 * @return
	 */
	private static String getOrderedContentStringFromMapAndStructureName(String structureName, Map<String, String> orderedFields) {
		StringBuilder x =  new StringBuilder();
		
		x.append("");
		
		if(structureName.toLowerCase().equals("article")){
			x.append(orderedFields.get("title") + " ");
			x.append(orderedFields.get("headline") + " ");		
			x.append(Jsoup.parse(orderedFields.get("bodytext")).text().toString() + " ");
			x.append(Jsoup.parse(orderedFields.get("teaser")).text().toString() + " ");
			
			if(StringUtils.isNotEmpty(orderedFields.get("tags"))){
				x.append(orderedFields.get("tags"));
			}
			
		}
		else if(structureName.toLowerCase().equals("event (mlh)")){
			x.append(orderedFields.get("title") + " ");
			x.append(Jsoup.parse(orderedFields.get("bodytext")).text().toString() + " ");
			if(StringUtils.isNotEmpty(orderedFields.get("tags"))){
				x.append(orderedFields.get("tags"));
			}
		}
		else if(structureName.toLowerCase().equals("facility")){
			x.append(orderedFields.get("title") + " ");
			
			x.append(Jsoup.parse(orderedFields.get("bodytext")).text().toString() + " ");
			x.append(Jsoup.parse(orderedFields.get("servicesoffered")).text().toString() + " ");
			x.append(Jsoup.parse(orderedFields.get("careerstab")).text().toString() + " ");
			x.append(Jsoup.parse(orderedFields.get("tab2content")).text().toString() + " ");
			x.append(Jsoup.parse(orderedFields.get("tab3content")).text().toString() + " ");
			x.append(Jsoup.parse(orderedFields.get("additionalinformation")).text().toString() + " ");
			
			if(StringUtils.isNotEmpty(orderedFields.get("tags"))){
				x.append(orderedFields.get("tags"));
			}
		}
		else if(structureName.toLowerCase().equals("leadership bio")){
			
			x.append(orderedFields.get("firstname") + " ");
			x.append(orderedFields.get("lastname") + " ");
			x.append(orderedFields.get("keywords") + " ");
			
			x.append(Jsoup.parse(orderedFields.get("bodytext")).text().toString() + " ");
			if(StringUtils.isNotEmpty(orderedFields.get("tags"))){
				x.append(orderedFields.get("tags"));
			}
		}else if(structureName.toLowerCase().equals("physician")){
			x.append(orderedFields.get("firstname") + " ");
			x.append(orderedFields.get("lastname") + " ");
			
			
			if(StringUtils.isNotEmpty(orderedFields.get("tags"))){
				x.append(orderedFields.get("tags"));
			}
		}
		return x.toString();
	}
	/*
	 * 
	 */
}
