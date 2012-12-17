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
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

/**
 * This class performs the functions needed to index {@link Contentlet}'s.  Uses {@link ESClient} functionality to remove
 * indexed content and uses the {@link ESContentletMapper} to parse the content into a JSON string.
 * @author Nicholas Padilla
 *
 */
public class ESContentletIndexAPI {

	private ESContentletIndexAPI() {}

	/**
	 * Adds content to the ES index or updates the currently indexed content with the given identifier. 
	 * @param content
	 * @throws DotDataException
	 * @throws DotSecurityException
	 */
	public static void addContentToIndex(final Contentlet content) throws DotDataException, DotSecurityException {

		if(content==null || !UtilMethods.isSet(content.getIdentifier())) return;

		Client client= ESClient.getClient();
		BulkRequestBuilder req = client.prepareBulk();

		List<Contentlet> contentToIndex=new ArrayList<Contentlet>();
		contentToIndex.add(content);

		indexContentletList(req, contentToIndex);

		if(req.numberOfActions()>0){
			Logger.debug(ESContentletIndexAPI.class, "-|-|--|-|--|-|- publishing contentlet item ");
			req.execute().actionGet();
		}
	}

	/**
	 * Removes the given content based on the given content's identifier.
	 * @param content
	 * @throws DotHibernateException
	 */
	public static void removeContentFromIndex(final Contentlet content) throws DotHibernateException {

		if(content==null || !UtilMethods.isSet(content.getIdentifier())){
			 Logger.warn(ESContentletIndexAPI.class, "Calling Delete Method with an invalid content.getIdentifier()");
			return;
		}

		try {
			Logger.debug(ESContentletIndexAPI.class, "-|-|--|-|--|-|- removing contentlet item ");
			ESClient.removeFromIndex(content.getIdentifier());
		} catch (Exception ex) {
			throw new ElasticSearchException(ex.getMessage(),ex);
		}
	}


	/**
	 * Indexes a list of {@link Contentlet}'s, will parse each piece of content into a JSON string and index to ES based on
	 * the identifier. 
	 * @param req
	 * @param contentToIndex
	 * @throws DotDataException
	 */
	private static void indexContentletList(BulkRequestBuilder req, List<Contentlet> contentToIndex) throws DotDataException {
		for(Contentlet con : contentToIndex) {
			String id=con.getIdentifier();
			String content = ESContentletMapper.toJson(con);
			Logger.debug(ESContentletIndexAPI.class, "****** Content : " + content);
			if(!content.isEmpty()){
				req.add(new IndexRequest(APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.indexName"), 
						APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.indexType"), id).source(content));
			}
		}
	}

}