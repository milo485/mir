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
package mir.misc;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 *  LineFilterWriter eliminates superfluous \t \r \n and spaces
 *  and thus compresses the output of html
 *
 **/
public final class LineFilterWriter extends PrintWriter{

  protected Writer out;

  public LineFilterWriter(Writer out) {
    super(out);
    this.out=out;
  }

  public final void write(String str){
    int i,j,len;
    boolean state=true;
    char c;
    len=str.length();
    if (len==1) {try{out.write(str);}catch(IOException e){}return;}
    StringBuffer sbuf = new StringBuffer();

    for(i=0;i<len;i++) {
      c=str.charAt(i);
      if(state) {
        j = str.indexOf('\n',i);
        if (j==-1) j=len-1;
				sbuf.append(str.substring(i,j+1));
        i=j;
        state=false;
      }
      else
         if (!Character.isWhitespace(c)) {
						sbuf.append(c);
            state=true;
         }
    }
    try{out.write(sbuf.toString());}catch(IOException e){;}
  }

  public final void write(char[] cbuf, int off,int len){
    int i,j=off;
    boolean state=true;
    char c;

    for(i=off;i<len;i++) {
      c=cbuf[i];
      if(state) {
        if (c=='\n') state=false;
        cbuf[j++]=c;
      }
      else
         if (!Character.isWhitespace(c)) {
						cbuf[j++]=c;
            state=true;
         }
    }
    try{out.write(cbuf,off,j);}catch(IOException e){;}
  }


}
