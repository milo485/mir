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

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Indymedia
 * @author
 * @version 1.0
 */

import java.io.*;
import java.util.Vector;
import javax.media.jai.*;
import com.sun.media.jai.codec.*;
import java.awt.image.renderable.ParameterBlock;

public class WebdbImage
{

	// default values for scaling
	private int               maxIconSize=120;
	private int               maxImageSize=640;

	private byte[]            iconData;
	private byte[]            imageData;
	private int               iconWidth;
	private int               iconHeight;

	// internal representation of the image
	private PlanarImage       planarImage;

  // type of the image
  private String _type;


	// constructor
	public WebdbImage(byte[] image, String type)
		throws IOException
	{
		planarImage = JAI.create("stream",new ByteArraySeekableStream(image));
    _type = type;
		scaleImage();
	}

	public WebdbImage(byte[] image, String type, int iconMax)
		throws IOException
	{
		maxIconSize=iconMax;
		planarImage = JAI.create("stream",new ByteArraySeekableStream(image));
    _type = type;
		scaleImage();
	}

	public WebdbImage(byte[] image, String type, int iconMax, int imageMax)
		throws IOException
	{
		maxIconSize=iconMax;
		maxImageSize=imageMax;
		planarImage = JAI.create("stream",new ByteArraySeekableStream(image));
    _type = type;
		scaleImage();
	}

	// acc3ssor-methods
	public int getIconWidth() throws IOException {
		if (iconData==null) scaleIcon();
		return iconWidth;
	}

	public int getIconHeight() throws IOException {
		if (iconData==null) scaleIcon();
		return iconHeight;
	}

	public int getImageWidth() {
		return (int)planarImage.getWidth();
	}

	public int getImageHeight() {
		return (int)planarImage.getHeight();
	}

	public byte[] getImage() {
		if (imageData==null) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      JAI.create("encode", planarImage, outStream, _type, null);
			imageData = outStream.toByteArray();
		}
		return imageData;
	}

	public byte[] getIcon()
		throws IOException
	{
		if (iconData == null) scaleIcon();
		return iconData;
	}

	private void scaleImage()
		throws java.io.IOException
	{
		if (maxImageSize>0 && ( getImageHeight()> maxImageSize|| getImageWidth() >maxImageSize))
		{
      System.out.println("SCALE_IMAGE");
			float scale;
      ParameterBlockJAI params = new ParameterBlockJAI("scale");
      params.addSource(planarImage);
			if (getImageHeight() > getImageWidth())
        scale = (float)maxImageSize / (float)getImageHeight();
      else
        scale = (float)maxImageSize / (float)getImageWidth();

      params.setParameter("xScale", scale);
      params.setParameter("yScale", scale);
			params.setParameter("xTrans",0.0F);
			params.setParameter("yTrans",0.0F);
			params.setParameter("interpolation",new InterpolationBilinear());
			planarImage = JAI.create("scale", params);
		}
	}

	private void scaleIcon()
		throws java.io.IOException
	{
    System.out.println("SCALE_ICON");
		if (iconData==null) {
			float scale;
      ParameterBlockJAI params = new ParameterBlockJAI("scale");
      params.addSource(planarImage);
			if (getImageHeight() > getImageWidth())
        scale = (float)maxIconSize / (float)getImageHeight();
      else
        scale = (float)maxIconSize / (float)getImageWidth();

      params.setParameter("xScale", scale);
      params.setParameter("yScale", scale);
			params.setParameter("xTrans",0.0F);
			params.setParameter("yTrans",0.0F);
			params.setParameter("interpolation",new InterpolationBilinear());
      PlanarImage temp = JAI.create("scale", params);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      JAI.create("encode", temp, outStream, _type, null);
			iconData = outStream.toByteArray();
			iconWidth=temp.getWidth();
			iconHeight=temp.getHeight();
		}
	}

}
