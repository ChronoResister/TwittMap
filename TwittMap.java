package TwittMap.TwittMap;

/**
 * Hello world!
 *
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.google.gson.*;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.core.search.sort.Sort;



public class TwittMap  extends HttpServlet
{    public String res="{";
     public String[] keywords={"Google","New York","Trump","Hillary","Brooklyn","NBA","football","cloud"};

     //use your endpoint here. we hide our endpoint for privacy
     //JavaWeb with Tomcat uses 80 port here
     String endPoint = "####" + ":80"
     public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{ 
	
    	resp.setContentType("application/json");
    	resp.setCharacterEncoding("UTF-8");
    	PrintWriter out=resp.getWriter();
    	JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
		        .Builder(endPoint)
		        .multiThreaded(true)
		        .build());
		JestClient client = factory.getObject();
		for(String Keyword:keywords){
			output(resp,Keyword,client);
		}
		
		
		StringBuilder res1 = new StringBuilder(res);
		res1.setCharAt(res.length()-1, '}');
		out.write(res1.toString());
		//out.println("</script><script async defer src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyDX9tC6vOBjmZJFjSTvoI-kTwZX2kEyrVo&callback=initMap\"></script></body></html>");
		out.close();

		    
    	}
     public void output( HttpServletResponse resp,String Keyword,JestClient client) throws IOException{
    	 SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
 		searchSourceBuilder.query(QueryBuilders.matchQuery("text", Keyword));

 		Search search = new Search.Builder(searchSourceBuilder.toString())
 		                                // multiple index or types can be added.
 		                                .addIndex("twittmap")
 		                                .addType("tweets")
 		                                .addSort(new Sort("time",Sort.Sorting.DESC))
 		                                .build();

 		SearchResult result = client.execute(search);
 		if(Keyword=="New York") res=res+"\"NewYork\":[";
 		else res=res+"\""+Keyword+"\":[";
List<Hit<Map, Void>> hits = result.getHits(Map.class);
	    for(int i=0;i<hits.size()&&i<10;i++ ){
	    	Hit hit=hits.get(i);
			Map<String, Object> json = new HashMap<String, Object>();
			Map source= (Map)hit.source;
			List geo=(List)source.get("geo");
			json.put("longitude",geo.get(0));
			json.put("latitude",geo.get(1));
			json.put("text",source.get("text"));
			String json1= new Gson().toJson(json);
			if(res.charAt(res.length()-1)=='[') res=res+json1;
			else res=res+","+json1;
		}
		    res=res+"],";
     }
}
