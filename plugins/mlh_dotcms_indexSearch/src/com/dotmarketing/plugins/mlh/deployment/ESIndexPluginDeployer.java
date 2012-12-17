package  com.dotmarketing.plugins.mlh.deployment;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.plugin.PluginDeployer;
import com.dotmarketing.plugins.mlh.esindex.core.ESClient;

/**
 * This class handles the creation of the needed index in ElasticSearch.
 * This is really only needed if one is running in a development environment.
 * To get this to run after initial deployment, increment the Plugin-Version in the
 * MANIFEST.MF and make sure the Deploy-Class setting in the MANIFEST.MF file points
 * to this class.
 * @author Nicholas Padilla
 *
 */
public class ESIndexPluginDeployer implements PluginDeployer {

	/* (non-Javadoc)
	 * @see com.dotmarketing.plugin.PluginDeployer#deploy()
	 */
	public boolean deploy() {
		return createIndex();
	}

	/* (non-Javadoc)
	 * @see com.dotmarketing.plugin.PluginDeployer#redeploy(java.lang.String)
	 */
	public boolean redeploy(String version) {
		return createIndex();
	}

	/**
	 * Creates the default index. 
	 * @return
	 * @throws ElasticSearchException
	 */
	private boolean createIndex() throws ElasticSearchException {
		boolean ret = false;
		try {
			IndicesAdminClient iac = ESClient.getClient().admin().indices();

			//default settings, if null
			@SuppressWarnings("unchecked")
			Map<String, Object> map = new ObjectMapper().readValue(getDefaultIndexSettings(3), LinkedHashMap.class);
			map.put("number_of_shards", 3);

			// create actual index
			CreateIndexRequestBuilder cirb = iac.prepareCreate(APILocator.getPluginAPI().loadProperty("mlh_dotcms_indexSearch", "es.indexName")).setSettings(map);

			cirb.execute().actionGet();
			ret = true;
		} catch (DotDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Returns the json (String) for
	 * the defualt ES index settings
	 * @param shards
	 * @return
	 * @throws IOException
	 */
	private String getDefaultIndexSettings(int shards) throws IOException{
		return jsonBuilder().startObject()
					.startObject("index").field("number_of_shards",shards+"")
						.startObject("analysis")
							.startObject("analyzer")
								.startObject("default").field("type", "Whitespace")
								.endObject()
							.endObject()
						.endObject()
					.endObject()
				.endObject().string();
	}

}
