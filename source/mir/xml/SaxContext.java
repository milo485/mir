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
package mir.xml;


// XXX this interface is not final, but a prototype.

/** SAX Context - used to match and perform actions
 *    provide access to the current stack and XML elements.
 *
 *    Inspired by Tomcat's SAX context, although our's is
 *    implemented and used slightly differently.
 *
 * @author 
 */
public class SaxContext  {

    private String tagStack[];
    private int pos;

    // -------------------- Konstruktor
    
    public SaxContext() {
        pos=0;
        tagStack = new String[256];
    }
    
    // -------------------- Access to parsing context

    /** Depth of the tag stack.
     */
    public int getTagCount() {
        return pos;
    }

    /** Access a particular tag
     */
    public String getTag( int pos ) {
        return tagStack[pos];
    }

    // ------------------- Adjusting the parsing context
    public void push(String tag) {
        tagStack[pos] = tag;
        pos++;
    }
        
    public void pop() {
        if(pos > 1)
            tagStack[pos]=null;

        pos--;
    }

}

