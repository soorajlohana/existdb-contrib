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
package org.expath.ftclient.SFTP;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.expath.ftclient.ConvertResourcesList;
import org.expath.ftclient.ExpathFTClientModule;
import org.expath.ftclient.InputStream2Base64String;
import org.w3c.dom.Document;

/**
 * Implements a public interface for a SFTP connection.
 * 
 * @author Claudius Teodorescu <claudius.teodorescu@gmail.com>
 */
public class SFTP {

	private static final Logger log = Logger.getLogger(SFTP.class);
	private static String moduleNsUri = "";
	static {
		moduleNsUri = ExpathFTClientModule.NAMESPACE_URI;
	}
	private static String modulePrefix = "";
	static {
		modulePrefix = ExpathFTClientModule.PREFIX;
	}

	public <X> X connect(URI remoteHostURI, String username, String password, String remoteHost, int remotePort,
			InputStream clientPrivateKey) throws Exception {
		long startTime = new Date().getTime();
		X connection = null;
		remotePort = (remotePort == -1) ? (int) 22 : remotePort;
		JSch jSch = new JSch();
		File clientPrivateKeyTempFile = null;
		Session SFTPconnection = null;
		try {
			if (clientPrivateKey != null) {
				try {
					String uuid = UUID.randomUUID().toString();
					clientPrivateKeyTempFile = File.createTempFile("SFTPprivateKey" + uuid, ".pem");
					OutputStream out = new FileOutputStream(clientPrivateKeyTempFile);
					byte buf[] = new byte[1024];
					int len;
					while((len = clientPrivateKey.read(buf))>0) {
						out.write(buf,0,len);
					}
					out.close();
					clientPrivateKey.close();
					jSch.addIdentity(clientPrivateKeyTempFile.getCanonicalPath());
					clientPrivateKeyTempFile.delete();
				} catch (IOException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			SFTPconnection = jSch.getSession(username, remoteHost, remotePort);
			SFTPconnection.setConfig("StrictHostKeyChecking", "no");
			SFTPconnection.setPassword(password);
			SFTPconnection.connect();
			connection = (X) SFTPconnection;
			log.info("The SFTP sub-module connected to '" + remoteHostURI + "' in " + (new Date().getTime() - startTime)
					+ " ms.");
		} catch (JSchException ex) {
			log.error(ex.getMessage(), ex);
			throw new Exception("err:FTC005: Authentication failed. The username, password, or private key is wrong.");
		}
		
		return connection;
	}

	public StreamResult listResources(Object remoteConnection, String remoteResourcePath) throws Exception {
		long startTime = new Date().getTime();
		Session session = (Session) remoteConnection;
		if (!session.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		Channel SFTPchannel = null;
		ChannelSftp connection = null;

		try {
			SFTPchannel = session.openChannel("sftp");
			connection = (ChannelSftp) SFTPchannel;
			SFTPchannel.connect();
		} catch (JSchException ex) {
			log.error(ex.getMessage(), ex);
		}
		List connectionObject = _checkResourcePath(connection, remoteResourcePath);
		connection = (ChannelSftp) connectionObject.get(1);
		Vector<LsEntry> resources = (Vector<LsEntry>)connectionObject.get(2);

		StringWriter writer = new StringWriter();
		XMLStreamWriter xmlWriter = null;

		try {
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
			xmlWriter.setPrefix(modulePrefix, moduleNsUri);
			xmlWriter.writeStartDocument();
			xmlWriter.writeStartElement(modulePrefix + ":resources-list");
			xmlWriter.writeNamespace(modulePrefix, moduleNsUri);
			for (LsEntry resource : resources) {
				if (resource.getFilename().equals(".") || resource.getFilename().equals("..")) {
					continue;
				}				
				_generateResourceElement(xmlWriter, resource, null, "", connection);
			}
			xmlWriter.writeEndElement();
			xmlWriter.writeEndDocument();
			xmlWriter.close();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}

		StreamResult resultAsStreamResult = new StreamResult(writer);

		connection.disconnect();

		log.info("The SFTP sub-module retrieved the list of resources in " + (new Date().getTime() - startTime)
				+ " ms.");

		return resultAsStreamResult;
	}

	public StreamResult retrieveResource(Object remoteConnection, String remoteResourcePath) throws Exception {
		long startTime = new Date().getTime();
		Session session = (Session) remoteConnection;
		if (!session.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		Channel channel = null;
		ChannelSftp connection = null;
		try {
			channel = session.openChannel("sftp");
			connection = (ChannelSftp) channel;
			channel.connect();
		} catch (JSchException ex) {
			log.error(ex.getMessage(), ex);
		}

		List connectionObject = _checkResourcePath(connection, remoteResourcePath);

		Vector<LsEntry> resources = (Vector<LsEntry>)connectionObject.get(2);

		InputStream is = (InputStream) connectionObject.get(3);

		StringWriter writer = new StringWriter();
		XMLStreamWriter xmlWriter = null;

		try {
			xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
			xmlWriter.setPrefix(modulePrefix, moduleNsUri);
			xmlWriter.writeStartDocument();
			for (LsEntry resource : resources) {
				_generateResourceElement(xmlWriter, resource, is, remoteResourcePath, connection);
			}
			xmlWriter.writeEndDocument();
			xmlWriter.close();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		
		StreamResult resultAsStreamResult = new StreamResult(writer);

		log.info("The SFTP sub-module retrieved the resource '" + remoteResourcePath + "' in "
				+ (new Date().getTime() - startTime) + " ms.");

		return resultAsStreamResult;
	}

	public boolean storeResource(Object remoteConnection, String remoteDirectoryPath, String resourceName,
			InputStream resourceInputStream) throws Exception {
		long startTime = new Date().getTime();
		Session session = (Session) remoteConnection;
		if (!session.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		Boolean result = true;
		Channel channel = null;
		ChannelSftp SFTPconnection = null;
		try {
			channel = session.openChannel("sftp");
			SFTPconnection = (ChannelSftp) channel;
			channel.connect();
		} catch (JSchException ex) {
			log.error(ex.getMessage(), ex);
		}
		try {
			List SFTPconnectionObject = null;
			if (resourceName.length() == 0) {
				resourceName = remoteDirectoryPath.substring(remoteDirectoryPath.lastIndexOf("/") + 1);
				remoteDirectoryPath = remoteDirectoryPath.substring(0, remoteDirectoryPath.lastIndexOf("/"));
				_checkResourcePath(SFTPconnection, remoteDirectoryPath);
				SFTPconnection.mkdir(resourceName);
				log.info("remoteDirectoryPath '" + remoteDirectoryPath + "'");
				log.info("resourceName '" + resourceName + "'");
			} else {
				_checkResourcePath(SFTPconnection, remoteDirectoryPath);
				SFTPconnection.put(resourceInputStream, resourceName);
			}
			log.info("The SFTP sub-module stored the resource '" + resourceName + "' at '" + remoteDirectoryPath + "' in "
					+ (new Date().getTime() - startTime) + " ms.");			
		} catch (SftpException ex) {
			System.out.println("ex.getMessage(): " + ex.getMessage() + ".\n");
			log.error(ex.getMessage(), ex);
			result = false;
		}
		
		return result;
	}

	public boolean deleteResource(Object remoteConnection, String remoteResourcePath) throws Exception {
		long startTime = new Date().getTime();
		Session session = (Session) remoteConnection;
		if (!session.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		Boolean result = true;
		Channel channel = null;
		ChannelSftp SFTPconnection = null;
		try {
			channel = session.openChannel("sftp");
			SFTPconnection = (ChannelSftp) channel;
			channel.connect();
		} catch (JSchException ex) {
			log.error(ex.getMessage(), ex);
		}

		List SFTPconnectionObject = _checkResourcePath(SFTPconnection, remoteResourcePath);
		SFTPconnection = (ChannelSftp) SFTPconnectionObject.get(1);

		try {
			if ((Boolean) SFTPconnectionObject.get(0)) {
				SFTPconnection.rmdir(remoteResourcePath);
			} else {
				SFTPconnection.rm(remoteResourcePath);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			result = false;
		}
		
		log.info("The SFTP sub-module deleted the resource '" + remoteResourcePath + "' in "
				+ (new Date().getTime() - startTime) + " ms.");

		return result;
	}

	public static Boolean disconnect(Object remoteConnection) throws Exception {
		long startTime = new Date().getTime();
		Session SFTPconnection = (Session) remoteConnection;
		if (!SFTPconnection.isConnected()) {
			throw new Exception("err:FTC002: The connection was closed by server.");
		}

		Boolean result = true;

		try {
			// close the Connection
			SFTPconnection.disconnect();
			log.info("The SFTP sub-module disconnected in " + (new Date().getTime() - startTime) + " ms.");
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			result = false;
		} finally {
			if (SFTPconnection.isConnected()) {
				try {
					SFTPconnection.disconnect();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
					result = false;
				}
			}
		}

		return result;
	}

	public static List _checkResourcePath(ChannelSftp SFTPconnection, String remoteResourcePath) throws Exception,
			SftpException {
		InputStream is = null;
		Vector<LsEntry> resources = null;
		List connectionObject = new LinkedList();
		SftpATTRS stat = null;

		try {
			stat = SFTPconnection.lstat(remoteResourcePath);
		} catch (SftpException ex) {
			throw new Exception("err:FTC003: The remote resource does not exist.");
		}
		try {
			// case when the resource is directory
			if (stat.isDir()) {
				connectionObject.add(true);
				SFTPconnection.cd(remoteResourcePath);
				resources = SFTPconnection.ls(".");
			} else {// case when the resource is not directory
				connectionObject.add(false);
				is = SFTPconnection.get(remoteResourcePath);
				resources = SFTPconnection.ls(remoteResourcePath);
			}
		} catch (SftpException ex) {
			throw new Exception("err:FTC004: The user has no rights to access the remote resource.");
		}

		connectionObject.add(SFTPconnection);
		connectionObject.add(resources);
		connectionObject.add(is);

		return connectionObject;
	}

	private static void _generateResourceElement(XMLStreamWriter xmlWriter, LsEntry resource, InputStream is,
			String remoteResourcePath, ChannelSftp connection) throws IOException, Exception {
		xmlWriter.writeStartElement(modulePrefix + ":resource");
		xmlWriter.writeNamespace(modulePrefix, moduleNsUri);
		_generateMetadataAttributes(xmlWriter, resource, null, remoteResourcePath, connection);
		if (is != null) {
			xmlWriter.writeCharacters(InputStream2Base64String.convert(is));
		}
		xmlWriter.writeEndElement();
	}

	private static void _generateMetadataAttributes(XMLStreamWriter xmlWriter, LsEntry resource, InputStream is,
			String remoteResourcePath, ChannelSftp connection) throws IOException, Exception {
		DateFormat SFTPdateStringFormatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

		Date SFTPdate = null;
		try {
			SFTPdate = (Date) SFTPdateStringFormatter.parse(resource.getAttrs().getMtimeString());
		} catch (ParseException ex) {
			log.error(ex);
		}
		DateFormat XSDdateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		String lastModified = XSDdateTimeFormatter.format(SFTPdate);
		lastModified = lastModified.replace(" ", "T");
		lastModified = lastModified.substring(0, 22) + ":" + lastModified.substring(22, 24);
		String userGroup = resource.getLongname().substring(10).trim().replaceFirst("^[\\S]+", "").trim();
		String user = userGroup.substring(0, userGroup.indexOf(" "));
		String group = userGroup.substring(userGroup.indexOf(" ")).trim();
		group = group.substring(0, group.indexOf(" "));
		long resourceSize = resource.getAttrs().getSize();

		xmlWriter.writeAttribute("name", resource.getFilename());
		xmlWriter.writeAttribute("type", ((resource.getAttrs().isDir()) ? "directory"
				: (((resource.getAttrs().isLink()) ? "link" : "file"))));
		xmlWriter.writeAttribute("last-modified", lastModified);
		xmlWriter.writeAttribute("size", String.valueOf(resourceSize));
		xmlWriter.writeAttribute("human-readable-size",
				org.apache.commons.io.FileUtils.byteCountToDisplaySize(resourceSize));
		xmlWriter.writeAttribute("user", user);
		xmlWriter.writeAttribute("user-group", group);
		xmlWriter.writeAttribute("permissions", resource.getAttrs().getPermissionsString());
		if (resource.getAttrs().isLink()) {
			xmlWriter.writeAttribute("link-to", connection.readlink(resource.getFilename()));
		}
	}
}