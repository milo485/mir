package mir.producer;

import java.util.Map;

import mir.log.LoggerWrapper;
import mir.rss.RSSData;
import mir.rss.RSSReader;
import mir.rss.RSSToMapConverter;
import mir.util.ParameterExpander;

public class RSSProducerNode implements ProducerNode {
  private String key;
  private String url;

  public RSSProducerNode(String aKey, String anURL) {
    key = aKey;
    url = anURL;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    try {
      String expandedKey = ParameterExpander.expandExpression( aValueMap, key );
      String expandedUrl = ParameterExpander.expandExpression( aValueMap, url );

      RSSReader reader = new RSSReader();
      RSSData rssData = reader.parseUrl(url);
      ParameterExpander.setValueForKey(aValueMap, expandedKey, RSSToMapConverter.convertRSSData(rssData));
    }
    catch (Throwable t) {
      aLogger.error("Error while processing RSS data: " + t.toString());
    }
  };
}