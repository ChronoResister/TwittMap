package Streaming.Streaming;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;

/**
 * Hello world!
 *
 */
public class Streaming 
{
    //use your endpoint here. we hide our endpoint for privacy
    //JavaWeb with Tomcat uses 80 port here
    String endPoint = "####" + ":80"

    String consumerKey = "###"),
        consumerSecret = ("###"),
        accessToken = ("###"),
        tokenSecret = ("###");


	public static void main(String[] args) throws Exception{
		final JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
		        .Builder(endPoint)
		        .multiThreaded(true)
		        .build());
		
		StatusListener listener = new StatusListener(){
			
			
			JestClient client = factory.getObject();
   			 
    	        public void onStatus(Status status) {
    	        
    	        	
					try {
						if(status.getGeoLocation()!=null){
						List<Double> geo=new ArrayList<Double>();
						geo.add(status.getGeoLocation().getLatitude());
						geo.add(status.getGeoLocation().getLongitude());
						String source = XContentFactory.jsonBuilder()
							    .startObject()
							        .field("user", status.getUser().getName())
							        .field("geo", geo)
							        .field("text", status.getText())
							        .field("time",status.getCreatedAt().getTime())
							    .endObject().string();
						Index index = new Index.Builder(source).index("twittmap").type("tweets").build();
						client.execute(index);
						
						
						
	    	            }
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    	    		
    	        
    	         
    	        }
    	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
    	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
    	        public void onException(Exception ex) {
    	            ex.printStackTrace();
    	        }
    			public void onScrubGeo(long arg0, long arg1) {
    				// TODO Auto-generated method stub
    				
    			}
    			public void onStallWarning(StallWarning arg0) {
    				// TODO Auto-generated method stub
    				
    			}
    	    };
    	   
    	    ConfigurationBuilder cb = new ConfigurationBuilder();
    	    cb.setDebugEnabled(true)
    	      .setOAuthConsumerKey(consumerKey)
    	      .setOAuthConsumerSecret(consumerSecret)
    	      .setOAuthAccessToken(accessToken)
    	      .setOAuthAccessTokenSecret(tokenSecret);
    	    TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
    	    twitterStream.addListener(listener);
  
    	    String[] keywordsArray={"Google","New York","Trump","Hillary","Brooklyn","NBA","football","cloud"};
    	    twitterStream.filter(new FilterQuery(keywordsArray));
    	}
    }

