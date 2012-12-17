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
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

/**
 * This class performs the needed functionality to gather a File fields and content into a JSON string used for ES Indexing.
 * Currently this class only handles Files of these types:
 * <ul>
 * <li>PDF</li>
 * <li>DOC</li>
 * <li>TXT</li>
 * </ul>
 * @author Nicholas Padilla
 *
 */
public class ESFileIndexAPI {

	private ESFileIndexAPI() {}
	
	/**
	 * Adds or updates the given file in the ES Index. All files that are not listed at the class level comments will be ignored. 
	 * @param file
	 * @throws DotDataException
	 * @throws DotSecurityException
	 */
	public static void addFileToIndex(final File file) throws DotDataException, DotSecurityException {
		// ensure we have a file that is valid
		if(file==null || !UtilMethods.isSet(file.getIdentifier()) || !ESFileMapper.isValidFile(file)) return;

		Client client= ESClient.getClient();
		BulkRequestBuilder req = client.prepareBulk();

		List<File> contentToIndex=new ArrayList<File>();
		contentToIndex.add(file);

		indexFileList(req, contentToIndex);

		if(req.numberOfActions()>0){
			Logger.debug(ESFileIndexAPI.class, "-|-|--|-|--|-|- add file item ");
			req.execute().actionGet();
		}
	}

	/**
	 * Method handles the removal of the given File identifier from the ES Index.
	 * @param identifier
	 * @throws DotHibernateException
	 */
	public static void removeIdentifierFromIndex(final String identifier) throws DotHibernateException {
		
		if(identifier==null || identifier.isEmpty()){
			 Logger.warn(ESFileIndexAPI.class, "Calling Delete Method with an invalid identifier.");
			return;
		}
		
		try {
			Logger.debug(ESFileIndexAPI.class, "-|-|--|-|--|-|- remove file item ");
			ESClient.removeFromIndex(identifier);
		} catch (Exception ex) {
			throw new ElasticSearchException(ex.getMessage(),ex);
		}
	}
	
	/**
	 * Method handles the index/update of a List of Files. 
	 * @param req
	 * @param files
	 * @throws DotDataException
	 */
	private static void indexFileList(BulkRequestBuilder req, List<File> files) throws DotDataException {
		for(File file : files) {
			Logger.debug(ESFileIndexAPI.class, "****** File : " + file);
			String id=file.getIdentifier();
			String content = ESFileMapper.toJson(file);
			if(!content.isEmpty()){
				req.add(new IndexRequest(APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.indexName"), 
						APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.indexType"), id).source(content));
			}
		}
	}

}