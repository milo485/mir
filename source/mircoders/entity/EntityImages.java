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

package mircoders.entity;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.sql.*;

/*
 * kind of hack for postgres non-standard LargeObjects that Poolman
 * doesn't know about. see all the casting, LargeObj stuff in getIcon, getImage
 * at some point when postgres has normal BLOB support, this should go.
 */
import org.postgresql.Connection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import mir.entity.*;
import mir.misc.*;
import mir.storage.*;

/**
 * Diese Klasse enthält die Daten eines MetaObjekts
 *
 * @author RK
 * @version 11.11.2000
 */


public class EntityImages extends EntityUploadedMedia
{
	public EntityImages()
	{
		super();
	}

	public EntityImages(StorageObject theStorage) {
		this();
		setStorage(theStorage);
	}

	//
	// methods



	public byte[] getImage() throws StorageObjectException
	{
		theLog.printDebugInfo("--getimage started");
		java.sql.Connection con=null;Statement stmt=null;
		byte[] img_data=null;

		try {
			con = theStorageObject.getPooledCon();
			con.setAutoCommit(false);
			LargeObjectManager lom;
            java.sql.Connection jCon;
            stmt = con.createStatement();
			ResultSet rs = theStorageObject.executeSql(stmt,
                            "select image_data from images where id="+getId());
            jCon = ((com.codestudio.sql.PoolManConnectionHandle)con)
                    .getNativeConnection();
            lom = ((org.postgresql.Connection)jCon).getLargeObjectAPI();
			if(rs!=null) {
              if (rs.next()) {
                LargeObject lob = lom.open(rs.getInt(1));
                img_data = lob.read(lob.size());
                lob.close();
                //img_data = rs.getBytes(1);
              }
            rs.close();
			}
		} catch (Exception e) {
          e.printStackTrace();
          theLog.printError("EntityImages -- getImage failed"+e.toString()); 
          throwStorageObjectException(e, "EntityImages -- getImage failed: ");
        }
        finally {
          try {
            con.setAutoCommit(true);
          } catch (Exception e) {
            e.printStackTrace();
            theLog.printError(
                    "EntityImages -- getImage reseting transaction mode failed"
                    +e.toString()); 
          }
          theStorageObject.freeConnection(con,stmt);
        }

		return img_data;
	}

	public void setImage(byte[] uploadData, String type)
	    throws StorageObjectException {

		if (uploadData!=null) {
			java.sql.Connection con=null;PreparedStatement pstmt=null;
			try {

				theLog.printDebugInfo("settimage :: making internal representation of image");
				WebdbImage webdbImage= new WebdbImage(uploadData, type);
				theLog.printDebugInfo("settimage :: made internal representation of image");
				byte[] imageData = webdbImage.getImage();
				theLog.printDebugInfo("settimage :: getImage");
				byte[] iconData = webdbImage.getIcon();
				theLog.printDebugInfo("settimage :: getIcon");


				if (iconData!=null && imageData!=null) {
					con = theStorageObject.getPooledCon();
					con.setAutoCommit(false);
					theLog.printDebugInfo("settimage :: trying to insert image");

					// setting values
                    LargeObjectManager lom;
                    java.sql.Connection jCon;
                    jCon = ((com.codestudio.sql.PoolManConnectionHandle)con)
                            .getNativeConnection();
                    lom = ((org.postgresql.Connection)jCon).getLargeObjectAPI();
                    int oidImage = lom.create();
                    int oidIcon = lom.create();
                    LargeObject lobImage = lom.open(oidImage);
                    LargeObject lobIcon = lom.open(oidIcon);
                    lobImage.write(imageData);
                    lobIcon.write(iconData);
                    lobImage.close();
                    lobIcon.close();
                    String sql = "update images set img_height='"
                        +webdbImage.getImageHeight() +
						"',img_width='"   + webdbImage.getImageWidth() +
						"',icon_height='" + webdbImage.getIconHeight() +
						"',icon_width='"  + webdbImage.getIconWidth()
                        +  "', image_data="+oidImage+", icon_data="+oidIcon
                        +" where id="+getId();
					theLog.printDebugInfo("settimage :: updating sizes: "+ sql);
					pstmt = con.prepareStatement(sql);
					//pstmt.setBytes(1, imageData);
					//pstmt.setBytes(2, iconData);
					pstmt.executeUpdate();
					sql="update content set is_produced='0' where to_media="+getId();
					pstmt = con.prepareStatement(sql);
					pstmt.executeUpdate();
				}
			}
			catch (Exception e) {throwStorageObjectException(e, "settimage :: setImage gescheitert: ");}
			finally {
				try { if (con!=null) con.setAutoCommit(true); } catch (Exception e) {;}
				theStorageObject.freeConnection(con,pstmt); }
		}
	}

	public void update() throws StorageObjectException {
		super.update();
		try {
			theStorageObject.executeUpdate("update content set is_produced='0' where to_media="+getId());
		} catch (SQLException e) {
			throwStorageObjectException(e, "EntityImages :: update :: failed!! ");
		}
	}

	public void setValues(HashMap theStringValues)
	{
		if (theStringValues != null) {
			if (!theStringValues.containsKey("is_published"))
			 theStringValues.put("is_published","0");
		}
		super.setValues(theStringValues);
	}

	public byte[] getIcon() throws StorageObjectException
	{
		java.sql.Connection con=null;Statement stmt=null;
		byte[] img_data=null;

		try {
			con = theStorageObject.getPooledCon();
			con.setAutoCommit(false);
            LargeObjectManager lom;
            java.sql.Connection jCon;
			stmt = con.createStatement();
			ResultSet rs = theStorageObject.executeSql(stmt,
                            "select icon_data from images where id="+getId());
            jCon = ((com.codestudio.sql.PoolManConnectionHandle)con)
                    .getNativeConnection();
            lom = ((org.postgresql.Connection)jCon).getLargeObjectAPI();
			if(rs!=null) {
				if (rs.next()) {
                  LargeObject lob = lom.open(rs.getInt(1));
                  img_data = lob.read(lob.size());
                  lob.close();
                  //img_data = rs.getBytes(1);
				}
                rs.close();
			}
		} catch (Exception e) {
          e.printStackTrace();
          theLog.printError("EntityImages -- getIcon failed"+e.toString()); 
          throwStorageObjectException(e, "EntityImages -- getIcon failed:");
		} finally {
          try {
            con.setAutoCommit(true);
          } catch (Exception e) {
            e.printStackTrace();
            theLog.printError(
                    "EntityImages -- getIcon reseting transaction mode failed"
                    +e.toString()); 
          }
          theStorageObject.freeConnection(con,stmt);
       }

       return img_data;
	}

}
