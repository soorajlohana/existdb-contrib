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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 * Implements the module definition.
 * 
 * @author Claudius Teodorescu <claudius.teodorescu@gmail.com>
 */
public class ExpathFTClientModule {

	public final static String NAMESPACE_URI = "http://expath.org/ns/ft-client";
	public final static String PREFIX = "ft-client";
	public final static String VERSION = "1.1";
	public final static String MODULE_DESCRIPTION = "A module for performing File Transfer requests as a client.";
	public final static String MODULE_NAME = "EXPath File Transfer Client";

	protected final static String SUPPORTED_PROTOCOLS = " FTP SFTP FTPS FTPES HAL ";
	protected final static Map<String, Boolean> LOADED_PROTOCOLS = new HashMap<String, Boolean>();
	protected final static Map<String, String> PROTOCOL_CLASS_CODES = new HashMap<String, String>();
	static {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		//TODO: put ACTUAL package name here
		String modulePackageName = "org.expath.ftclient";

		modulePackageName = modulePackageName.replace(".", "/")
				+ "/";

		String[] supportedProtocols = SUPPORTED_PROTOCOLS.split(" ");
		for (int i = 1, il = supportedProtocols.length; i < il; i++) {
			String protocol = supportedProtocols[i];
			String protocolPackageName = modulePackageName + protocol;
			if (classLoader.getResource(protocolPackageName) != null) {
				// load protocol name as loaded protocol
				LOADED_PROTOCOLS.put(protocol, true);
			}
		}
	}
	static {
		PROTOCOL_CLASS_CODES.put("org.apache.commons.net.ftp.FTPClient", "FTP");
		PROTOCOL_CLASS_CODES.put("com.jcraft.jsch.Session", "SFTP");
		PROTOCOL_CLASS_CODES.put("java.util.LinkedList", "HAL");
	}
}