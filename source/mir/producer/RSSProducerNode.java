/*
 * Copyright (C) 2001, 2002 The Mir-coders group
 *
 * This file is part of Mir.
 *
 * Mir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Mir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, The Mir-coders gives permission to link
 * the code of this program with  any library licensed under the Apache Software License,
 * The Sun (tm) Java Advanced Imaging library (JAI), The Sun JIMI library
 * (or with modified versions of the above that use the same license as the above),
 * and distribute linked combinations including the two.  You must obey the
 * GNU General Public License in all respects for all of the code used other than
 * the above mentioned libraries.  If you modify this file, you may extend this
 * exception to your version of the file, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package mir.producer;

import java.util.Map;

import mir.log.LoggerWrapper;
import mir.rss.RSSData;
import mir.rss.*;
import mir.util.ExceptionFunctions;
import mir.util.ParameterExpander;

public class RSSProducerNode implements ProducerNode {
  private String key;
  private String url;
  private String version;

  public RSSProducerNode(String aKey, String anURL, String aVersion) {
    key = aKey;
    url = anURL;
    version = aVersion;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    try {
      RSSData rssData = null;
      String expandedKey = ParameterExpander.expandExpression( aValueMap, key );
      String expandedUrl = ParameterExpander.expandExpression( aValueMap, url );
      String expandedVersion = ParameterExpander.expandExpression( aValueMap, version );

      ParameterExpander.setValueForKey(aValueMap, expandedKey, null);

      if (version.equals("1.0")) {
        RSSReader reader = new RSSReader();
        rssData = reader.parseUrl(expandedUrl);
      }
      else if (version.equals("0.91")) {
        RSS091Reader reader = new RSS091Reader();
        rssData = reader.parseUrl(expandedUrl);
      }
      ParameterExpander.setValueForKey(aValueMap, expandedKey, rssData);
    }
    catch (Throwable t) {
      Throwable s = ExceptionFunctions.traceCauseException(t);
      aLogger.error("Error while processing RSS data: " + s.getClass().getName()+","+ s.getMessage());
    }
  };
}