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

package mir.misc;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import org.apache.struts.util.MessageResources;

import java.util.List;
import java.util.Locale;

/**
 * A FreeMarker <code>TemplateMethodModel</code> that provides access to a
 * Struts <code>MessageResources</code>, for use in Interantionalized templates.
 *
 * @author Kellan <kellan@protest.net>
 */

public class MessageMethodModel implements TemplateMethodModel {

    /**
     * The perferred locale for this instance of MessageMethod.
     */
    private Locale locale;

    /**
     * The MessageResources to query, a single instance shared for
     * the lifetime of servlet.
     */
    private MessageResources messages;


    /**
     * Construct a MessageMethod that uses the JVM's default locale.
     *
     * @param message The MessageResources object to query
     */
    public MessageMethodModel(MessageResources messages) {
        this(null, messages);
    }

    /**
     * Construct a MessageMethod
     *
     * @param locale a Locale object, persumably initialized
     *               from users Accept-Language header field
     *
     * @param message The MessageResources object to query
     */
    public MessageMethodModel(Locale locale, MessageResources messages) {
        this.locale = locale;
        this.messages = messages;
    }


    /**
     * Takes the first argument as a resource key, then looks up
     * a string in the MessagesResources, based on that key, and the Locale
     *
     * TODO: error messages should be i18n :)
     *
     * @param arguments List passed in by FM, first arguement is a string used as the key
     *                  all subsequent arguments are used as described in MessageResources
     *                  (they are filled into the placehoders of the string being returned)
     */
    public TemplateModel exec(List arguments) {
        if (arguments != null) {
            String key = (String) arguments.get(0);
            arguments.remove(0);
            String mesg = messages.getMessage(locale, key, arguments.toArray());

            if (mesg == null) {
                return new SimpleScalar(errUnknownTag+key);
            }
            return new SimpleScalar(mesg);
        }
        else {
            return missingKeyScalar;
        }
    }

    // i'm not real clear on how this is used - kellan :)
    public boolean isEmpty() {
        if (messages == null)
            return true;
        else
            return false;
    }

    private static String errUnknownTag = "MESSAGE NOT FOUND: ";
    private static String missingKey = "MESSAGE CALL WITHOUT KEY";
    private static SimpleScalar missingKeyScalar =
            new SimpleScalar(missingKey);
}
