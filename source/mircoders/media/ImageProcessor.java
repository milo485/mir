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

package mircoders.media;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;

import mir.log.LoggerWrapper;

import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

/**
 *
 * <p>Title: Image processor</p>
 * <p>Description: Temporary image processor class. (Made for the immediate needs of CMI brasil.
 *                 Will become obsolete when mh's media handler rewrite is finished. </p>
 * @author Zapata
 * @version 1.0
 */

public class ImageProcessor {
  static final LoggerWrapper logger = new LoggerWrapper("media");

  private PlanarImage image;
  private PlanarImage scaledImage;

  private byte[] iconData;
  private byte[] imageData;
  private int iconWidth;
  private int iconHeight;
  private String imageType;

//  public ImageScaler(

  public ImageProcessor(SeekableStream anImageStream, String anImageType) throws IOException {
    image = JAI.create("stream", anImageStream);
    scaledImage = image;
    imageType = anImageType;
  }

  public ImageProcessor(File aFile, String anImageType) throws IOException {
    this(new FileSeekableStream(aFile), anImageType);
  }

  public ImageProcessor(byte[] anImageData, String anImageType) throws IOException {
    this(new ByteArraySeekableStream(anImageData), anImageType);
  }

  public void descaleImage(int aMaxSize) throws java.io.IOException {
    descaleImage(aMaxSize, 0);
  }

  public void descaleImage(int aMaxSize, float aMinDescale) throws java.io.IOException {
    descaleImage(aMaxSize, aMaxSize, aMinDescale, 0);
  }

  public void descaleImage(int aMaxSize, int aMinResize) throws java.io.IOException {
    descaleImage(aMaxSize, aMaxSize, 0, aMinResize);
  }

  public void descaleImage(int aMaxSize, float aMinDescale, int aMinResize) throws java.io.IOException {
    descaleImage(aMaxSize, aMaxSize, aMinDescale, aMinResize);
  }

  /**
   *
   * Resizes an image to fit inside <code>aMaxWidth</code> and <code>aMaxHeight</code>, provided
   *    this requires at least <code>aMinResize</code> pixels will be removed from either the width or
   *    the height
   *
   * @param aMaxWidth
   * @param aMaxHeight
   * @param aMinDescale
   * @param aMinResize
   * @throws java.io.IOException
   */
  public void descaleImage(int aMaxWidth, int aMaxHeight, float aMinDescale, int aMinResize) throws java.io.IOException {
    float scale;
    scaledImage = image;

    if ((aMaxWidth>0 && image.getWidth()>aMaxWidth+aMinResize-1) || (aMaxHeight>0 && image.getHeight()>aMaxHeight+aMinResize-1))
    {
      logger.info("Scaling image");

      scale=1;

      if (aMaxWidth>0 && image.getWidth()>aMaxWidth) {
        scale = Math.min(scale, (float) aMaxWidth / (float) image.getWidth());
      }
      if (aMaxHeight>0 && image.getHeight()>aMaxHeight) {
        scale = Math.min(scale, (float) aMaxHeight / (float) image.getHeight());
      }

      if (1-scale>aMinDescale) {
        scaleImage(scale);
      }
    }
  }

  public void scaleImage(float aScalingFactor) throws java.io.IOException {
    ParameterBlockJAI params = new ParameterBlockJAI("scale");
    params.addSource(image);

    params.setParameter("xScale", aScalingFactor);
    params.setParameter("yScale", aScalingFactor);
    params.setParameter("xTrans", 0.0F);
    params.setParameter("yTrans", 0.0F);
    params.setParameter("interpolation", new InterpolationBilinear());
    scaledImage = JAI.create("scale", params);
  }

  public int getWidth() {
    return image.getWidth();
  }

  public int getHeight() {
    return image.getHeight();
  }

  public int getScaledWidth() {
    return scaledImage.getWidth();
  }

  public int getScaledHeight() {
    return scaledImage.getHeight();
  }

  public void writeScaledData(OutputStream aStream) {
    JAI.create("encode", scaledImage, aStream, imageType, null);
  }

  public byte[] getScaledData() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    writeScaledData(outputStream);
    return outputStream.toByteArray();
  }

  public void writeScaledData(File aFile) throws IOException {
    writeScaledData(new FileOutputStream(aFile));
  }
}
