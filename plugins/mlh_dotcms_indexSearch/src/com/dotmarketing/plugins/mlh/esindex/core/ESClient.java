/**
 * 
 */
package com.dotmarketing.plugins.mlh.esindex.core;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Logger;


/**
 * This class allow access to a single ES Client.  Ensures we always have an ES Client available and ensures
 * we only ever use one ES Client, as this class follow as Singleton pattern.
 * @author Nicholas Padilla
 *
 */
public class ESClient {

	private static Client client;

	private ESClient(){}

	/**
	 * Return the current initialized client or initialize and return a new client. Uses plugin-properties file
	 * to get at the ES Cluster Name, ES Host, ES Port.  This also attempts to register a shutdown hook that should
	 * shut down the ES Client if not null.  This is unreliable and a work around should be found at some point. 
	 * @return
	 */
	public static Client getClient(){
		if(client == null){
			try {
				String clusterName = APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.cluster.name");			
				String host = APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.host");
				String port = APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.port");
				
				Logger.debug(ESClient.class, "****** Client Created : clusterName - " + clusterName + "     host - " + host + "     port - " + port);
				
				// defaulted to 'elasticsearch' - this should be the default setting in the properties file.
				Builder settingsBuilder = ImmutableSettings.settingsBuilder()
						.put("cluster.name", clusterName)
						.put("config.ignore_system_properties", true);
						
				client = new TransportClient(settingsBuilder.build()).addTransportAddress(new InetSocketTransportAddress(host, Integer.parseInt(port)));
				
				// register our hook so that we properly close the client - don't want to create any leaks. However, doesn't seem to always work.
				Runtime.getRuntime().addShutdownHook( new Hook() );
			} catch (DotDataException e) {
				e.printStackTrace();
			}
		}
		return client;
	}
	
	private static class Hook extends Thread {
		public void run() {
			Logger.info(ESClient.class, "Running Clean Up ES Indexing Plugin...");
			if(client != null){
				client.close();
			}
		}
	}
	
	public static void removeFromIndex(String id) throws Exception {
		String indexName = APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.indexName");
		
		BulkRequestBuilder bulk = getClient().prepareBulk();
		
		bulk.add(client.prepareDelete(indexName, "content", id));

		bulk.execute().actionGet();
		
		Logger.debug(ESClient.class, "-|-|--|-|--|-|- remove item ");
	}

}
