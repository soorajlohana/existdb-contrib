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
package org.exist.protocolhandler.protocols.xmldb;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  jUnit tests for GETting and PUTting data to eXist.
 *
 * @author Dannes Wessels
 */
public class ConnectionTest {

    private static Logger LOG = Logger.getLogger(ConnectionTest.class);

    @BeforeClass
    public static void start() throws Exception {

        PropertyConfigurator.configure("log4j.conf");
        System.setProperty("java.protocol.handler.pkgs", "org.exist.protocolhandler.protocols");

    }

    @AfterClass
    public static void tearDown() throws Exception {
    // empty yet
    }

    /**
     * Test of writing data to eXist server. Data will be reused by
     * subsequent tests.
     */
    @Test
    public void putDocumentToExistingCollection() {
        System.out.println("putDocumentToExistingCollection");
        try {
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/build.xml");

            OutputStream os = url.openConnection().getOutputStream();
            FileInputStream is = new FileInputStream("build.xml");
            copyDocument(is, os);
            is.close();
            os.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());

        }
    }

    @Test
    public void putDocumentToExistingNotExistingCollection() {
        System.out.println("putDocumentToExistingNotExistingCollection");
        try {
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/foobar/build.xml");

            OutputStream os = url.openConnection().getOutputStream();
            FileInputStream is = new FileInputStream("build.xml");
            copyDocument(is, os);
            is.close();
            os.close();

        } catch (Exception ex) {

            if (!ex.getCause().getMessage().matches(".*Collection /db/foobar not found.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    /**
     * Test reading an existing document from eXist.
     */
    @Test
    public void getExistingDocument() {
        System.out.println("getExistingDocument");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/build.xml");

            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test reading an non-existing document from eXist.
     */
    @Test
    public void getNonExistingDocument() {
        System.out.println("getNonExistingDocument");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            URL url = new URL("xmldb:exist://guest:guest@localhost:8080" + "/exist/xmlrpc/db/foobar/build.xml");

            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();

            fail("Document should not exist");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Collection /db/foobar not found!.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    /**
     * Test reading an existing document from eXist as a non-existing user.
     */
    @Test
    public void getDocumentNonExistingUser() {
        System.out.println("getDocumentNonExistingUser");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            URL url = new URL("xmldb:exist://foo:bar@localhost:8080" + "/exist/xmlrpc/db/build.xml");

            InputStream is = url.openStream();
            copyDocument(is, baos);
            is.close();

            fail("user should not exist");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*User foo unknown.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    // Transfer bytes from inputstream to outputstream

    private void copyDocument(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
    }
}
