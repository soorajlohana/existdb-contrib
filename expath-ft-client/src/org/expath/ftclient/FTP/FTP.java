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
package org.expath.ftclient.FTP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.expath.ftclient.ConvertResourcesList;
import org.expath.ftclient.ExpathFTClientModule;
import org.expath.ftclient.InputStream2Base64String;
import org.w3c.dom.Document;

/**
 * Implements a public interface for a FTP connection.
 * 
 * @author Claudius Teodorescu <claudius.teodorescu@gmail.com>
 */
public class FTP {
	private static final Logger log = Logger.getLogger(FTP.class);
	private static String moduleNsUri = "";
	static {
		moduleNsUri = ExpathFTClientModule.NAMESPACE_URI;
	}
	private static String modulePrefix = "";
	static {
		modulePrefix = ExpathFTClientModule.PREFIX;
	}

	public <X> X connect(URI remoteHostURI, String username, String password, String remoteHost, int remotePort,
			String clientPrivateKey) throws Exception {
		long startTime = new Date().getTime();
		X connection = null;
		FTPClient FTPconnection = new FTPClient();
		try {
			remotePort = (remotePort == -1) ? (int) 21 : remotePort;
			FTPconnection.setDefaultTimeout(60 * 1000);
			FTPconnection.setRemoteVerificationEnabled(false);
			// FTPconnection.setSoTimeout( 60 * 1000 );
			// FTPconnection.setDataTimeout( 60 * 1000 );
			FTPconnection.connect(remoteHost, remotePort);
			FTPconnection.login(username, password);
			FTPconnection.enterLocalPassiveMode();
			FTPconnection.setFileType(FTPClient.BINARY_FILE_TYPE);
			// FTPconnection.setControlKeepAliveTimeout(300);
			// Check reply code for success
			int reply = FTPconnection.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				FTPconnection.disconnect();
				throw new Exception(
						"err:FTC005: Authentication failed. The username, password, or private key is wrong.");
			} else {
				connection = (X) FTPconnection;
			}
		} catch (IOException se) {
			if (FTPconnection.isConnected()) {
				try {
					FTPconnection.disconnect();
				} catch (IOException ioe) {
					throw new Exception(
							"err:FTC005: Authentication failed. The username, password, or private key is wrong.");
				}
			}
		}
		log.info("The FTP sub-module connected to '" + remoteHostURI + "' in " + (new Date().getTime() - startTime)
				+ " ms.");
		return connection;
	}

	public StreamResult listResources(Object remoteConnection, String remoteResourcePath) throws Exception {
		long startTime = new Date().getTime();
		FTPClient FTPconnection = (FTPClient) remoteConnection;
		if (!FTPconnection.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		List FTPconnectionObject = _checkResourcePath(FTPconnection, remoteResourcePath);

		FTPFile[] resources = (FTPFile[]) FTPconnectionObject.get(1);

		StringWriter writer = new StringWriter();
		XMLStreamWriter xmlWriter = null;

		try {
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
			xmlWriter.setPrefix(modulePrefix, moduleNsUri);
			xmlWriter.writeStartDocument();
			xmlWriter.writeStartElement(modulePrefix + ":resources-list");
			xmlWriter.writeNamespace(modulePrefix, moduleNsUri);
			for (FTPFile resource : resources) {
				_generateResourceElement(xmlWriter, resource, null, "");
			}
			xmlWriter.writeEndElement();
			xmlWriter.writeEndDocument();
			xmlWriter.close();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}

		StreamResult resultAsStreamResult = new StreamResult(writer);
		log.info("The FTP sub-module retrieved the list of resources in " + (new Date().getTime() - startTime) + " ms.");

		return resultAsStreamResult;
	}

	public StreamResult getResourceMetadata(Object remoteConnection, String remoteResourcePath) throws Exception {
		long startTime = new Date().getTime();
		FTPClient FTPconnection = (FTPClient) remoteConnection;
		if (!FTPconnection.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		List FTPconnectionObject = _checkResourcePath(FTPconnection, remoteResourcePath);

		FTPFile[] resources = (FTPFile[]) FTPconnectionObject.get(1);

		StringWriter writer = new StringWriter();
		XMLStreamWriter xmlWriter = null;

		try {
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
			xmlWriter.setPrefix(modulePrefix, moduleNsUri);
			xmlWriter.writeStartDocument();
			for (FTPFile resource : resources) {
				_generateResourceElement(xmlWriter, resource, null, remoteResourcePath);
			}
			xmlWriter.writeEndDocument();
			xmlWriter.close();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}

		StreamResult resultAsStreamResult = new StreamResult(writer);

		log.info("The FTP sub-module retrieved the metadata for resource '" + remoteResourcePath + "' in "
				+ (new Date().getTime() - startTime) + " ms.");

		return resultAsStreamResult;
	}

	public StreamResult retrieveResource(Object remoteConnection, String remoteResourcePath) throws Exception {
		long startTime = new Date().getTime();
		FTPClient connection = (FTPClient) remoteConnection;
		if (!connection.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		List connectionObject = _checkResourcePath(connection, remoteResourcePath);

		FTPFile[] resources = (FTPFile[]) connectionObject.get(1);

		InputStream is = (InputStream) connectionObject.get(2);

		StringWriter writer = new StringWriter();
		XMLStreamWriter xmlWriter = null;

		try {
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
			xmlWriter.setPrefix(modulePrefix, moduleNsUri);
			xmlWriter.writeStartDocument();
			for (FTPFile resource : resources) {
				_generateResourceElement(xmlWriter, resource, is, remoteResourcePath);
			}
			xmlWriter.writeEndDocument();
			xmlWriter.close();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}

		StreamResult resultAsStreamResult = new StreamResult(writer);

		if (!connection.completePendingCommand()) {
			throw new Exception("err:FTC007: The current operation failed.");
		}

		log.info("The FTP sub-module retrieved the resource '" + remoteResourcePath + "' in "
				+ (new Date().getTime() - startTime) + " ms.");

		return resultAsStreamResult;
	}

	public boolean storeResource(Object remoteConnection, String remoteDirectoryPath, String resourceName,
			InputStream resourceInputStream) throws Exception {
		long startTime = new Date().getTime();
		FTPClient FTPconnection = (FTPClient) remoteConnection;
		if (!FTPconnection.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		Boolean result = true;
		try {
			if (resourceName.length() == 0) {
				resourceName = remoteDirectoryPath.substring(remoteDirectoryPath.lastIndexOf("/") + 1);
				remoteDirectoryPath = remoteDirectoryPath.substring(0, remoteDirectoryPath.lastIndexOf("/"));
				_checkResourcePath(FTPconnection, remoteDirectoryPath);
				result = FTPconnection.makeDirectory(resourceName);
			} else {
				_checkResourcePath(FTPconnection, remoteDirectoryPath);
				result = FTPconnection.storeFile(resourceName, resourceInputStream);
			}
		} catch (IOException ioe) {
			log.error(ioe.getMessage(), ioe);
			// TODO: add throw exception here for cases when server doesn't
			// allow storage of file - a use case is when vsftpd was configured
			// with mandatory SSL encryption
			result = false;
		}

		// if(!FTPconnection.completePendingCommand()) {
		// throw new Exception(
		// "err:FTC007: The current operation failed.");
		// }

		log.info("The FTP sub-module stored the resource '" + resourceName + "' at '" + remoteDirectoryPath + "' in "
				+ (new Date().getTime() - startTime) + " ms.");

		return result;
	}

	public boolean deleteResource(Object remoteConnection, String remoteResourcePath) throws Exception {
		long startTime = new Date().getTime();
		FTPClient FTPconnection = (FTPClient) remoteConnection;
		log.info("The FTP sub-module get the FTP connection in " + (new Date().getTime() - startTime) + " ms.");
		if (!FTPconnection.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}
		log.info("The FTP sub-module checked the FTP connection in " + (new Date().getTime() - startTime) + " ms.");
		Boolean result = true;
		List FTPconnectionObject = _checkResourcePath(FTPconnection, remoteResourcePath);
		log.info("The FTP sub-module checked the resource path in " + (new Date().getTime() - startTime) + " ms.");
		try {
			if ((Boolean) FTPconnectionObject.get(0)) {
				FTPconnection.removeDirectory(remoteResourcePath);
				log.info("The FTP sub-module deleted the directory in " + (new Date().getTime() - startTime) + " ms.");
			} else {
				FTPconnection.deleteFile(remoteResourcePath);
				log.info("The FTP sub-module deleted the file in " + (new Date().getTime() - startTime) + " ms.");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			result = false;
		}

//		if (!FTPconnection.completePendingCommand()) {
//			throw new Exception("err:FTC007: The current operation failed.");
//		}

		log.info("The FTP sub-module deleted the resource '" + remoteResourcePath + "' in "
				+ (new Date().getTime() - startTime) + " ms.");

		return result;
	}

	public static Boolean disconnect(Object remoteConnection) throws Exception {
		long startTime = new Date().getTime();
		FTPClient FTPconnection = (FTPClient) remoteConnection;
		if (!FTPconnection.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		Boolean result = true;

		// try {
		// // close the Connection
		// long startTime = new Date().getTime();
		// //FTPconnection.logout();
		// log.info("Logout was done in " + (new Date().getTime() - startTime) +
		// " ms.");
		// } catch (IOException ioe) {
		// // log.error(ioe.getMessage(), ioe);
		// result = false;
		// } finally {
		// long startTime = new Date().getTime();
		// try {
		// FTPconnection.disconnect();
		// } catch (IOException ioe) {
		// log.error(ioe.getMessage(), ioe);
		// result = false;
		// }
		// log.info("Disconnection was done in " + (new Date().getTime() -
		// startTime) + " ms.");
		// }

		try {
			// FTPconnection.logout();
			FTPconnection.disconnect();
			log.info("The FTP sub-module disconnected in " + (new Date().getTime() - startTime) + " ms.");
		} catch (IOException ioe) {
			log.error(ioe.getMessage(), ioe);
			result = false;
		}

		return result;
	}

	private static List _checkResourcePath(FTPClient FTPconnection, String remoteResourcePath) throws Exception {
		InputStream is = null;
		FTPFile[] resources = null;
		List connectionObject = new LinkedList();

		if (FTPconnection.getStatus(remoteResourcePath) != null) {
			boolean remoteDirectoryExists = FTPconnection.changeWorkingDirectory(remoteResourcePath);
			connectionObject.add(remoteDirectoryExists);
			resources = FTPconnection.listFiles(remoteResourcePath);
			if (!remoteDirectoryExists) {
				FTPconnection.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
				is = FTPconnection.retrieveFileStream(remoteResourcePath);
				if (is == null || resources.length == 0) {
					throw new Exception("err:FTC004: The user has no rights to access the remote resource.");
				}
			}
		} else {
			throw new Exception("err:FTC003: The remote resource does not exist.");
		}

		connectionObject.add(resources);
		connectionObject.add(is);

		return connectionObject;
	}

	private static void _generateResourceElement(XMLStreamWriter xmlWriter, FTPFile resource, InputStream is,
			String remoteResourcePath) throws IOException, Exception {
		xmlWriter.writeStartElement(modulePrefix + ":resource");
		xmlWriter.writeNamespace(modulePrefix, moduleNsUri);
		_generateMetadataAttributes(xmlWriter, resource, null, remoteResourcePath);
		if (is != null) {
			xmlWriter.writeCharacters(InputStream2Base64String.convert(is));
		}
		xmlWriter.writeEndElement();
	}

	private static void _generateMetadataAttributes(XMLStreamWriter xmlWriter, FTPFile resource, InputStream is,
			String remoteResourcePath) throws IOException, Exception {
		Calendar resourceTimeStamp = resource.getTimestamp();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		String lastModified = formatter.format(resourceTimeStamp.getTimeInMillis());
		lastModified = lastModified.replace(" ", "T");
		lastModified = lastModified.substring(0, 22) + ":" + lastModified.substring(22, 24);
		long resourceSize = resource.getSize();

		xmlWriter.writeAttribute("name", resource.getName());
		xmlWriter.writeAttribute("type", ((resource.getType() == 1) ? "directory"
				: (((resource.getType() == 0) ? "file" : "link"))));
		xmlWriter.writeAttribute("absolute-path", remoteResourcePath);
		xmlWriter.writeAttribute("last-modified", lastModified);
		xmlWriter.writeAttribute("size", String.valueOf(resourceSize));
		xmlWriter.writeAttribute("human-readable-size",
				org.apache.commons.io.FileUtils.byteCountToDisplaySize(resourceSize));
		xmlWriter.writeAttribute("user", resource.getUser());
		xmlWriter.writeAttribute("user-group", resource.getGroup());
		xmlWriter.writeAttribute("permissions", resource.getRawListing().substring(0, 10));
		String linkTo = resource.getLink();
		if (linkTo != null) {
			xmlWriter.writeAttribute("link-to", linkTo);
		}
	}

}
