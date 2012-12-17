/**
 * 
 */
package com.dotmarketing.plugins.mlh.esindex.core;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.time.FastDateFormat;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class holds all functions that are the same throughout the object mapping requirements.  This is a singleton and so only one instance 
 * will ever be used - best for performance reasons.  If you need the {@link #getMapper()} or the {@link #getAsJsonString(Map)} then you need to 
 * call {@link #getInstance()} first.
 * @author Nicholas Padilla
 *
 */
public class ObjectMapperHelper {
	public static final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyyMMdd");
	public static final FastDateFormat datetimeFormat = FastDateFormat.getInstance("yyyyMMddHHmmss");

	public static final FastDateFormat timeFormat = FastDateFormat.getInstance("HHmmss");
	public static final DecimalFormat numFormatter = new DecimalFormat("0000000000000000000.000000000000000000");

	private ObjectMapper mapper = null;

	private static ObjectMapperHelper esObjectMapper;

	private ObjectMapperHelper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
			ThreadSafeSimpleDateFormat df = new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			mapper.setDateFormat(df);
		}
	}

	/**
	 * Provides a valid ObjectMapper instance to use.
	 * @return
	 */
	public static ObjectMapperHelper getInstance(){
		if(esObjectMapper == null){
			esObjectMapper = new ObjectMapperHelper();
		}
		return esObjectMapper;
	}
	
	/**
	 * Get the currently build out ObjectMapper, can only get at this after calling {@link #getInstance()}.
	 * @return
	 */
	public ObjectMapper getMapper(){
		return mapper;
	}

	/**
	 * This method will parse the given Map of values to a JSON String.  Will use the already constructed ObjectMapper, to use this
	 * method you must call {@link #getInstance()}.
	 * @param m
	 * @return
	 * @throws IOException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 */
	public String getAsJsonString(Map<String, String> m)
			throws IOException, JsonGenerationException, JsonMappingException {
		String x;
		Map<String,Object> mlowered=new HashMap<String,Object>();
		for(Entry<String,String> entry : m.entrySet()){
			
			mlowered.put(entry.getKey().toLowerCase(), entry.getValue() == null ? "" : entry.getValue().toString());
		}
		x = mapper.writeValueAsString(mlowered);
		return x;
	}
}
