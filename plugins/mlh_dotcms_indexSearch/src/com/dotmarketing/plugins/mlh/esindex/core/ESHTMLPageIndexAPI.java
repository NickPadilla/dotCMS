package com.dotmarketing.plugins.mlh.esindex.core;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotHibernateException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

/**
 * Class handles the details of publish/unpublish of a HTMLPage into the ES Index.
 * @author Nicholas Padilla
 *
 */
public class ESHTMLPageIndexAPI {

	private ESHTMLPageIndexAPI() {}
	
	/**
	 * Handles a single HTMLPage for initial insert or update of an existing index.
	 * @param page
	 * @throws DotDataException
	 * @throws DotSecurityException
	 */
	public static void addHTMLPageToIndex(final HTMLPage page) throws DotDataException, DotSecurityException {

		if(page==null || !UtilMethods.isSet(page.getIdentifier())) return;

		Client client= ESClient.getClient();
		BulkRequestBuilder req = client.prepareBulk();

		List<HTMLPage> contentToIndex=new ArrayList<HTMLPage>();
		contentToIndex.add(page);

		indexHTMLPageList(req, contentToIndex);

		if(req.numberOfActions()>0){
			Logger.debug(ESHTMLPageIndexAPI.class, "-|-|--|-|--|-|- add htmlpage item ");
			req.execute().actionGet();
		}
	}
	
	/**
	 * Removes the given HTMLPage identifier in the ES index. If no identifier exists in the ES Index then nothing happens.
	 * @param identifier
	 * @throws DotHibernateException
	 */
	public static void removeIdentifierFromIndex(final String identifier) throws DotHibernateException {
		
		if(identifier==null || identifier.isEmpty()){
			 Logger.warn(ESHTMLPageIndexAPI.class, "Calling Delete Method with an invalid identifier.");
			return;
		}
		
		try {
			Logger.debug(ESHTMLPageIndexAPI.class, "-|-|--|-|--|-|- remove htmlpage item ");
			ESClient.removeFromIndex(identifier);
		} catch (Exception ex) {
			throw new ElasticSearchException(ex.getMessage(),ex);
		}
	}
	
	/**
	 * Here we add/update the given list of HTMLPages.
	 * @param req
	 * @param contentToIndex
	 * @throws DotDataException
	 */
	private static void indexHTMLPageList(BulkRequestBuilder req, List<HTMLPage> contentToIndex) throws DotDataException {
		for(HTMLPage con : contentToIndex) {
			Logger.debug(ESHTMLPageIndexAPI.class, "****** HTMLPage : " + con);
			String id=con.getIdentifier();
			String content = ESHTMLPageMapper.toJson(con);
			if(!content.isEmpty()){
				req.add(new IndexRequest(APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.indexName"), 
						APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.indexType"), id).source(content));
			}
		}
	}

}