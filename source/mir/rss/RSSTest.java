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

package mir.rss;

import java.util.*;
import mir.util.*;
import gnu.regexp.RE;


public class RSSTest {

  public static void main(String[] args) {

    RSSReader reader = new RSSReader();
//..    RSS091Reader reader2 = new RSS091Reader();
    try {
//      RE test = new RE(".*\\bLB\\b.*", RE.REG_ICASE);
//      System.out.println(" LB II : " + test.isMatch("LB II"));
//      System.out.println(" revised LB: " + test.isMatch("revised LB"));
//      System.out.println(" revised LB II : " + test.isMatch("revised LB II"));
//      System.out.println(" buLB: " + test.isMatch("buLB"));

      RSSData nl = reader.parseUrl("http://biotechdev.mir.dnsalias.net/test.rss");
//      RSSData it = reader.parseUrl("http://g8.mir.dnsalias.net/italynewswire.rss");
//      Object result = StructuredContentParser.parse(" { a = 'b' 'as a' = [ 'asd' asd 'asdas asd as''asd' ] }") ;
//      System.out.println("" + wvl.get("rss:item"));
//      RSSData fr = reader2.parseUrl("http://paris.indymedia.org/backendg8.php3");
//      RSSData be = reader.parseUrl("http://belgium.indymedia.org/features.rdf");

//      RSSAggregator agg = new RSSAggregator(10, "dc:date", true, null, null);

//      agg.appendItems(wvl.getResourcesForRdfClass("rss:item"));
//      agg.appendItems(be.getResourcesForRdfClass("rss:item"));

//      System.out.println(fr);

//      Iterator i = fr.getResourcesForRdfClass("rss:item").iterator();
//      while (i.hasNext())
//        System.out.println(ParameterExpander.evaluateExpression((RDFResource) i.next(), "['dc:date']"));

//      System.out.println(agg.getItems());
    }
    catch (Throwable t) {
      System.out.println("Exception: " + t.toString());
      t.printStackTrace(System.out);
    }
  }
}
