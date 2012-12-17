package com.dotmarketing.plugins.mlh.esindex.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;

/**
 * This class handles mapping a File object to the required JSON String format required by the requirements. We use the Tika file parsing library
 * to handle the details of the file parsing. Here are the requirements needed :
 * <ul> File Type <b>-</b> Layout
 * <li>PDF File <b>-</b> Title, Raw Contents of file</li>
 * <li>Word Doc File <b>-</b> Title, Raw Contents of file</li>
 * <li>Text File <b>-</b> Title, Raw Contents of file</li>
 * </ul>
 * 
 * Currently we limit the number of chars to : 10*1024*1024 
 * @author Nicholas Padilla
 */
public class ESFileMapper {
	
	private static final String PDF_MIMETYPE = Config.CONTEXT.getMimeType("test.pdf");
	private static final String DOC_MIMETYPE = Config.CONTEXT.getMimeType("test.doc");
	private static final String TXT_MIMETYPE = Config.CONTEXT.getMimeType("test.txt");

	/**
	 * Method handles the parsing of the given file.  Must be one of the supported file types, that restriction happens further up 
	 * the chain - but others needing to update/add functionality must not pass in invalid file types. We pass all file fields in the 
	 * Type index file and only the fields specified at the top of this class.  Also we respect the ordering of the fields as specified
	 * by the list at the top of this class.
	 * @param file
	 * @return
	 */
	public static String toJson(File file) {		

		String x = ""; 
		try {
			Map<String,String> m = new HashMap<String,String>();
			Map<String,String> typeMap = new HashMap<String,String>();
			
			File newFile = APILocator.getFileAPI().find(file.getInode(), APILocator.getUserAPI().getSystemUser(), false);
			
			Identifier ident = APILocator.getIdentifierAPI().find(file.getIdentifier());
			Host host = APILocator.getHostAPI().find(ident.getHostId(), APILocator.getUserAPI().getSystemUser(), false);

			m.put("identifier", ident.getInode());
			m.put("inode", newFile.getInode()); 
			m.put("site", host.getHostname());
			m.put("title", newFile.getNameOnly());
			m.put("timestamp", ObjectMapperHelper.datetimeFormat.format(newFile.getModDate()));
			m.put("ttl", "");
			
			typeMap.put("file", ObjectMapperHelper.getInstance().getAsJsonString(loadFileMap(newFile)));
			// here we will have the structure type and then all fields for the structure
			m.put("type","");
			m.put("url", newFile.getURI());
			m.put("content", newFile.getNameOnly());
			m.put("tags",  newFile.getFriendlyName());// here files have no tags so just leave blank
			m.put("shortDescr", newFile.getTitle());
			
			// there are no categories that are associated with this object - that I can find anyway - so just keep it blank
			m.put("categories", newFile.getCategories() == null ? "" : file.getCategories().toString());
			 
			if(StringUtils.isNotEmpty(newFile.getFriendlyName()))
			{
				if(StringUtils.contains(newFile.getFriendlyName(), "featuredInSearch"))
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
			/*            
             * m.put("tagclicked", "0");
             * m.put("tagclicked", "0");
             */              

			x = ObjectMapperHelper.getInstance().getAsJsonString(m);
			Logger.debug(ESFileMapper.class, "*****JSON FOR FILE : " + x);
		} catch (Exception e) {
			Logger.error(ESFileMapper.class, e.getMessage(), e);
		}
		return x;
	}
	
	/**
	 * This method handles changing a File to a map of field names as the keys and field values as the map values. 
	 * @param file
	 * @return
	 */
	private static Map<String, String> loadFileMap(File file) {
		Map<String,String> m = new HashMap<String,String>();
		m.put("friendlyName", file.getFriendlyName());
		m.put("author", file.getAuthor());
		m.put("extension()", file.getExtension());
		m.put("fileName", file.getFileName());
		m.put("mimeType", file.getMimeType());		
		m.put("title", file.getTitle());	
		m.put("type", file.getType());	
		m.put("uri", file.getURI());	
		m.put("nameOnly", file.getNameOnly());	
		m.put("size", Integer.toString(file.getSize()));		
		m.put("categories", file.getCategories() == null ? "" : file.getCategories().toString());	
		m.put("modDate", ObjectMapperHelper.datetimeFormat.format(file.getModDate()));	
		m.put("publishDate", ObjectMapperHelper.datetimeFormat.format(file.getPublishDate()));	
		return m;
	}	

	/**
	 * Check to ensure we only manipulate .pdf, .doc, or .txt files.
	 * @param file
	 * @return
	 */
	public static boolean isValidFile(File file) {
		boolean ret = false;
		//getting mime type
		String mimeType = APILocator.getFileAPI().getMimeType(file.getFileName().toLowerCase());
		// only index if we have a valid mime type
		if(mimeType.equals(PDF_MIMETYPE) || mimeType.equals(DOC_MIMETYPE) || mimeType.equals(TXT_MIMETYPE)){
			ret = true;
		}
		return ret;
	}
	
}
