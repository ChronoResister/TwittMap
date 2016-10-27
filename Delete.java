package Delete.Delete;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Delete;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;

/**
 * Hello world!
 *
 */
public class Delete 
{	
	//use your endpoint here. we hide our endpoint for privacy
    //JavaWeb with Tomcat uses 80 port here
    String endPoint = "####" + ":80"

    public static void main( String[] args ) throws Exception
    {
    	while (true){
    	JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
		        .Builder(endPoint)
		        .multiThreaded(true)
		        .build());
		JestClient client = factory.getObject();
	        long today=new Date().getTime();
	        long DAY_IN_MS = 1000 * 60 * 60 * 24;
	        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
	 		searchSourceBuilder.query(QueryBuilders.rangeQuery("time").lt(today-7*DAY_IN_MS));

	 		Search search = new Search.Builder(searchSourceBuilder.toString())
	 		                                // multiple index or types can be added.
	 		                                .addIndex("twittmap")
	 		                                .addType("tweets")
	 		                                .build();

	 		SearchResult result = client.execute(search);
	 		List<Hit<Map, Void>> hits = result.getHits(Map.class);
	 		for (Hit hit:hits){
	 			Map source= (Map)hit.source;
	 			
	 			client.execute(new Delete.Builder(source.get(JestResult.ES_METADATA_ID).toString())
	 	                .index("twittmap")
	 	                .type("tweets")
	 	                .build());
	 		}
    	    
	        System.out.println("finish");
    	}
    	}
}
