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


import java.io.*;

/**
 * Reports the location of the error in the File.
 * Based and inspired by a source from the Ant distribution
 * (Copyright (c) 1999-2001 The Apache Software Foundation.)
 *
 * @version $Id: ConfigException.java,v 1.2 2002/09/01 22:05:49 mh Exp $
 *
 * @author The Mir-coders group
 */

public class ConfigException extends RuntimeException {

    /** Exception that might have caused this one. */
    private Throwable cause;

    /** Location in the build file where the exception occured */
    private Location location = Location.UNKNOWN_LOCATION;

    /**
     * Constructs a build exception with no descriptive information.
     */
    public ConfigException() {
        super();
    }

    /**
     * Constructs an exception with the given descriptive message.
     * @param msg Description of or information about the exception.
     */
    public ConfigException(String msg) {
        super(msg);
    }

    /**
     * Constructs an exception with the given message and exception as
     * a root cause.
     * @param msg Description of or information about the exception.
     * @param cause Throwable that might have cause this one.
     */
    public ConfigException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    /**
     * Constructs an exception with the given message and exception as
     * a root cause and a location in a file.
     * @param msg Description of or information about the exception.
     * @param cause Exception that might have cause this one.
     * @param location Location in the project file where the error occured.
     */
    public ConfigException(String msg, Throwable cause, Location location) {
        this(msg, cause);
        this.location = location;
    }

    /**
     * Constructs an exception with the given exception as a root cause.
     * @param cause Exception that might have caused this one.
     */
    public ConfigException(Throwable cause) {
        super(cause.toString());
        this.cause = cause;
    }

    /**
     * Constructs an exception with the given descriptive message and a location
     * in a file.
     * @param msg Description of or information about the exception.
     * @param location Location in the project file where the error occured.
     */
    public ConfigException(String msg, Location location) {
        super(msg);
        this.location = location;
    }

    /**
     * Constructs an exception with the given exception as
     * a root cause and a location in a file.
     * @param cause Exception that might have cause this one.
     * @param location Location in the project file where the error occured.
     */
    public ConfigException(Throwable cause, Location location) {
        this(cause);
        this.location = location;
    }

    /**
     * Returns the nested exception.
     */
    public Throwable getException() {
        return cause;
    }

    /**
     * Returns the location of the error and the error message.
     */
    public String toString() {
        return location.toString() + getMessage();
    }

    /**
     * Sets the file location where the error occured.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Returns the file location where the error occured.
     */
    public Location getLocation() {
        return location;
    }

    // Override stack trace methods to show original cause:
    public void printStackTrace() {
        printStackTrace(System.err);
    }
    
}
