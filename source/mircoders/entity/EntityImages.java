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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import mir.config.MirPropertiesConfiguration;
import mir.log.LoggerWrapper;
import mir.misc.FileUtil;
import mir.misc.WebdbImage;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;

import org.postgresql.largeobject.BlobInputStream;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

/**
 * Diese Klasse enth?lt die Daten eines MetaObjekts
 *
 * @author RK, mh, mir-coders
 * @version $Id: EntityImages.java,v 1.19 2003/03/08 17:18:19 idfx Exp $
 */


public class EntityImages extends EntityUploadedMedia
{

  public EntityImages()
  {
    super();

    logger = new LoggerWrapper("Entity.UploadedMedia.Images");
  }

  public EntityImages(StorageObject theStorage) {
    this();
    setStorage(theStorage);
  }

  //
  // methods


  public InputStream getImage() throws StorageObjectFailure {
    logger.debug("EntityImages.getimage started");
    java.sql.Connection con=null;
    Statement stmt=null;
    BlobInputStream in;
    InputStream img_in = null;
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
    }
    catch (Throwable t) {
      logger.error("EntityImages.getImage failed: " + t.toString());
      t.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      try {
        con.setAutoCommit(true);
      }
      catch (Throwable e) {
        logger.error("EntityImages.getImage resetting transaction mode failed: " + e.toString());
        e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      }

      try {
        theStorageObject.freeConnection(con, stmt);
      }
      catch (Throwable e) {
        logger.error("EntityImages.getImage freeing connection failed: " +e.toString());
      }

      throwStorageObjectFailure(t, "EntityImages -- getImage failed: ");
    }
    return img_in;
  }

  public void setImage(InputStream in, String type)
        throws StorageObjectFailure {

    if (in != null) {
      Connection con = null;
      PreparedStatement pstmt = null;
      File f = null;
      try {
        logger.debug("EntityImages.settimage :: making internal representation of image");

        File tempDir = new File(MirPropertiesConfiguration.instance().getString("TempDir"));
        f = File.createTempFile("mir", ".tmp", tempDir);
        FileUtil.write(f, in);
        WebdbImage webdbImage= new WebdbImage(f, type);
        logger.debug("EntityImages.settimage :: made internal representation of image");

        con = theStorageObject.getPooledCon();
        con.setAutoCommit(false);
        logger.debug("EntityImages.settimage :: trying to insert image");

        // setting values
        LargeObjectManager lom;
        java.sql.Connection jCon;
        jCon = ((com.codestudio.sql.PoolManConnectionHandle)con)
             .getNativeConnection();

        lom = ((org.postgresql.Connection) jCon).getLargeObjectAPI();

        int oidImage = lom.create();
        int oidIcon = lom.create();
        LargeObject lobImage = lom.open(oidImage);
        LargeObject lobIcon = lom.open(oidIcon);
        webdbImage.setImage(lobImage.getOutputStream());
        webdbImage.setIcon(lobIcon.getOutputStream());
        lobImage.close();
        lobIcon.close();

        setValueForProperty("img_height", new Integer(webdbImage.getImageHeight()).toString());
        setValueForProperty("img_width", new Integer(webdbImage.getImageWidth()).toString());
        setValueForProperty("icon_height", new Integer(webdbImage.getIconHeight()).toString());
        setValueForProperty("icon_width", new Integer(webdbImage.getIconWidth()).toString());
        setValueForProperty("image_data", new Integer(oidImage).toString());
        setValueForProperty("icon_data", new Integer(oidIcon).toString());
        update();
      } catch (Exception e) {
        throwStorageObjectFailure(e, "settimage :: setImage gescheitert: ");
      } finally {
        try {
          if (con!=null)
            con.setAutoCommit(true);
          // get rid of the temp. file
          f.delete();
        } catch (SQLException e) {
          throwStorageObjectFailure(e,"Resetting transaction-mode failed");
        }
        if (con!=null)
          theStorageObject.freeConnection(con,pstmt);
      }
    }
  }

  /**
   * Takes an OutputStream as an argument and reads in the data
   * from the DB and writes it to the OutputStream.
   *
   * It will also take care of closing the OutputStream.
   */
  public InputStream getIcon() throws StorageObjectFailure {
    Connection con=null;
    Statement stmt=null;
    BlobInputStream in=null;
    ImageInputStream img_in=null;

    try {
      con = theStorageObject.getPooledCon();
      con.setAutoCommit(false);
      LargeObjectManager lom;
      java.sql.Connection jCon;
      stmt = con.createStatement();
      ResultSet rs = theStorageObject.executeSql(stmt, "select icon_data from images where id="+getId());
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
    }
    catch (Throwable t) {
      logger.error("EntityImages.getIcon failed: "+t.toString());
      t.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      try {
        con.setAutoCommit(true);
      }
      catch (SQLException e) {
        logger.error("EntityImages.getIcon resetting transaction mode failed: " + e.toString());
        e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      }
      try {
        theStorageObject.freeConnection(con, stmt);
      }
      catch (Throwable e) {
       logger.error("EntityImages -- freeing connection failed: " + e.getMessage());
      }

      throwStorageObjectFailure(t, "EntityImages -- getIcon failed:");
    }

    return img_in;
  }

  /**
   * a small wrapper class that allows us to store the DB connection resources
   * that the BlobInputStream is using and free them upon closing of the stream
   */
  private class ImageInputStream extends InputStream {

    InputStream _in;
    Connection _con;
    Statement _stmt;

    public ImageInputStream(BlobInputStream in, Connection con,
                            Statement stmt ) {
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
