package mir.producer;

import java.util.Map;

import mir.log.LoggerWrapper;
import mir.rss.RSSData;
import mir.rss.RSSReader;
import mir.rss.RSSToMapConverter;
import mir.util.ParameterExpander;
import mir.util.ExceptionFunctions;

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

      ParameterExpander.setValueForKey(aValueMap, expandedKey, null);
      RSSReader reader = new RSSReader();
      RSSData rssData = reader.parseUrl(expandedUrl);
      ParameterExpander.setValueForKey(aValueMap, expandedKey, RSSToMapConverter.convertRSSData(rssData));
    }
    catch (Throwable t) {
      Throwable s = ExceptionFunctions.traceCauseException(t);
      aLogger.error("Error while processing RSS data: " + s.getClass().getName()+","+ s.getMessage());
    }
  };
}