package com.dotmarketing.plugins.mlh.esindex.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.UtilMethods;

/**
 * Utility class to handle Key Value field 
 * @author Roger 
 *
 */
public class KeyValueFieldUtil {
	
	private static final JsonFactory factory = new JsonFactory(); 
	private static final ObjectMapper mapper = new ObjectMapper(factory); 
	
	/**
	 * We use a JSON ObjectMapper to read the k/v pairs into a Map.  This is used when generating the {@link Contentlet}'s fields into 
	 * a JSON String.  Some fields may be in k/v format.
	 * @param json
	 * @return
	 */
	public static Map<String,Object> JSONValueToHashMap(final String json){
		LinkedHashMap<String,Object> keyValueMap  = new LinkedHashMap<String,Object>();
		if(UtilMethods.isSet(json)){
			TypeReference<LinkedHashMap<String,Object>> typeRef = new TypeReference<LinkedHashMap<String,Object>>() {}; 
			try {
				keyValueMap = mapper.readValue(json, typeRef);
			} catch (Exception e) {
				//TODO
			} 
		}
		return keyValueMap;
	}
	

}
