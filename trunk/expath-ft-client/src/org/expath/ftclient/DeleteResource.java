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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
/**
 * Implements a method for deleting a remote resource.
 * 
 * @author Claudius Teodorescu <claudius.teodorescu@gmail.com>
 */
public class DeleteResource {
	private static final Logger log = Logger.getLogger(DeleteResource.class);
	private static final String moduleName = ExpathFTClientModule.MODULE_NAME;

    public static boolean deleteResource(Object remoteConnection, String remoteResourcePath) throws Exception {
        
        Boolean result = true;
        String protocol = ExpathFTClientModule.PROTOCOL_CLASS_CODES.get(remoteConnection.getClass().getName());

        Class<?> clazz = Class.forName("org.expath.ftclient." + protocol + "." + protocol);
        Method method = clazz.getMethod("deleteResource", new Class<?>[] {Object.class, String.class});
        try {
            result = (Boolean) method.invoke(clazz.newInstance(), new Object[] {remoteConnection, remoteResourcePath});
            log.info(moduleName + " deleted the resource '" + remoteResourcePath + "'.");
        } catch(InvocationTargetException ex) {
            throw new Exception(ex.getCause().getMessage());
        }
        
        return result;
    }
}