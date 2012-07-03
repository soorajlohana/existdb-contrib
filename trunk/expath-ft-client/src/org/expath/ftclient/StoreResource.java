/*
 *  Copyright (C) 2011 Claudius Teodorescu
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.expath.ftclient;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Implements a method for storing a resource to a remote directory.
 *
 * @author Claudius Teodorescu <claudius.teodorescu@gmail.com>
 */
public class StoreResource {
	private static final Logger log = Logger.getLogger(StoreResource.class);
	private static final String moduleName = ExpathFTClientModule.MODULE_NAME;

    public static boolean storeResource(Object remoteConnection, String remoteResourcePath, InputStream resourceInputStream) throws Exception {
        
        Boolean result = true;

        String remoteDirectoryPath = remoteResourcePath.substring(0, remoteResourcePath.lastIndexOf("/"));

        String resourceName = remoteResourcePath.substring(remoteResourcePath.lastIndexOf("/") + 1);
        
        String protocol = ExpathFTClientModule.PROTOCOL_CLASS_CODES.get(remoteConnection.getClass().getName());

        Class<?> clazz = Class.forName("org.expath.ftclient." + protocol + "." + protocol);
        Method method = clazz.getMethod("storeResource", new Class<?>[] {Object.class, String.class, String.class, InputStream.class});
        try {
            result = (Boolean) method.invoke(clazz.newInstance(), new Object[] {remoteConnection, remoteDirectoryPath, resourceName, resourceInputStream});
            log.info(moduleName + " stored the resource '" + remoteResourcePath + "' at '" + remoteDirectoryPath + "'.");
        } catch(InvocationTargetException ex) {
            throw new Exception(ex.getCause().getMessage());
        }
        
        return result;
    }
}