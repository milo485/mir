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

package mir.util;

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;
import multex.Exc;
import multex.Failure;

/**
 * a class to do some basic, regexp based parsing of character data
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SimpleParser {
  private String data;
  private int position;

  /**
   *
   * @param aData
   */

  public SimpleParser(String aData) {
    data=aData;
    position=0;
  }

  /**
   *
   * @param aRegularExpression
   * @return
   * @throws SimpleParserExc
   */

  public boolean parses(RE aRegularExpression) throws SimpleParserExc {
    REMatch match = aRegularExpression.getMatch(data, position);

    return (match!=null && match.getStartIndex()==position) ;
  }

  /**
   *
   * @param aRegularExpression
   * @param aMessage
   * @return
   * @throws SimpleParserExc
   */

  public String parse(RE aRegularExpression, String aMessage) throws SimpleParserExc {
    REMatch match = aRegularExpression.getMatch(data, position);

    if (match==null || match.getStartIndex()!=position)
      throw new SimpleParserExc(aMessage+" at position "+position+" in '"+data+"'");

    position=match.getEndIndex();

    return match.toString();
  }

  /**
   *
   * @param aRegularExpression
   * @return
   * @throws SimpleParserExc
   */

  public String parse(RE aRegularExpression) throws SimpleParserExc {
    return parse( aRegularExpression, "No match found for '"+aRegularExpression.toString()+"'");
  }

  /**
   *
   * @param aRegularExpression
   * @throws SimpleParserExc
   */

  public void skip(RE aRegularExpression) throws SimpleParserExc {
    REMatch match = aRegularExpression.getMatch(data, position);

    if (match!=null && match.getStartIndex()==position)
      position=match.getEndIndex();
  }

  /**
   *
   * @param anExpression
   * @return
   * @throws SimpleParserExc
   */

  public boolean parses(String anExpression) throws SimpleParserExc {
    try {
      return parses(new RE(anExpression));
    }
    catch (SimpleParserExc e) {
      throw e;
    }
    catch (REException e) {
      throw new SimpleParserFailure( "Error compiling regular expression '" + anExpression + "': " + e.getMessage(), e);
    }
    catch (Throwable t) {
      throw new SimpleParserFailure( t );
    }
  }

  /**
   *
   * @param anExpression
   * @return
   * @throws SimpleParserExc
   * @throws SimpleParserFailure
   */

  public String parse(String anExpression) throws SimpleParserExc, SimpleParserFailure {
    try {
      return parse(new RE(anExpression));
    }
    catch (SimpleParserExc e) {
      throw e;
    }
    catch (REException e) {
      throw new SimpleParserFailure( "Error compiling regular expression '" + anExpression + "': " + e.getMessage(), e);
    }
    catch (Throwable t) {
      throw new SimpleParserFailure( t );
    }
  }

  /**
   *
   * @param anExpression
   * @param aMessage
   * @return
   * @throws SimpleParserExc
   * @throws SimpleParserFailure
   */

  public String parse(String anExpression, String aMessage) throws SimpleParserExc, SimpleParserFailure {
    try {
      return parse(new RE(anExpression), aMessage);
    }
    catch (SimpleParserExc e) {
      throw e;
    }
    catch (REException e) {
      throw new SimpleParserFailure( "Error compiling regular expression '" + anExpression + "': " + e.getMessage(), e);
    }
    catch (Throwable t) {
      throw new SimpleParserFailure( t );
    }
  }

  /**
   *
   * @param anExpression
   * @throws SimpleParserExc
   * @throws SimpleParserFailure
   */

  public void skip(String anExpression) throws SimpleParserExc, SimpleParserFailure {
    try {
      skip(new RE(anExpression));
    }
    catch (SimpleParserExc e) {
      throw e;
    }
    catch (REException e) {
      throw new SimpleParserFailure( "Error compiling regular expression '" + anExpression + "': " + e.getMessage(), e);
    }
    catch (Throwable t) {
      throw new SimpleParserFailure( t );
    }
  }

  /**
   *
   * @return true if the parser is at the end of the data
   */

  public boolean isAtEnd() {
    return position>=data.length();
  }

  /**
   *
   * @return
   */
  public String remainingData() {
    return data.substring(position);
  }

  /**
   *
   * <p>Title: </p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2003</p>
   * <p>Company: </p>
   * @author not attributable
   * @version 1.0
   */

  public static class SimpleParserFailure extends Failure {
    public SimpleParserFailure(Throwable aThrowable) {
      super(aThrowable.getMessage(), aThrowable);
    }

    public SimpleParserFailure(String aMessage, Throwable aThrowable) {
      super(aMessage, aThrowable);
    }
  }

  public static class SimpleParserExc extends Exc {
    public SimpleParserExc(String aMessage) {
      super(aMessage);
    }
  }
}