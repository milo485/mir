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
import org.postgresql.largeobject.BlobInputStream;

import mir.entity.*;
import mir.misc.*;
import mir.storage.*;

/**
 * Diese Klasse enthält die Daten eines MetaObjekts
 *
 * @author RK, mh
 * @version $Id: EntityImages.java,v 1.9 2002/11/15 22:13:21 mh Exp $
 */


public class EntityImages extends EntityUploadedMedia
{

  Random r = new Random();

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


	public InputStream getImage() throws StorageObjectException
	{
		theLog.printDebugInfo("--getimage started");
		java.sql.Connection con=null;Statement stmt=null;
    BlobInputStream in; InputStream img_in = null;

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
          in = (BlobInputStream)lob.getInputStream();
          img_in = new ImageInputStream(in, con, stmt);
        }
        rs.close();
			}
		} catch (Exception e) {
      e.printStackTrace();
      theLog.printError("EntityImages -- getImage failed"+e.toString()); 
      try {
        con.setAutoCommit(true);
      } catch (Exception e2) {
        e.printStackTrace();
        theLog.printError(
          "EntityImages -- getImage reseting transaction mode failed"
            +e2.toString()); 
      }
      theStorageObject.freeConnection(con,stmt);
      throwStorageObjectException(e, "EntityImages -- getImage failed: ");
    }

    return img_in;
	}

	public void setImage(InputStream in, String type)
	    throws StorageObjectException {

		if (in!=null) {
			java.sql.Connection con=null;PreparedStatement pstmt=null;
      File f = null;
			try {

				theLog.printDebugInfo("settimage :: making internal representation of image");

        String fName = MirConfig.getProp("TempDir")+File.separator+r.nextInt();
        f = new File(fName);
        FileUtil.write(f, in);
				WebdbImage webdbImage= new WebdbImage(f, type);
				theLog.printDebugInfo("settimage :: made internal representation of image");

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
        webdbImage.setImage(lobImage.getOutputStream());
        webdbImage.setIcon(lobIcon.getOutputStream());
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
			catch (Exception e) {throwStorageObjectException(e, "settimage :: setImage gescheitert: ");}
			finally {
				try {
          if (con!=null)
            con.setAutoCommit(true);
          // get rid of the temp. file
          f.delete();
        } catch (Exception e) {;}
				theStorageObject.freeConnection(con,pstmt);
      }
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

  /**
   * Takes an OutputStream as an argument and reads in the data
   * from the DB and writes it to the OutputStream.
   *
   * It will also take care of closing the OutputStream.
   */
	public InputStream getIcon() throws StorageObjectException
	{
		java.sql.Connection con=null;Statement stmt=null;
    BlobInputStream in=null;ImageInputStream img_in=null;

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
          in = (BlobInputStream)lob.getInputStream();
          img_in = new ImageInputStream( in, con ,stmt);
          //img_data = rs.getBytes(1);
				}
        rs.close();
			}
		} catch (Exception e) {
      e.printStackTrace();
      theLog.printError("EntityImages -- getIcon failed"+e.toString()); 
      try {
        con.setAutoCommit(true);
      } catch (Exception e2) {
        e.printStackTrace();
        theLog.printError(
          "EntityImages -- getIcon reseting transaction mode failed"
            +e2.toString()); 
      }
      theStorageObject.freeConnection(con,stmt);
      throwStorageObjectException(e, "EntityImages -- getIcon failed:");
		}

    return img_in;
	}

  /**
   * a small wrapper class that allows us to store the DB connection resources
   * that the BlobInputStream is using and free them upon closing of the stream
   */
  private class ImageInputStream extends InputStream {

    InputStream _in;
    java.sql.Connection _con;
    Statement _stmt;

    public ImageInputStream(BlobInputStream in, java.sql.Connection con,
                            Statement stmt )
    {
      _in = in;
      _con = con;
      _stmt = stmt;
    }

    public void close () throws IOException {
      _in.close();

      try {
        _con.setAutoCommit(true);
        theStorageObject.freeConnection(_con,_stmt);
      } catch (Exception e) {
        throw new IOException("close(): "+e.toString());
      }
    }

    public int read() throws IOException {
      return _in.read();
    }

  }

}
