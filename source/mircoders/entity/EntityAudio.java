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
 * doesn't know about. see all the casting, LargeObj stuff in getIcon, getAudio
 * at some point when postgres has normal BLOB support, this should go.
 */
import org.postgresql.Connection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import mir.entity.*;
import mir.misc.*;
import mir.storage.*;

/**
 * This class handles storage of audio data and meta data
 *
 * @author mh
 * @version 11.11.2000
 */


public class EntityAudio extends EntityUploadedMedia
{
	public EntityAudio()
	{
		super();
	}

	public EntityAudio(StorageObject theStorage) {
		this();
		setStorage(theStorage);
	}

	//
	// methods



	public byte[] getAudio() throws StorageObjectException
	{
		theLog.printDebugInfo("--getaudio started");
		java.sql.Connection con=null;Statement stmt=null;
		byte[] data=null;

		try {
			con = theStorageObject.getPooledCon();
			con.setAutoCommit(false);
			LargeObjectManager lom;
            java.sql.Connection jCon;
            stmt = con.createStatement();
			ResultSet rs = theStorageObject.executeSql(stmt,
                            "select audio_data from audio where id="+getId());
            jCon = ((com.codestudio.sql.PoolManConnectionHandle)con)
                    .getNativeConnection();
            lom = ((org.postgresql.Connection)jCon).getLargeObjectAPI();
			if(rs!=null) {
              if (rs.next()) {
                LargeObject lob = lom.open(rs.getInt(1));
                data = lob.read(lob.size());
                lob.close();
                //data = rs.getBytes(1);
              }
            rs.close();
			}
		} catch (Exception e) {
          e.printStackTrace();
          theLog.printError("EntityAudio -- getAudio failed"+e.toString()); 
          throwStorageObjectException(e, "EntityAudio -- getAudio failed: ");
        }
        finally {
          try {
            con.setAutoCommit(true);
          } catch (Exception e) {
            e.printStackTrace();
            theLog.printError(
                    "EntityAudio -- getAudio reseting transaction mode failed"
                    +e.toString()); 
          }
          theStorageObject.freeConnection(con,stmt);
        }

		return data;
	}

	public void setAudio(byte[] uploadData)
	    throws StorageObjectException {

		if (uploadData!=null) {
			java.sql.Connection con=null;PreparedStatement pstmt=null;
			try {

				if (uploadData!=null) {
					con = theStorageObject.getPooledCon();
					con.setAutoCommit(false);
					theLog.printDebugInfo("setaudio :: trying to insert audio");

					// setting values
                    LargeObjectManager lom;
                    java.sql.Connection jCon;
                    jCon = ((com.codestudio.sql.PoolManConnectionHandle)con)
                            .getNativeConnection();
                    lom = ((org.postgresql.Connection)jCon).getLargeObjectAPI();
                    int oid = lom.create();
                    LargeObject lob = lom.open(oid);
                    lob.write(uploadData);
                    lob.close();
                    String sql = "update images set"
                                 +" audio_data="+oid
                                 +" where id="+getId();
					theLog.printDebugInfo("setaudio :: updating: "+ sql);
					pstmt = con.prepareStatement(sql);
					//pstmt.setBytes(1, imageData);
					//pstmt.setBytes(2, iconData);
					pstmt.executeUpdate();
					sql="update content set is_produced='0' where to_media="+getId();
					pstmt = con.prepareStatement(sql);
					pstmt.executeUpdate();
				}
			}
			catch (Exception e) {
                throwStorageObjectException(e,"setaudio ::failed: ");
            }
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
			throwStorageObjectException(e, "EntityAudio :: update :: failed!! ");
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

}
