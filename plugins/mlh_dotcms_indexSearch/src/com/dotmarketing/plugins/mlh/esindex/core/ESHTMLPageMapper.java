package com.dotmarketing.plugins.mlh.esindex.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.util.Logger;

/**
 * This class is responsible for taking a HTMLPage and parsing it into a JSON string that represents it for the index. Here are the 
 * requirements needed for the Content Index field in ES: 
 * <ul>
 * <li>Page/Menu Title, Body string*, SEO Keywords (this is also used as the "tags" field)</li>
 * <li>Use HTML parser to (1) get string of contents between body tags and (2) strip all HTML tags</li>
 * </ul>
 * The Type field in the Index will contain all the fields associated with the HTMLPage.  The HTML is parsed by the Jsoup library.
 * @author Nicholas Padilla
 */
public class ESHTMLPageMapper {

	/**
	 * Parses the given HTMLPage into a JSON string.  Please see the class level comments for the Content field structure. 
	 * @param page
	 * @return
	 */
	public static String toJson(HTMLPage page) {		
		String x = ""; 
		try {
			Map<String,String> m = new HashMap<String,String>();
			Map<String, String> htmlPage = new HashMap<String,String>();
			
			Identifier ident = APILocator.getIdentifierAPI().find(page.getIdentifier());
			Host host = APILocator.getHostAPI().find(ident.getHostId(), APILocator.getUserAPI().getSystemUser(), false);

			m.put("identifier", ident.getInode());
			m.put("inode", page.getInode()); 
			m.put("site", host.getHostname());
			m.put("title", page.getTitle());
			m.put("timestamp", ObjectMapperHelper.datetimeFormat.format(page.getModDate()));
			m.put("ttl", Long.toString(page.getCacheTTL()));
			
			// type is htmlPage with page and relatedContent as fields
			htmlPage.put("htmlPage", ObjectMapperHelper.getInstance().getAsJsonString(loadPageMap(page)));
			// here we will have the structure type and then all fields for the structure
			m.put("type","");
			m.put("url", page.getURI());

			
			//URL myURL = new URL("http://"+host.getHostname()+page.getURI());
			org.jsoup.nodes.Document doc = Jsoup.parse(APILocator.getHTMLPageAPI().getHTML(page, page.isLive()));
			StringBuilder pageContentSB = new StringBuilder();
			StringBuilder contentSB = new StringBuilder();
			
			pageContentSB.append("");
			
			if(doc!=null && doc.getElementById("main")!=null)
	        {
	        	boolean idMatch = false;
	        	if (doc.getElementById("article_Text")!=null) {
	        		pageContentSB.append( " " + doc.getElementById("article_Text").text());
					idMatch = true;
				}
	        	if (doc.getElementById("sectionListing")!=null) {
					pageContentSB.append( " " + doc.getElementById("sectionListing").text());
					idMatch = true;
				}
				if(!idMatch){
					pageContentSB.append(doc.getElementById("main").text());
				}
	        }
			
			contentSB.append(page.getTitle());
			
			if(StringUtils.isNotEmpty(page.getSeoKeywords())){
				contentSB.append(page.getSeoKeywords());
			}
			
			
			
			if (StringUtils.isNotEmpty(pageContentSB.toString())) {
				
				contentSB.append(pageContentSB.toString());
				
				if (pageContentSB.length() > 301)
				{
					m.put("shortDescr", pageContentSB.substring(0, 300));

				}
				else {
					m.put("shortDescr",pageContentSB.substring(0, pageContentSB.length() - 1));
					
				}
			}
			else {
				m.put("shortDescr", page.getSeoDescription());
			}
			
			
			
			m.put("content", contentSB.toString());// here we will have only the fields marked to be indexed.            
			m.put("tags", page.getSeoKeywords());// here we need to figure out how we get the tags field
			
			
			// there are no categories that are associated with this object - that I can find anyway - so just keep it blank
			m.put("categories", "");	
			
			if(StringUtils.isNotEmpty(page.getSeoKeywords()))
			{
				if(StringUtils.contains(page.getSeoKeywords(), "featuredInSearch"))
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
			Logger.debug(ESHTMLPageMapper.class, "*****JSON FOR HTMLPage : " + x);
		} catch (Exception e) {
			Logger.error(ESHTMLPageMapper.class, e.getMessage(), e);
		}
		return x;
	}
	
	/**
	 * Creats a map of HTMLPage field values.  The keys are the field names and the map values are the field values.
	 * @param page
	 * @return
	 * @throws DotDataException 
	 * @throws DotStateException 
	 */
	private static Map<String, String> loadPageMap(HTMLPage page) throws DotStateException, DotDataException {
		Map<String,String> m = new HashMap<String,String>();
		m.put("friendlyName", page.getFriendlyName());
		m.put("metadata", page.getMetadata());
		m.put("pageUrl", page.getPageUrl());
		m.put("seoDescription", page.getSeoDescription());
		m.put("seoKeywords", page.getSeoKeywords());		
		m.put("title", page.getTitle());	
		m.put("type", page.getType());	
		m.put("uri", page.getURI());	
		m.put("webEndDate", page.getWebEndDate());	
		m.put("webStartDate", page.getWebStartDate());	
		m.put("endDate", ObjectMapperHelper.datetimeFormat.format(page.getEndDate()));	
		m.put("startDate", ObjectMapperHelper.datetimeFormat.format(page.getStartDate()));	
		return m;
	}


}
