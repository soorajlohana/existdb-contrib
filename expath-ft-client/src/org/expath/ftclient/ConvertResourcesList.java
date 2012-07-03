package org.expath.ftclient;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.expath.ftclient.FTP.FTP;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConvertResourcesList {

	private static String namespaceUri = "";
	static {
		namespaceUri = ExpathFTClientModule.NAMESPACE_URI;
	}
	private static String prefix = "";
	static {
		prefix = ExpathFTClientModule.PREFIX;
	}
	
	private static final Logger log = Logger.getLogger(ConvertResourcesList.class);

	public static Document convert(String resourcesString) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation impl = builder.getDOMImplementation();

		Document resourcesDoc = impl.createDocument(namespaceUri, prefix + ":resources-list", null);
		Element resourcesDocRoot = resourcesDoc.getDocumentElement();

		String[] resourceStrings = resourcesString.split("\n");
		int resourceStringsLength = resourceStrings.length;
		if (resourceStringsLength  > 1) {
			for (int i = 0, il = resourceStringsLength ; i < il; i++) {
				String[] resourceMetadata = resourceStrings[i].split("\t");
				Element resourceElement = resourcesDoc.createElementNS(namespaceUri, prefix + ":resource");
				resourcesDocRoot.appendChild(resourceElement);
				resourceElement.setAttribute("type", resourceMetadata[1]);
				resourceElement.setAttribute("last-modified", resourceMetadata[2]);
				resourceElement.setAttribute("size", resourceMetadata[3]);
				resourceElement.setAttribute("human-readable-size", resourceMetadata[4]);
				resourceElement.setAttribute("user", resourceMetadata[5]);
				resourceElement.setAttribute("group", resourceMetadata[6]);
				resourceElement.setAttribute("permissions", resourceMetadata[7]);
				if (resourceMetadata.length == 9) {
					resourceElement.setAttribute("link-to", resourceMetadata[8]);
				}
				resourceElement.setTextContent(resourceMetadata[0]);
			}
		}

		return resourcesDoc;
	}
}
