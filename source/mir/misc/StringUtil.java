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
package  mir.misc;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import gnu.regexp.RE;
import gnu.regexp.REException;

/**
 * Statische Hilfsmethoden zur Stringbehandlung
 *
 * @version $Id: StringUtil.java,v 1.34 2003/09/03 18:29:02 zapata Exp $
 * @author mir-coders group
 *
 */
public final class StringUtil {

  private static RE   re_newline2br, re_brbr2p, re_mail, re_url, re_tags,
                      re_tables, re_forbiddenTags;

  private StringUtil() { }  // this avoids contruction

  static {
    try {
      //precompile regex
      re_newline2br = new RE("(\r?\n){1}");
      re_brbr2p     = new RE("(<br>\r?\n<br>){1,}");
      re_mail       = new RE("\\b([a-zA-Z0-9_.-]+)@([a-zA-Z0-9_-]+)\\.([a-zA-Z0-9_.-]+)\\b");
      re_url        = new RE("((https://)|(http://)|(ftp://)){1}([a-zA-Z0-9_-]+).([a-zA-Z0-9_.:-]+)/?([^ \t\r\n<>\\)\\]]+[^ \t\r\n.,<>\\)\\]])");
      re_tags       = new RE("<[^>]*>",RE.REG_ICASE);
      re_tables = new RE("<[ \t\r\n/]*(table|td|tr)[ \t\r\n]*>",RE.REG_ICASE);
      re_forbiddenTags = new RE("<[ \t\r\n/]*(html|meta|body|head|script)[ \t\r\n]*>",RE.REG_ICASE);
    }
    catch (REException e){
      System.err.println("FATAL: StringUtil: could not precompile REGEX: "+e.toString());
    }
  }

  /**
   * Formats a number with the specified minimum and maximum number of digits.
   **/
  public static synchronized String zeroPaddingNumber(long value, int minDigits,
      int maxDigits)
  {
    NumberFormat numberFormat = NumberFormat.getInstance();
    numberFormat.setMinimumIntegerDigits(minDigits);
    numberFormat.setMaximumIntegerDigits(maxDigits);
    return numberFormat.format(value);
  }

  /**
   * Wandelt Datum in einen 8-ziffrigen String um (yyyymmdd)
   * @param theDate
   * @return 8-ziffriger String (yyyymmdd)
   */

  public static final String date2webdbDate (GregorianCalendar theDate) {
    StringBuffer webdbDate = new StringBuffer();
    webdbDate.append(String.valueOf(theDate.get(Calendar.YEAR)));
    webdbDate.append(pad2(theDate.get(Calendar.MONTH) + 1));
    webdbDate.append(pad2(theDate.get(Calendar.DATE)));
    return  webdbDate.toString();
  }

  /**
   * Wandelt Calendar in einen 12-ziffrigen String um (yyyymmddhhmm)
   * @param theDate
   * @return 12-ziffriger String (yyyymmdd)
   */

  public static final String date2webdbDateTime (GregorianCalendar theDate) {
    StringBuffer webdbDate = new StringBuffer();
    webdbDate.append(String.valueOf(theDate.get(Calendar.YEAR)));
    webdbDate.append(pad2(theDate.get(Calendar.MONTH) + 1));
    webdbDate.append(pad2(theDate.get(Calendar.DATE)));
    webdbDate.append(pad2(theDate.get(Calendar.HOUR)));
    webdbDate.append(pad2(theDate.get(Calendar.MINUTE)));
    return  webdbDate.toString();
  }

  /**
   * Return a http://www.w3.org/TR/NOTE-datetime formatted date (yyyy-mm-ddThh:mm:ssTZ)
   * @param theDate
   * @return w3approved datetime
   */

  public static final String date2w3DateTime (GregorianCalendar theDate) {
    StringBuffer webdbDate = new StringBuffer();
    webdbDate.append(String.valueOf(theDate.get(Calendar.YEAR)));
    webdbDate.append("-");
    webdbDate.append(pad2(theDate.get(Calendar.MONTH) + 1));
    webdbDate.append("-");
    webdbDate.append(pad2(theDate.get(Calendar.DATE)));
    webdbDate.append("T");
    webdbDate.append(pad2(theDate.get(Calendar.HOUR_OF_DAY)));
    webdbDate.append(":");
    webdbDate.append(pad2(theDate.get(Calendar.MINUTE)));
    webdbDate.append(":");
    webdbDate.append(pad2(theDate.get(Calendar.SECOND)));
    //assumes you are an hour-multiple away from UTC....
    int offset=(theDate.get(Calendar.ZONE_OFFSET)/(60*60*1000));
    if (offset < 0){
      webdbDate.append("-");
    }
    else{
      webdbDate.append("+");
    }
    webdbDate.append(pad2(Math.abs(offset)));
    webdbDate.append(":00");
    return  webdbDate.toString();
  }

  /**
   * wandelt Calendar in dd.mm.yyyy / hh.mm um
   * @param theDate
   * @return String mit (dd.mm.yyyy / hh.mm um)
   */
  public static String date2readableDateTime (GregorianCalendar theDate) {
    String readable = "";
    int hour;
    readable += pad2(theDate.get(Calendar.DATE));
    readable += "." + pad2(theDate.get(Calendar.MONTH) + 1);
    readable += "." + String.valueOf(theDate.get(Calendar.YEAR));
    hour = theDate.get(Calendar.HOUR);
    if (theDate.get(Calendar.AM_PM) == Calendar.PM)
      hour += 12;
    readable += " / " + pad2(hour);
    readable += ":" + pad2(theDate.get(Calendar.MINUTE));
    return  readable;
  }

  /**
  *  deleteForbiddenTags
  *  this method deletes all <script>, <body> and <head>-tags
  */
  public static final String deleteForbiddenTags(String haystack) {
    return re_forbiddenTags.substituteAll(haystack,"");
  }

  /**
   *  deleteHTMLTableTags
   *  this method deletes all <table>, <tr> and <td>-tags
   */
  public static final String deleteHTMLTableTags(String haystack) {
    return re_tables.substituteAll(haystack,"");
  }

  /**
   * wandelt eine Datum in einen 8-buchstabigen String, der durch <code>/</code>
   * getrennt ist.
   *
   * @param webdbDate
   * @return String mit <code>/yyyy/mm/dd</code>
   */
  public static final String webdbDate2path (String webdbDate) {
    StringBuffer path = new StringBuffer();
    path.append("/").append(webdbDate.substring(0, 4));
    path.append("/").append(webdbDate.substring(4, 6));
    path.append("/");
    //who did this?
    //path.append("/").append(webdbDate.substring(6, 8));
    return  path.toString();
  }

  /**
   * Ersetzt in String <code>s</code> das Regexp <code>pattern</code> durch <code>substitute</code>
   * @param s
   * @param pattern
   * @param substitute
   * @return String mit den Ersetzungen
   */
  public static String regexpReplace(String haystack, String pattern, String substitute) {
    try {
      RE regex = new RE(pattern);
      return regex.substituteAll(haystack,substitute);
    } catch(REException ex){
      return null;
    }
  }

  /**
   * L?scht <code>/</code> am Ende des Strings, falls vorhanden
   * @param path
   * @return String ohne <code>/</code> am Ende
   */
  public static final String removeSlash (String path) {
    return  path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length()
        - 1) : path;
  }

  /**
   * formatiert eine Zahl (0-99) zweistellig (z.B. 5 -> 05)
   * @return zwistellige Zahl
   */
  public static String pad2 (int number) {
    return  number < 10 ? "0" + number : String.valueOf(number);
  }

  /**
   * formatiert eine Zahl (0-999) dreistellig (z.B. 7 -> 007)
   *
   * @return 3-stellige Zahl
   */
  public static String pad3 (int number) {
    return  number < 10 ? "00" + number : number < 100 ? "0" + number : String.valueOf(number);
  }

  /**
   * Liefert Default-Wert def zur?ck, wenn String <code>s</code>
   * kein Integer ist.
   *
   * @param s
   * @param def
   * @return geparster int aus s oder def
   */
  public static int parseInt(String s, int def) {
    if (s == null) return def;
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return def;
    }
  }


  /**
   *  convertNewline2P ist eine regex-routine zum umwandeln von 2 oder mehr newlines (\n)
   *  in den html-tag <p>
   *  nur sinnvoll, wenn text nicht im html-format eingegeben
   */
  public static String convertNewline2P(String haystack) {
    return re_brbr2p.substituteAll(haystack,"\n</p><p>");
  }

  /**
   *  convertNewline2Break ist eine regex-routine zum umwandeln von 1 newline (\n)
   *  in den html-tag <br>
   *  nur sinnvoll, wenn text nicht im html-format eingegeben
   */
  public static String convertNewline2Break(String haystack) {
    return re_newline2br.substituteAll(haystack,"$0<br />");
  }

  /**
   *  createMailLinks wandelt text im email-adressenformat
   *  in einen klickbaren link um
   *  nur sinnvoll, wenn text nicht im html-format eingegeben
   */
  public static String createMailLinks(String haystack) {
    return re_mail.substituteAll(haystack,"<a href=\"mailto:$0\">$0</a>");
  }


  /**
   *  createMailLinks wandelt text im email-adressenformat
   *  in einen klickbaren link um
   *  nur sinnvoll, wenn text nicht im html-format eingegeben
   */
  public static String createMailLinks(String haystack, String imageRoot, String mailImage) {
    return re_mail.substituteAll(haystack,"<img src=\""+imageRoot+"/"+mailImage+"\" border=\"0\"/>&#160;<a href=\"mailto:$0\">$0</a>");
  }


  /**
   *  createURLLinks wandelt text im url-format
   *  in einen klickbaren link um
   *  nur sinnvoll, wenn text nicht im html-format eingegeben
   */
  public static String createURLLinks(String haystack) {
    return re_url.substituteAll(haystack,"<a href=\"$0\">$0</a>");
  }

  /**
   * this routine takes text in url format and makes
   * a clickaeble "<href>" link removing any "illegal" html tags
   * @param haystack, the url
   * @param title, the href link text
   * @param imagRoot, the place to find icons
   * @param extImage, the url of the icon to show next to the link
   * @return a String containing the url
   */
  public static String createURLLinks(String haystack, String title, String imageRoot,String extImage) {
    if (title == null) {
      return re_url.substituteAll(haystack,"<img src=\""+imageRoot+"/"+extImage+"\" border=\"0\"/>&#160;<a href=\"$0\">$0</a>");
    } else {
      title = removeHTMLTags(title);
      return re_url.substituteAll(haystack,"<img src=\""+imageRoot+"/"+extImage+"\" border=\"0\"/>&#160;<a href=\"$0\">"+title+"</a>");
    }
  }

  /**
   * this routine takes text in url format and makes
   * a clickaeble "<href>" link removing any "illegal" html tags
   * @param haystack, the url
   * @param imageRoot, the place to find icons
   * @param extImage, the url of the icon to show next to the link
   * @param intImage, unused
   * @return a String containing the url
   */
  public static String createURLLinks(String haystack, String title, String imageRoot,String extImage,String intImage) {
    return createURLLinks(haystack, title, imageRoot, extImage);
  }

  /**
   * this method deletes all html tags
   */
  public static final String removeHTMLTags(String haystack){
    return re_tags.substituteAll(haystack,"");
  }

  /**
   * this method deletes all but the approved tags html tags
   * it also deletes approved tags which contain malicious-looking attributes and doesn't work at all
   */
  public static String approveHTMLTags(String haystack){
    try {
      String approvedTags="a|img|h1|h2|h3|h4|h5|h6|br|b|i|strong|p";
      String badAttributes="onAbort|onBlur|onChange|onClick|onDblClick|onDragDrop|onError|onFocus|onKeyDown|onKeyPress|onKeyUp|onLoad|onMouseDown|onMouseMove|onMouseOut|onMouseOver|onMouseUp|onMove|onReset|onResize|onSelect|onSubmit|onUnload";
      String approvedProtocols="rtsp|http|ftp|https|freenet|mailto";

      // kill all the bad tags that have attributes
      String s = "<\\s*/?\\s*(?!(("+approvedTags+")\\s))\\w+\\s[^>]*>";
      RE regex = new RE(s,RE.REG_ICASE);
      haystack = regex.substituteAll(haystack,"");

      // kill all the bad tags that are attributeless
      regex = new RE("<\\s*/?\\s*(?!(("+approvedTags+")\\s*>))\\w+\\s*>",RE.REG_ICASE);
      haystack = regex.substituteAll(haystack,"");

      // kill all the tags which have a javascript attribute like onLoad
      regex = new RE("<[^>]*("+badAttributes+")[^>]*>",RE.REG_ICASE);
      haystack = regex.substituteAll(haystack,"");

      // kill all the tags which include a url to an unacceptable protocol
      regex = new RE("<\\s*a\\s+[^>]*href=(?!(\'|\")?("+approvedProtocols+"))[^>]*>",RE.REG_ICASE);
      haystack = regex.substituteAll(haystack,"");

      return haystack;
    } catch(REException ex){
      ex.printStackTrace();
      return null;
    }
  }


  /**
   *  createHTML ruft alle regex-methoden zum unwandeln eines nicht
   *  htmlcodierten string auf und returnt einen htmlcodierten String
   */
  public static String createHTML(String content){
    content=convertNewline2Break(content);
    content=convertNewline2P(content);
    content=createMailLinks(content);
    content=createURLLinks(content);
    return content;
  }


  /**
   *  createHTML ruft alle regex-methoden zum unwandeln eines nicht
   *  htmlcodierten string auf und returnt einen htmlcodierten String
   */
  public static String createHTML(String content,String producerDocRoot,String mailImage,String extImage,String intImage){
    content=convertNewline2Break(content);
    content=convertNewline2P(content);
    content=createMailLinks(content,producerDocRoot,mailImage);
    content=createURLLinks(content,null,producerDocRoot,extImage,intImage);
    return content;
  }

  /**
   * Converts mir's horrible internal date format (yyyy-MM-dd HH:mm:ss+zz) into a java Date
   *
   * @param anInternalDate
   * @return
   */
  public static Date convertMirInternalDateToDate(String anInternalDate) {
    Calendar calendar = new GregorianCalendar();

    int year;
    int month;
    int day;
    int hours;
    int minutes;
    int seconds;
    int timezoneOffset;

    year = Integer.parseInt(anInternalDate.substring(0,4));
    month = Integer.parseInt(anInternalDate.substring(5,7));
    day = Integer.parseInt(anInternalDate.substring(8,10));
    hours = Integer.parseInt(anInternalDate.substring(11,13));
    minutes = Integer.parseInt(anInternalDate.substring(14,16));
    seconds = Integer.parseInt(anInternalDate.substring(17,19));

    timezoneOffset = Integer.parseInt(anInternalDate.substring(20,22));
    if (anInternalDate.charAt(19) == '-')
      timezoneOffset = -timezoneOffset;

    calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    calendar.set(year, month-1, day, hours, minutes, seconds);
    calendar.add(Calendar.HOUR, -timezoneOffset);

    return calendar.getTime();
  }

}

