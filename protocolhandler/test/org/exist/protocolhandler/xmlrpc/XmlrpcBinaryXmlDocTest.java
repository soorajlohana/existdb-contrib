/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
 *  http://exist-db.org
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
package org.exist.protocolhandler.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exist.protocolhandler.eXistURLStreamHandlerFactory;
import org.exist.protocolhandler.xmldb.XmldbURL;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  jUnit tests for XmlrpcInputStream class.
 *
 * @author Dannes Wessels
 */
public class XmlrpcBinaryXmlDocTest {

    private static Logger LOG = Logger.getLogger(XmlrpcBinaryXmlDocTest.class);
    private String TESTCASENAME = getClass().getName();

    @BeforeClass
    public static void setUp() throws Exception {
        PropertyConfigurator.configure("log4j.conf");
        URL.setURLStreamHandlerFactory(new eXistURLStreamHandlerFactory());
    }


    // ***************************************

    private void sendDocument(XmldbURL uri, InputStream is) throws IOException {

        // Setup
        XmlrpcOutputStream xos = new XmlrpcOutputStream(uri);

        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            xos.write(buf, 0, len);
        }

        // Shutdown
        xos.close();
    }

    // Copy document from URL to outputstream

    private void getDocument(XmldbURL uri, OutputStream os) throws IOException {

        // Setup
        InputStream xis = new XmlrpcInputStream(uri);

        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = xis.read(buf)) > 0) {
            os.write(buf, 0, len);
        }

        // Shutdown
        xis.close(); // required; checks wether all is OK
    }

    // ***************************************

    @Test
    public void createCollection() {
        try {
            URL url = new URL("http://localhost:8080/exist/rest/db?_query=" + "xmldb:create-collection(%22/db/%22,%22" + TESTCASENAME + "%22)");
            url.openStream();

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }

    @Test
    public void toDB_XmlDoc() {
        System.out.println("toDB_XmlDoc");
        try {
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/" + TESTCASENAME + "/build.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }

    @Test
    public void fromDB_XmlDoc() {
        System.out.println("fromDB_XmlDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/" + TESTCASENAME + "/build.xml";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            assertTrue(baos.size() > 0);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }

    // ***************************************

    @Test
    public void toDB_NotExistingCollection_XmlDoc() {
        System.out.println("toDB_NotExistingCollection_XmlDoc");
        try {
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/" + TESTCASENAME + "/notexisting/build.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            fis.close();
            fail("Not existing collection: Expected exception");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Collection .* not found")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    @Test
    public void fromDB_NotExistingDoc_XmlDoc() {
        System.out.println("fromDB_NotExistingDoc_XmlDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/" + TESTCASENAME + "/foobar.xml";
        try {
            XmldbURL xmldbUri = xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("Not existing document: exception should be thrown");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*document not found.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getCause().getMessage());
            }
        }
    }

    // *************

    @Test
    public void toDB_BinaryDoc() {
        System.out.println("voidtoDB_BinaryDoc");
        try {
            FileInputStream fis = new FileInputStream("manifest.mf");
            String uri = "xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/" + TESTCASENAME + "/manifest.mf";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            fis.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }

    @Test
    public void fromDB_BinaryDoc() throws Exception {
        System.out.println("fromDB_BinaryDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/" + TESTCASENAME + "/manifest.mf";
        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);

            assertTrue("Filesize must be greater than 0", baos.size() > 0);
            assertEquals(85, baos.size());

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getCause().getMessage());
        }
    }

    // *************

    @Test
    public void toDB_NotExistingCollection_BinaryDoc() {
        System.out.println("toDB_NotExistingCollection_BinaryDoc");
        try {
            FileInputStream fis = new FileInputStream("manifest.mf");
            String uri = "xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/" + TESTCASENAME + "/notexisting/manifest.mf";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            fis.close();

            fail("Not existing collection: Expected exception");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Collection .* not found")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    @Test
    public void fromDB_NotExistingDoc_BinaryDoc() throws Exception {
        System.out.println("fromDB_NotExistingDoc_BinaryDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/" + TESTCASENAME + "/manifest.foo";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("Not existing document: exception should be thrown");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*document not found.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getCause().getMessage());
            }
        }
    }

    //FromDB_NotExistingCollection

    @Test
    public void fromDB_NotExistingCollection() {
        System.out.println("FromDB_NotExistingCollection");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/" + TESTCASENAME + "/foo/bar.xml";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("Not existing collection: exception should be thrown");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Collection /db/.* not found.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    //FromDB_NotExistingUser

    @Test
    public void fromDB_NotExistingUser() {
        System.out.println("fromDB_NotExistingUser");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://foo:bar@localhost:8080/exist/xmlrpc/db/" + TESTCASENAME + "/bar.xml";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("Not existing user: exception should be thrown");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*User foo unknown.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    //FromDB_NotAuthorized

    @Test
    public void fromDB_NotAuthorized() {
        System.out.println("fromDB_NotAuthorized");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://guest:guest@localhost:8080/exist/xmlrpc/db/system/users.xml";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("Not authorized user: exception should be thrown");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Insufficient privileges to read resource.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }
}
