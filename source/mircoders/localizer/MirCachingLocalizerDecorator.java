/*
 * Copyright (C) 2001, 2002 The Mir-coders group
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
 * the code of this program with  any library licensed under the Apache Software License, 
 * The Sun (tm) Java Advanced Imaging library (JAI), The Sun JIMI library 
 * (or with modified versions of the above that use the same license as the above), 
 * and distribute linked combinations including the two.  You must obey the 
 * GNU General Public License in all respects for all of the code used other than 
 * the above mentioned libraries.  If you modify this file, you may extend this 
 * exception to your version of the file, but you are not obligated to do so.  
 * If you do not wish to do so, delete this exception statement from your version.
 */

package mircoders.localizer;

public class MirCachingLocalizerDecorator implements MirLocalizer {
  private MirLocalizer localizer;
  private MirProducerLocalizer producerLocalizer;
  private MirGeneratorLocalizer generatorLocalizer;
  private MirOpenPostingLocalizer openPostingsLocalizer;
  private MirProducerAssistantLocalizer producerAssistantLocalizer;
  private MirDataModelLocalizer dataModelLocalizer;
  private MirAdminInterfaceLocalizer adminInterfaceLocalizer;

  public MirCachingLocalizerDecorator(MirLocalizer aLocalizer) {
    localizer = aLocalizer;
  }

  public MirProducerLocalizer producers() throws MirLocalizerFailure, MirLocalizerExc {
    if (producerLocalizer==null) {
      producerLocalizer = localizer.producers();
    }

    return producerLocalizer;
  }

  public MirGeneratorLocalizer generators() throws MirLocalizerFailure, MirLocalizerExc {
    if (generatorLocalizer==null) {
      generatorLocalizer = localizer.generators();
    }

    return generatorLocalizer;
  }

  public MirOpenPostingLocalizer openPostings() throws MirLocalizerFailure, MirLocalizerExc {
    if (openPostingsLocalizer==null) {
      openPostingsLocalizer = localizer.openPostings();
    }

    return openPostingsLocalizer;
  }

  public MirProducerAssistantLocalizer producerAssistant() throws MirLocalizerFailure, MirLocalizerExc {
    if (producerAssistantLocalizer==null) {
      producerAssistantLocalizer = localizer.producerAssistant();
    }

    return producerAssistantLocalizer;
  }

  public MirDataModelLocalizer dataModel() throws MirLocalizerFailure, MirLocalizerExc {
    if (dataModelLocalizer==null) {
      dataModelLocalizer = localizer.dataModel();
    }

    return dataModelLocalizer;
  }

  public MirAdminInterfaceLocalizer adminInterface() throws MirLocalizerFailure, MirLocalizerExc {
    if (adminInterfaceLocalizer==null) {
      adminInterfaceLocalizer = localizer.adminInterface();
    }

    return adminInterfaceLocalizer;
  };


}