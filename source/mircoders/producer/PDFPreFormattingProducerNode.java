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

package mircoders.producer;

import java.util.*;
import java.io.*;

import mir.util.*;
import mir.producer.*;
import mir.entity.*;
import mir.entity.adapter.*;
import mir.log.*;

import mircoders.entity.*;
import mircoders.storage.*;

//because images are returned as a template model!(maybe not needed after all!)
//import freemarker.template.*;


public class PDFPreFormattingProducerNode implements ProducerNode {
  private String contentKey;
  private int numLinesBetweenImages;
  private float contentAreaWidthCM;
  private float characterWidthCM;
  private float pixelWidthCM;
  private float lineHeightCM;

  public PDFPreFormattingProducerNode(String aContentKey,String someNumLinesBetweenImages,String aContentAreaWidthCM,String aCharacterWidthCM,String aPixelWidthCM,String aLineHeightCM) {
    contentKey = aContentKey;
    numLinesBetweenImages=(new Integer(someNumLinesBetweenImages)).intValue();
    contentAreaWidthCM=(new Float(aContentAreaWidthCM)).floatValue();
    characterWidthCM=(new Float(aCharacterWidthCM)).floatValue();
    pixelWidthCM=(new Float(aPixelWidthCM)).floatValue();
    lineHeightCM=(new Float(aLineHeightCM)).floatValue();

    //    float characterWidthCM = 0.17F;
    //float contentAreaWidthCM = 16;
    //float pixelWidthCM = .03F;
    //float lineHeightCM = .5F;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    Object data;
    Entity entity;

    try {
      data = ParameterExpander.findValueForKey( aValueMap, contentKey );

      if (! (data instanceof EntityAdapter)) {
        throw new ProducerFailure("ContentMarkingProducerNode: value of '"+contentKey+"' is not an EntityAdapter, but an " + data.getClass().getName(), null);
      }

      entity = ((EntityAdapter) data).getEntity();
      if (! (entity instanceof EntityContent)) {
        throw new ProducerFailure("ContentMarkingProducerNode: value of '"+contentKey+"' is not a content EntityAdapter, but a " + entity.getClass().getName() + " adapter", null);
      }

      int currentPosition = 0;

      //int numLinesBetweenImages=3;




      int numCharsInAnImagelessRow = (new Float(numLinesBetweenImages * (contentAreaWidthCM/characterWidthCM))).intValue();

      boolean outOfText = false;

      ArrayList brokenUpContent = new ArrayList();


      EntityList images=DatabaseContentToMedia.getInstance().getImages((EntityContent)entity);
      String theContent = ((EntityContent) entity).getValue("content_data");
      if (images == null){
          HashMap row = new HashMap();
          row.put("text",theContent);
          row.put("hasImage","0");
          brokenUpContent.add(row);
      }
      if (images != null){
          //need to add checks for out of content!
          HashMap row0 = new HashMap();
          if (numCharsInAnImagelessRow>(theContent).length()){
              row0.put("text",theContent);
              outOfText = true;
          }
          else {
              //break on words so we don't split html entities
              int lastSpaceAt = theContent.lastIndexOf(" ",numCharsInAnImagelessRow);
              row0.put("text",theContent.substring(0,lastSpaceAt));
              currentPosition=lastSpaceAt;
          }
          row0.put("hasImage","0");
          brokenUpContent.add(row0);
          aLogger.debug("CP1 is "+ currentPosition);
          while(images.hasNext()){
              HashMap row1 = new HashMap();
              HashMap row2 = new HashMap();
              EntityImages currentImage=(EntityImages) images.next();
              float img_width=(new Float(currentImage.getValue("img_width"))).floatValue();
              float img_height=(new Float(currentImage.getValue("img_height"))).floatValue();

              //oversize images must be shrunk
              if (img_width>400){
                  img_height=(new Float((new Float(img_height*(400.0F/img_width))).intValue())).floatValue();
                  img_width=400.0F;
              }


              //calculate how much text goes in the column(use 8 pixels to pad the column)
              float text_widthCM = contentAreaWidthCM-((img_width+8)*pixelWidthCM);
              float number_of_lines = img_height*pixelWidthCM/lineHeightCM; //don't worry we will make it an int
              //add one line for image description
              int text_amount= (new Float((text_widthCM/characterWidthCM)*(number_of_lines+1))).intValue();

              row1.put("text_widthCM",Float.toString(text_widthCM));

              row1.put("img_title",currentImage.getValue("title"));

              row1.put("img_width",Float.toString(img_width));
              row1.put("img_height",Float.toString(img_height));

              aLogger.debug("img_width " +Float.toString(img_width));
              aLogger.debug("img_height "+Float.toString(img_height));

              row1.put("img_src",currentImage.getValue("publish_path"));
              row1.put("hasImage","1");
              if (! outOfText){
                  try {
                      int lastSpaceAt = theContent.lastIndexOf(" ",currentPosition+text_amount);
                      row1.put("text",theContent.substring(currentPosition,lastSpaceAt));
                      currentPosition=lastSpaceAt;
                  }
                  catch (IndexOutOfBoundsException e){
                      row1.put("text",theContent.substring(currentPosition));
                      outOfText = true;
                          }
              }
              aLogger.debug("CP2 is "+ currentPosition);
              brokenUpContent.add(row1);

              if (! outOfText){
                  try {
                      int lastSpaceAt = theContent.lastIndexOf(" ",currentPosition+numCharsInAnImagelessRow);
                      row2.put("text",theContent.substring(currentPosition,lastSpaceAt));
                      currentPosition=lastSpaceAt;
                  }
                  catch (IndexOutOfBoundsException e){
                      row2.put("text",theContent.substring(currentPosition));
                      outOfText = true;
                          }
              }
              row2.put("hasImage","0");
              brokenUpContent.add(row2);

              aLogger.debug("CP3 is "+ currentPosition);
          }
          HashMap row3 = new HashMap();
          if (! outOfText){
              row3.put("text",theContent.substring(currentPosition));
              row3.put("hasImage","0");
              brokenUpContent.add(row3);
          }

      }





      ParameterExpander.setValueForKey(
                                       aValueMap,
                                       "data.formatted_content",
                                       new CachingRewindableIterator(brokenUpContent.iterator())
                                       );


    }
    catch (Throwable t) {
      aLogger.error("Error while formatting content for PDF: " + t.getMessage());
      t.printStackTrace(new PrintWriter(new LoggerToWriterAdapter(aLogger, LoggerWrapper.DEBUG_MESSAGE)));
    }
  }
}




