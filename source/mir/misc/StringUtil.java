/*
 * Copyright (C) 2001, 2002  The Mir-coders group
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
 * the code of this program with the com.oreilly.servlet library, any library
 * licensed under the Apache Software License, The Sun (tm) Java Advanced
 * Imaging library (JAI), The Sun JIMI library (or with modified versions of
 * the above that use the same license as the above), and distribute linked
 * combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than the above
 * mentioned libraries.  If you modify this file, you may extend this exception
 * to your version of the file, but you are not obligated to do so.  If you do
 * not wish to do so, delete this exception statement from your version.
 */

package  mir.misc;

import  java.io.*;
import  java.lang.*;
import  java.util.*;
import  java.text.NumberFormat;
import  gnu.regexp.*;

/**
 * Statische Hilfsmethoden zur Stringbehandlung
 *
 * @version $Revision: 1.25 $ $Date: 2002/09/14 03:32:12 $
 * @author $Author: zapata $
 *
 * $Log: StringUtil.java,v $
 * Revision 1.25  2002/09/14 03:32:12  zapata
 * fixed a small email address filtering bug
 *
 * Revision 1.24  2002/09/01 22:05:50  mh
 * Mir goes GPL
 *
 * Revision 1.23.2.1  2002/09/01 21:31:40  mh
 * Mir goes GPL
 *
 * Revision 1.23  2002/06/28 20:39:37  mh
 * added numberformat helper. make webdbDate2readableDate use webdb_create instead. make the order and appearance of it more consistent. cvs macros. and finally code tidying
 *
 *
 */
public final class StringUtil {

        private static RE   re_newline2br, re_brbr2p, re_mail, re_url, re_tags;

        private StringUtil() { }  // this avoids contruction

        static {
                try {
                        //precompile regex
                        re_newline2br = new RE("(\r?\n){1}");
                        re_brbr2p     = new RE("(<br>\r?\n<br>){1,}");
                        re_mail       = new RE("([a-zA-Z0-9_.-]+)@([a-zA-Z0-9_-]+)\\.([a-zA-Z0-9_.-]+)");
                        re_url        = new RE("((https://)|(http://)|(ftp://)){1}([a-zA-Z0-9_-]+).([a-zA-Z0-9_.:-]+)/?([^ \t\r\n<>\\)\\]]+[^ \t\r\n.,<>\\)\\]])");
                        re_tags       = new RE("<[^>]*>",RE.REG_ICASE);
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
                webdbDate.append(pad2(theDate.get(Calendar.HOUR)));
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
         * wandelt Calendar in dd.mm.yyyy um
         *
         * @param theDate
         * @return String mit  <code>yyyy.mm.dd</code>
         */
        public static final String webdbDate2readableDate (String webdbDate) {
                String date = "";
                date += webdbDate.substring(0, 4);
                date += "-" + webdbDate.substring(5, 7);
                date += "-"+webdbDate.substring(8, 10);
                return  date;
        }


        /**
         * converts string from format: yyyy-mm-dd__hh:mm:ss.d
         * to dd.mm.yyyy hh:mm
         */
        public static String dateToReadableDate(String date) {
                StringBuffer returnDate = new StringBuffer();
                if (date!=null) {

                        returnDate.append(date.substring(8,10)).append('.');
                        returnDate.append(date.substring(5,7)).append('.');
                        returnDate.append(date.substring(0,4)).append(' ');
                        returnDate.append(date.substring(11,16));
                }
                return returnDate.toString();
        }

  /**
         * converts string from format: yyyy-mm-dd__hh:mm:ss.dddddd+TZ
         * to yyyy-mm-ddThh:mm:ss+TZ:00 (w3 format for Dublin Core)
         */
        public static String webdbdateToDCDate(String date) {
                StringBuffer returnDate = new StringBuffer();
                if (date!=null) {
      returnDate.append(date.substring(0,10));
      returnDate.append("T");
      returnDate.append(date.substring(11,19));
      //String tzInfo=date.substring(26,29);
      //if (tzInfo.equals("+00")){
      //UTC gets a special code in w3 dates
      //    returnDate.append("Z");
      //}
      //else{
      //need to see what a newfoundland postgres
      //timestamp looks like before making this robust
      //    returnDate.append(tzInfo);
      //    returnDate.append(":00");
      //}

                }
                return returnDate.toString();
        }


        /**
         * converts string from format: yyyy-mm-dd__hh:mm:ss.d
         * to yyyy
         */
        public static String dateToYear (String date) {
                StringBuffer returnDate = new StringBuffer();
                if (date!=null) {

                        returnDate.append(date.substring(0,4));
                }
                return returnDate.toString();
        }

        /**
         * converts string from format: yyyy-mm-dd__hh:mm:ss.d
         * to [m]m
         */
        public static String dateToMonth (String date) {
                StringBuffer returnDate = new StringBuffer();
                if (date!=null) {
                        if (!date.substring(5,6).equalsIgnoreCase("0")) returnDate.append(date.substring(5,7));
                        else returnDate.append(date.substring(6,7));
                }
                return returnDate.toString();
        }

        /**
         * converts string from format: yyyy-mm-dd__hh:mm:ss.d
         * to [d]d
         */
        public static String dateToDayOfMonth (String date) {
                StringBuffer returnDate = new StringBuffer();
                if (date!=null) {
                        if (!date.substring(8,9).equalsIgnoreCase("0")) returnDate.append(date.substring(8,10));
                        else returnDate.append(date.substring(9,10));
                }
                return returnDate.toString();
        }

        /**
         * converts string from format: yyyy-mm-dd__hh:mm:ss.d
         * to hh:mm
         */
        public static String dateToTime (String date) {
                StringBuffer returnDate = new StringBuffer();
                if (date!=null) {
                        returnDate.append(date.substring(11,16));
                }
                return returnDate.toString();
        }

    /**
     * Splits the provided CSV text into a list. stolen wholesale from
     * from Jakarta Turbine StrinUtils.java -mh
     *
     * @param text      The CSV list of values to split apart.
     * @param separator The separator character.
     * @return          The list of values.
     */
    public static String[] split(String text, String separator)
    {
        StringTokenizer st = new StringTokenizer(text, separator);
        String[] values = new String[st.countTokens()];
        int pos = 0;
        while (st.hasMoreTokens())
        {
            values[pos++] = st.nextToken();
        }
        return values;
    }

    /**
     * Joins the elements of the provided array into a single string
     * containing a list of CSV elements. Stolen wholesale from Jakarta
     * Turbine StringUtils.java. -mh
     *
     * @param list      The list of values to join together.
     * @param separator The separator character.
     * @return          The CSV text.
     */
    public static String join(String[] list, String separator)
    {
        StringBuffer csv = new StringBuffer();
        for (int i = 0; i < list.length; i++)
        {
            if (i > 0)
            {
                csv.append(separator);
            }
            csv.append(list[i]);
        }
        return csv.toString();
    }


        /**
         * schließt einen String in Anführungsszeichen ein, falls er Leerzeichen o.ä. enthält
         *
         * @return gequoteter String
         */
         public static String quoteIfNecessary(String s) {
                for (int i = 0; i < s.length(); i++)
                        if (!(Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '.'))
                                return quote(s, '"');
                return s;
        }

         /**
         * schließt <code>s</code> in <code>'</code> ein und setzt Backslashes vor
         * "gefährliche" Zeichen innerhalb des Strings
         * Quotes special SQL-characters in <code>s</code>
         *
         * @return geqoteter String
         */
        public static String quote(String s)
        {
                //String s2 = quote(s, '\'');
                //Quickhack	ÊÊ Ê Ê Ê Ê Ê Ê
                //Because of '?-Bug in Postgresql-JDBC-Driver
                StringBuffer temp = new StringBuffer();
                for(int i=0;i<s.length();i++){
                        if(s.charAt(i)=='\''){
                                temp.append("&#39;");
                        } else {
                                temp.append(s.charAt(i));
                        }
                }
                String s2 = temp.toString();
                //end Quickhack

                s2 = quote(s2, '\"');
                return s2;
        }

        /**
         * schließt <code>s</code> in <code>'</code> ein und setzt Backslashes vor
         * "gefährliche" Zeichen innerhalb des Strings
         *
         * @param s String, der gequoted werden soll
         * @param quoteChar zu quotendes Zeichen
         * @return gequoteter String
         */
        public static String quote(String s, char quoteChar)
        {
                StringBuffer buf = new StringBuffer(s.length());
                int pos = 0;
                while (pos < s.length()) {
                        int i = s.indexOf(quoteChar, pos);
                        if (i < 0) i = s.length();
                        buf.append(s.substring(pos, i));
                        pos = i;
                        if (pos < s.length()) {
                                buf.append('\\');
                                buf.append(quoteChar);
                                pos++;
                        }
                }
                return buf.toString();
        }

        /**
         * replaces dangerous characters in <code>s</code>
         *
         */

        public static String unquote(String s)
        {
                char quoteChar='\'';
                StringBuffer buf = new StringBuffer(s.length());
                int pos = 0;
                String searchString = "\\"+quoteChar;
                while (pos < s.length()) {
                        int i = s.indexOf(searchString, pos);
                        if (i < 0) i = s.length();
                        buf.append(s.substring(pos, i));
                        pos = i+1;
                }
                return buf.toString();
        }

        /**
         * Wandelet String in byte[] um.
         * @param s
         * @return byte[] des String
         */

        public static byte[] stringToBytes(String s) {
                String crlf = System.getProperty("line.separator");
                if (!crlf.equals("\n"))
                        s = replace(s, "\n", crlf);
                // byte[] buf = new byte[s.length()];
                byte[] buf = s.getBytes();
                return buf;
        }

                /**
         * Ersetzt in String <code>s</code> das <code>pattern</code> durch <code>substitute</code>
         * @param s
         * @param pattern
         * @param substitute
         * @return String mit den Ersetzungen
         */
        public static String replace(String s, String pattern, String substitute) {
                int i = 0, pLen = pattern.length(), sLen = substitute.length();
                StringBuffer buf = new StringBuffer(s.length());
                while (true) {
                        int j = s.indexOf(pattern, i);
                        if (j < 0) {
                                buf.append(s.substring(i));
                                break;
                        } else {
                                buf.append(s.substring(i, j));
                                buf.append(substitute);
                                i = j+pLen;
                        }
                }
                return buf.toString();
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
         * Fügt einen Separator an den Pfad an
         * @param path
         * @return Pfad mit Separator am Ende
         */
        public static final String addSeparator (String path) {
                return  path.length() == 0 || path.endsWith(File.separator) ? path : path
                                + File.separatorChar;
        }

        /**
         * Fügt ein <code>/</code> ans ende des Strings and
         * @param path
         * @return Pfad mit <code>/</code> am Ende
         */
        public static final String addSlash (String path) {
                return  path.length() == 0 || path.endsWith("/") ? path : path + '/';
        }

        /**
         * Löscht <code>/</code> am Ende des Strings, falls vorhanden
         * @param path
         * @return String ohne <code>/</code> am Ende
         */
        public static final String removeSlash (String path) {
                return  path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length()
                                - 1) : path;
        }

        /**
         * Checks to see if the path is absolute by looking for a leading file
         * separater
         * @param path
         * @return
         */
        public static boolean isAbsolutePath (String path) {
                return  path.startsWith(File.separator);
        }

        /**
         * Löscht Slash am Anfang des Strings
         * @param path
         * @return
         */
        public static String removeFirstSlash (String path) {
                return  path.startsWith("/") ? path.substring(1) : path;
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
         * Konvertiert Unix-Linefeeds in Win-Linefeeds
         * @param s
         * @return Konvertierter String
         */
        public static String unixLineFeedsToWin(String s) {
                int i = -1;
                while (true) {
                        i = s.indexOf('\n', i+1);
                        if (i < 0) break;
                        if ((i == 0 || s.charAt(i-1) != '\r') &&
                                (i == s.length()-1 || s.charAt(i+1) != '\r')) {
                                s = s.substring(0, i)+'\r'+s.substring(i);
                                i++;
                        }
                }
                return s;
        }


        /**
         * verwandelt einen String in eine gültige Url, konvertiert Sonderzeichen
         * und Spaces werden zu Underscores
         *
         * @return gültige Url
         */
        public static String convert2url(String s) {
                s = toLowerCase(s);
                StringBuffer buf = new StringBuffer();
                for(int i = 0; i < s.length(); i++ ) {
                                switch( s.charAt( i ) ) {
                                case 'ö':
                        buf.append( "oe" ); break;
                                case 'ä':
                        buf.append( "ae" ); break;
                                case 'ü':
                        buf.append( "ue" ); break;
                                case 'ã':
                        buf.append( "a" ); break;
                                case '´':
                                case '.':
                        buf.append( "_" ); break;
                                case ' ':
                        if( buf.charAt( buf.length() - 1 ) != '_' ) {
                                        buf.append( "_" );
                        }
                        break;
                                default:
                        buf.append( s.charAt( i ) );
                                }
                }
                return buf.toString();
        }


        public static String decodeHTMLinTags(String s){
                StringBuffer buffer = new StringBuffer();
                boolean start = false;
                boolean stop = false;
                int startIndex = 0;
                int stopIndex = 0;
                int temp = 0;

                for(int i=0;i<s.length();i++){
                        if(s.charAt(i)=='<'){
                                start = true;
                                startIndex = i;
                        } else if(s.charAt(i)=='>'){
                                stop = true;
                                stopIndex = i;

                                if(start && stop){
                                        buffer.append(s.substring(temp,startIndex));
                                        buffer.append(replaceQuot(s.substring(startIndex,stopIndex+1)));
                                        i= temp= stopIndex+1;
                                        start= stop= false;
                                }
                        }
                }
                if(stopIndex>0){
                        buffer.append(s.substring(stopIndex+1));
                        return buffer.toString();
                } else {
                        return s;
                }
        }

        public static String replaceQuot(String s) {
                StringBuffer buffer = new StringBuffer();
                for(int j = 0; j < s.length();j++){
                        if(s.charAt(j)=='&'){
                                if(s.indexOf( "&quot;",j) == j) {
                                        buffer.append( "\"" );
                                        j += 5;
                                }//if
                        } else {
                                buffer.append(s.charAt(j));
                        }//else
                }//for
                return buffer.toString();
        }

        /** wandelt Quotes in Sonderzeichen um
         */
        /**
        public static String decodeHtml(String s) {
                StringBuffer buf = new StringBuffer();
                for(int i=0;i < s.length(); i++ ) {
                        if( s.indexOf( "&ouml;", i ) == i ) {
                                buf.append( "ö" ); i += 5;
                                continue;
                        }
                        if( s.indexOf( "&auml;", i ) == i ) {
                                buf.append( "ä" ); i += 5;
                                continue;
                        }
                        if( s.indexOf( "&uuml;", i ) == i ) {
                                buf.append( "ü" ); i += 5;
                                continue;
                        }
                        if( s.indexOf( "&Ouml;", i ) == i ) {
                                buf.append( "Ö" ); i += 5;
                                continue;
                        }
                        if( s.indexOf( "&Auml;", i ) == i ) {
                                buf.append( "Ä" ); i += 5;
                                continue;
                        }
                        if( s.indexOf( "&Uuml;", i ) == i ) {
                                buf.append( "Ü" ); i += 5;
                                continue;
                        }
                        if( s.indexOf( "&szlig;", i ) == i ) {
                                buf.append( "ß" ); i += 6;
                                continue;
                        }
                        if( s.indexOf( "&quot;", i ) == i ) {
                                buf.append( "\"" ); i += 5;
                                continue;
                        }
                        buf.append( s.charAt(i) );
                }
                return buf.toString();
        }
         */

        /**
         * schnellere Variante der String.toLowerCase()-Routine
         *
         * @return String in Kleinbuchsten
         */
        public static String toLowerCase(String s) {
                int l = s.length();
                char[] a = new char[l];
                for (int i = 0; i < l; i++)
                        a[i] = Character.toLowerCase(s.charAt(i));
                return new String(a);
        }

                /**
         * Findet <code>element</code> im String-Array <code>array</code>
         * @param array
         * @param element
         * @return Fundstelle als int oder -1
         */
        public static int indexOf(String[] array, String element) {
                if (array != null)
                        for (int i = 0; i < array.length; i++)
                                if (array[i].equals(element))
                                        return i;
                return -1;
        }

        /**
         * Testet auf Vorkommen von <code>element</code> in <code>array</code>
         * @param array String-Array
         * @param element
         * @return true wenn <code>element</code> vorkommt, sonst false
         */
        public static boolean contains(String[] array, String element) {
                return indexOf(array, element) >= 0;
        }

                /**
         * Ermittelt CRC-Prüfsumme von String <code>s</code>
         * @param s
         * @return CRC-Prüfsumme
         */
        public static int getCRC(String s) {
                int h = 0;
                char val[] = s.toCharArray();
                int len = val.length;

                for (int i = 0 ; i < len; i++) {
                        h &= 0x7fffffff;
                        h = (((h >> 30) | (h << 1)) ^ (val[i]+i));
                }

                return (h << 8) | (len & 0xff);
        }

                /**
         * Liefert Default-Wert def zurück, wenn String <code>s</code>
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
         * Liefert Defaultwert def zurück, wenn s nicht zu einem float geparsed werden kann.
         * @param s
         * @param def
         * @return geparster float oder def
         */
        public static float parseFloat(String s, float def) {
                if (s == null) return def;
                try {
                        return new Float(s).floatValue();
                } catch (NumberFormatException e) {
                        return def;
                }
        }

                /**
         * Findet Ende eines Satzes in String <code>text</code>
         * @param text
         * @param startIndex
         * @return index des Satzendes, oder -1
         */
        public static int findEndOfSentence(String text, int startIndex) {
                 while (true) {
                         int i = text.indexOf('.', startIndex);
                         if (i < 0) return -1;
                         if (i > 0 && !Character.isDigit(text.charAt(i-1)) &&
                                        (i+1 >= text.length()
                                        || text.charAt(i+1) == ' '
                                        || text.charAt(i+1) == '\n'
                                        || text.charAt(i+1) == '\t'))
                                        return i+1;
                         startIndex = i+1;
                 }
        }

                /**
         * Findet Wortende in String <code>text</code> ab <code>startIndex</code>
         * @param text
         * @param startIndex
         * @return Index des Wortendes, oder -1
         */
        public static int findEndOfWord(String text, int startIndex) {
                int i = text.indexOf(' ', startIndex),
                        j = text.indexOf('\n', startIndex);
                if (i < 0) i = text.length();
                if (j < 0) j = text.length();
                return Math.min(i, j);
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
         *  deleteForbiddenTags
         *  this method deletes all <script>, <body> and <head>-tags
         */
        public static final String deleteForbiddenTags(String haystack) {
                try {
                        RE regex = new RE("<[ \t\r\n](.*?)script(.*?)/script(.*?)>",RE.REG_ICASE);
                        haystack = regex.substituteAll(haystack,"");
                        regex = new RE("<head>(.*?)</head>");
                        haystack = regex.substituteAll(haystack,"");
                        regex = new RE("<[ \t\r\n/]*body(.*?)>");
                        haystack = regex.substituteAll(haystack,"");
                        return haystack;
                } catch(REException ex){
                        return null;
                }
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

}

