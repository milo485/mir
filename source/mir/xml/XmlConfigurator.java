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

package mir.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mir.misc.ConfigException;
import mir.misc.Location;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Configures a based on
 * a XML config file.
 *
 * Based and inspired by ant's XmlConfigurator.java
 * It's a simplified version of the ant parser with
 * the addition of calling set* methods for defined
 * classes as well as the inclusion of a method to
 * add parameters (nested tags) that are required.
 * (the addRequired method) in the config file.
 * that part is from tomcat.
 *
 * much code is stolen from ant ProjectHelper.java.
 *
 * @author -mh <heckmann@hbe.ca>
 * @version 2001.10.21
 */

public class XmlConfigurator {

    private static SAXParserFactory parserFactory = null;

    private SAXParser saxParser;
    //private Project project;
    private File configFile;
    private File configFileParent;
    private Locator locator;

    private SaxContext saxContext;

    XmlMatch requiredXmlMatch[]=new XmlMatch[256]; //maximum amount of rules
    int requiredXmlMatchCount=0;
    boolean matched[] = new boolean[256];
    int matchedCount=0;

    XmlMatch mustComeFirstMatch[]=new XmlMatch[256]; //maximum amount of rules
    int comeFirstMatchCount=0;

    Property comesFirstArr[]=new Property[128];
    int comesFirstCount=0;

    Property propertyArr[]=new Property[128];
    int propertyCount=0;

    private static XmlConfigurator instance = new XmlConfigurator();
    public static XmlConfigurator getInstance() { return instance; }

    /**
     * Configures the Project with the contents of the specified XML file.
     */
    public void configure(File configFile) throws ConfigException {
        setConfigFile(configFile);
        parse();
    }

    /**
     * konstruktor. private so no one calls "new" on us.
     */
    private XmlConfigurator() {}


    /**
     * Constructs a new Ant parser for the specified XML file.
     */
    private void setConfigFile(File configFile) {
        this.configFile = new File(configFile.getAbsolutePath());
        configFileParent = new File(this.configFile.getParent());
        saxContext = new SaxContext();
    }

    /**
     * Parses the config file.
     */
    private void parse() throws ConfigException {
        FileInputStream inputStream = null;
        InputSource inputSource = null;
        
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            saxParser = factory.newSAXParser();
                
            String uri = "file:" + configFile.getAbsolutePath().replace('\\', '/');
            for (int index = uri.indexOf('#'); index != -1; index = uri.indexOf('#')) {
                uri = uri.substring(0, index) + "%23" + uri.substring(index+1);
            }
            
            inputStream = new FileInputStream(configFile);
            inputSource = new InputSource(inputStream);
            inputSource.setSystemId(uri);
            saxParser.parse(inputSource, new RootHandler());
            if(matchedCount < requiredXmlMatchCount) {
                for( int i=0; i<requiredXmlMatchCount; i++) {
                    if( !matched[i] )
                        throw new ConfigException("Error parsing config file, missing required element: "+requiredXmlMatch[i].toString());
                }
            }
            try {
                for(int i=0; i<comesFirstCount;i++) {
                    comesFirstArr[i].set();
                }
                for(int i=0; i<propertyCount;i++) {
                    propertyArr[i].set();
                    System.out.println("about to set: "+i);
                }
            } catch (Exception e) {
                throw new SAXParseException(e.toString(), locator);
            }
}
        catch(ParserConfigurationException exc) {
            throw new ConfigException("Parser has not been configured correctly", exc);
        }
        catch(SAXParseException exc) {
            Location location =
                new Location(configFile.toString(), exc.getLineNumber(), exc.getColumnNumber());

            Throwable t = exc.getException();
            if (t instanceof ConfigException) {
                ConfigException be = (ConfigException) t;
                if (be.getLocation() == Location.UNKNOWN_LOCATION) {
                    be.setLocation(location);
                }
                throw be;
            }
            
            throw new ConfigException(exc.getMessage(), t, location);
        }
        catch(SAXException exc) {
            Throwable t = exc.getException();
            if (t instanceof ConfigException) {
                throw (ConfigException) t;
            }
            throw new ConfigException(exc.getMessage(), t);
        }
        catch(FileNotFoundException exc) {
            throw new ConfigException(exc);
        }
        catch(IOException exc) {
            throw new ConfigException("Error reading config file", exc);
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ioe) {
                    // ignore this
                }
            }
        }
    }

    /**
     * The common superclass for all sax event handlers in Ant. Basically
     * throws an exception in each method, so subclasses should override
     * what they can handle.
     *
     * Each type of xml element (task, target, etc) in ant will
     * have its own subclass of AbstractHandler.
     *
     * In the constructor, this class    takes over the handling of sax
     * events from the parent handler, and returns
     * control back to the parent in the endElement method.
     */
    private class AbstractHandler extends DefaultHandler {
        protected ContentHandler parentHandler;

        public AbstractHandler(ContentHandler parentHandler) {
            this.parentHandler = parentHandler;

            // Start handling SAX events
            try {
                saxParser.getXMLReader().setContentHandler(this);
            } catch (SAXException e) {
                throw new ConfigException("Error getting XMLReader",e);
            }
                
        }

        public void startElement(String uri, String tag, String qName, Attributes attrs) throws SAXParseException {
            throw new SAXParseException("Unexpected element \"" + tag + "\"", locator);
        }

        public void characters(char[] buf, int start, int end) throws SAXParseException {
            String s = new String(buf, start, end).trim();

            if (s.length() > 0) {
                throw new SAXParseException("Unexpected text \"" + s + "\"", locator);
            }
        }

        /**
         * Called when this element and all elements nested into it have been
         * handeled.
         */
        protected void finished() {}

        public void endElement(String uri, String tag, String qName) throws SAXException {
            finished();
            // Let parent resume handling SAX events
            saxParser.getXMLReader().setContentHandler(parentHandler);
        }
    }

    /**
     * Handler for the root element. It's only child must be the "mir" element.
     */
    private class RootHandler extends DefaultHandler {

        /**
         * resolve file: URIs as relative to the build file.
         */
        public InputSource resolveEntity(String publicId,
                                         String systemId) {
        
        
            if (systemId.startsWith("file:")) {
                String path = systemId.substring(5);
                int index = path.indexOf("file:");
                
                // we only have to handle these for backward compatibility
                // since they are in the FAQ.
                while (index != -1) {
                    path = path.substring(0, index) + path.substring(index + 5);
                    index = path.indexOf("file:");
                }
                
                String entitySystemId = path;
                index = path.indexOf("%23");
                // convert these to #
                while (index != -1) {
                    path = path.substring(0, index) + "#" + path.substring(index + 3);
                    index = path.indexOf("%23");
                }

                File file = new File(path);
                if (!file.isAbsolute()) {
                    file = new File(configFileParent, path);
                }
                
                try {
                    InputSource inputSource = new InputSource(new FileInputStream(file));
                    inputSource.setSystemId("file:" + entitySystemId);
                    return inputSource;
                } catch (FileNotFoundException fne) {
                    System.out.println(file.getAbsolutePath()+" could not be found");
                }
            }
            // use default if not file or file not found
            return null;
        }

        public void startElement(String uri, String tag, String qName, Attributes attrs) throws SAXParseException {
            if (tag.equals("mir")) {
                new MirHandler(this).init(tag, attrs);
            } else {
                throw new SAXParseException("Config file is not of expected XML type", locator);
            }
        }

        public void setDocumentLocator(Locator locator) {
            XmlConfigurator.this.locator = locator;
        }
    }

    /**
     * Handler for the top level "project" element.
     */
    private class MirHandler extends AbstractHandler {
        public MirHandler(ContentHandler parentHandler) {
            super(parentHandler);
        }

        public void init(String tag, Attributes attrs) throws SAXParseException {
            String name = null;

            for (int i = 0; i < attrs.getLength(); i++) {
                String key = attrs.getLocalName(i);
                String value = attrs.getValue(i);

                if (key.equals("name")) {
                    name = value;
                } else {
                    throw new SAXParseException("Unexpected attribute \"" + attrs.getLocalName(i) + "\"", locator);
                }
            }

            if (name == null) {
                throw new SAXParseException("The default attribute of \"name\" is required", 
                                            locator);
            }
            
            saxContext.push(tag);
            matchedCount += checkRequiredTag(saxContext);

            //MirConfig.setName(name);

        }

        public void startElement(String uri, String name, String qName, Attributes attrs) throws SAXParseException {
            if (name.equals("class")) {
                handleClassdef(name, attrs);
            } else {
                throw new SAXParseException("Unexpected element \"" + name + "\"", locator);
            }
        }

        public void finished() {
            System.out.println("COUNT "+saxContext.getTagCount()+" TAG "+saxContext.getTag(saxContext.getTagCount()-1));
            saxContext.pop();
        }

        private void handleClassdef(String name, Attributes attrs) throws SAXParseException {
            (new ClassHandler(this)).init(name, attrs);
        }

    }

    /**
     * Handler for "class" elements.
     */
    private class ClassHandler extends AbstractHandler {

        Class classN;

        public ClassHandler(ContentHandler parentHandler) {
            super(parentHandler);
        }

        public void init(String tag, Attributes attrs) throws SAXParseException {
            String name = null;

            for (int i = 0; i < attrs.getLength(); i++) {
                String key = attrs.getLocalName(i);
                String value = attrs.getValue(i);

                if (key.equals("name")) {
                    name = value;
                } else {
                    throw new SAXParseException("Unexpected attribute \"" + key + "\"", locator);
                }
            }

            if (name == null) {
                throw new SAXParseException("class element appears without a \"name\" attribute", locator);
            }

            saxContext.push(tag+":"+name);
            matchedCount += checkRequiredTag(saxContext);
            try {
                classN=Class.forName(name, false, this.getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new ConfigException("Error invoking class: \""+name+
                    "\"",e);
            }

        }

        public void startElement(String uri, String name, String qName, Attributes attrs) throws SAXParseException {
            if (name.equals("property")) {
                handleProperties(name, attrs);
            } else {
                throw new SAXParseException("Unexpected element \"" + name + "\"", locator);
            }
        }

        public void finished() {
            System.out.println("COUNT "+saxContext.getTagCount()+" TAG "+saxContext.getTag(saxContext.getTagCount()-1));
            System.out.println("COUNT "+saxContext.getTagCount());
            saxContext.pop();
        }

        private void handleProperties(String name, Attributes attrs) throws SAXParseException {
            (new PropertiesHandler(this, classN )).init(name, attrs);
        }

    }

    /**
     * Handler for all property elements.
     */
    private class PropertiesHandler extends AbstractHandler {
        private Class classN;

        public PropertiesHandler(ContentHandler parentHandler, Class classN) {
            super(parentHandler);

            this.classN = classN;
        }

        public void init(String tag, Attributes attrs) throws SAXParseException {
            String name=null;
            String value=null;

            for (int i = 0; i < attrs.getLength(); i++) {
                String key = attrs.getLocalName(i);
                String v = attrs.getValue(i);

                if (key.equals("name")) {
                    name = v;
                } else if (key.equals("value")) {
                    value = v; 
                } else {
                    throw new SAXParseException("Unexpected attribute \"" + key + "\"", locator);
                }
            }

            if (name == null) {
                throw new SAXParseException("property element appears without a \"name\" attribute", locator);
            }
            if (value == null) {
                throw new SAXParseException("property element appears without a \"value\" attribute", locator);
            }
            saxContext.push(tag+":"+name);
            matchedCount += checkRequiredTag(saxContext);

            //finally add it to the lists
            //to be processed later
            if (checkComesFirstTag(saxContext)) {
                comesFirstArr[comesFirstCount]=new Property(classN, name, value);
                comesFirstCount++;
            } else {
                propertyArr[propertyCount]=new Property(classN, name, value);
                propertyCount++;
            }
        }

        protected void finished() {
            System.out.println("COUNT "+saxContext.getTagCount()+" TAG "+saxContext.getTag(saxContext.getTagCount()-1));
            System.out.println("COUNT "+saxContext.getTagCount());
            saxContext.pop();
        }

    }

    public void addComesFirstTag(String xmlPath) {
        mustComeFirstMatch[comeFirstMatchCount]=new XmlMatch(xmlPath);
        comeFirstMatchCount++;
    }

    private boolean checkComesFirstTag(SaxContext ctx) {
        for( int i=0; i<comeFirstMatchCount; i++ ) {
            if( mustComeFirstMatch[i].match(ctx) ) {
                return true;
            }
        }
        return false;
    }

    public void addRequiredTag(String xmlPath) {
        requiredXmlMatch[requiredXmlMatchCount]=new XmlMatch(xmlPath);
        matched[requiredXmlMatchCount]=false;
        requiredXmlMatchCount++;
    }

    private int checkRequiredTag(SaxContext ctx) {
        int matchCount=0;
        for( int i=0; i<requiredXmlMatchCount; i++ ) {
            if( requiredXmlMatch[i].match(ctx) ) {
                matched[i]=true;
                matchCount++;
            }
        }
        return matchCount;
    }


    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    private class Property {
        Class classN;
        String name;
        String value;

        public Property( Class classN, String name, String value) {
            this.classN=classN;
            this.name=name;
            this.value=value;
        }
            
        /** Find a method with the right name
         * If found, call the method ( if param is int or boolean we'll convert 
         * value to the right type before) - that means you can have setDebug(1).
         */
        public void set() throws Exception {
            
            String setter= "set" +capitalize(name);

            try {
                Method methods[]=classN.getMethods();
                Method setPropertyMethod=null;

                // First, the ideal case - a setFoo( String ) method
                for( int i=0; i< methods.length; i++ ) {
                    Class paramT[]=methods[i].getParameterTypes();
                    if( setter.equals( methods[i].getName() ) &&
                        paramT.length == 1 &&
                        "java.lang.String".equals( paramT[0].getName())) {

                        methods[i].invoke( null, new Object[] { value } );
                        return;
                    }
                } //end for

                // Try a setFoo ( int ), (float) or ( boolean )
                for( int i=0; i< methods.length; i++ ) {
                    boolean ok=true;
                    if( setter.equals( methods[i].getName() ) &&
                        methods[i].getParameterTypes().length == 1) {

                        // match - find the type and invoke it
                        Class paramType=methods[i].getParameterTypes()[0];
                        Object params[]=new Object[1];
                        if ("java.lang.Integer".equals( paramType.getName()) ||
                            "int".equals( paramType.getName())) {
                            try {
                                params[0]=new Integer(value);
                            } catch( NumberFormatException ex ) {ok=false;}
                        } else if ("java.lang.Float".equals( paramType.getName()) ||
                            "float".equals( paramType.getName())) {
                            try {
                                params[0]=new Float(value);
                            } catch( NumberFormatException ex ) {ok=false;}
                        } else if ("java.lang.Boolean".equals( paramType.getName()) ||
                            "boolean".equals( paramType.getName())) {
                            params[0]=new Boolean(value);
                        } else {
                            throw new Exception("Unknown type " + paramType.getName() + "for property \""+name+"\"with value \""+value+"\"");
                        }

                        if( ok ) {
                            System.out.println("XXX: " + methods[i] + " " + classN + " " + params[0] );
                            methods[i].invoke( null, params );
                            return; 
                        } //end if
                    } //end if setter
                } //end for

                //if we got this far it means we were not successful in setting the
                //property
                throw new Exception("Count not find method \""+setter+"\" in Class \""+classN.getName()+"\" in order to set property \""+name+"\"");

            } catch( SecurityException ex1 ) {
                throw new Exception("SecurityException for " + classN.getName() + " " +  name + "="  + value  +")" );
                //if( ctx.getDebug() > 1 ) ex1.printStackTrace();
            } catch (IllegalAccessException iae) {
                throw new Exception("IllegalAccessException for " + classN.getName() + " " +  name + "="  + value  +")" );
                //if( ctx.getDebug() > 1 ) iae.printStackTrace();
            } catch (InvocationTargetException ie) {
                throw new Exception("InvocationTargetException for " + classN.getName() + " " +  name + "="  + value  +")" );
                //if( ctx.getDebug() > 1 ) ie.printStackTrace();
            }
        }
    }

}
