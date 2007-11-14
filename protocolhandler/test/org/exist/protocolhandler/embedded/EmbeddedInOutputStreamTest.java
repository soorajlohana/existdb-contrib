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
 * $Id$
 */
package org.exist.protocolhandler.embedded;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import org.exist.protocolhandler.GenericTestcase;
import org.exist.protocolhandler.xmldb.XmldbURL;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  Testsuite for performing tests on EmbeddedInputstream / EmbeddedOutputStream
 *
 * @author Dannes Wessels
 */
public class EmbeddedInOutputStreamTest extends GenericTestcase {

    private static Logger LOG = Logger.getLogger(GenericTestcase.class);

    private void sendDocument(XmldbURL uri, InputStream is) throws IOException {

        EmbeddedOutputStream eos = new EmbeddedOutputStream(uri);

        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            eos.write(buf, 0, len);
        }

        eos.close();
    }

    private void getDocument(XmldbURL uri, OutputStream os) throws IOException {

        EmbeddedInputStream eis = new EmbeddedInputStream(uri);

        // Transfer bytes from in to out
        byte[] buf = new byte[4096];
        int len;
        while ((len = eis.read(buf)) > 0) {
            os.write(buf, 0, len);
        }

        os.close();
        eis.close();

    }

    //toDB

    @Test
    public void toDB() {
        System.out.println("toDB");
        try {
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist:///db/build_embedded_toDB.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }

    //fromDB

    @Test
    public void fromDB() {
        System.out.println("fromDB");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist:///db/build_embedded_toDB.xml";

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

    //toDB_NotExistingCollection

    @Test
    public void toDB_NotExistingCollection() {
        System.out.println("toDB_NotExistingCollection");
        try {
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist:///db/foobar/build_embedded_toDB_NotExistingCollection.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);

            fail("Exception expected, not existing collection.");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Resource /db/foobar is not a collection.")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    //fromDB_NotExistingCollection

    @Test
    public void fromDB_NotExistingCollection() {
        System.out.println("fromDB_NotExistingCollection");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist:///db/foobar/build_embedded_toDB_NotExistingCollection.xml";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);

            fail("Exception expected, not existing collection.");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Resource .* not found.")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    //toDB_NotExistingUser

    @Test
    public void toDB_NotExistingUser() {
        System.out.println("toDB_NotExistingUser");
        try {
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist://foo:bar@/db/build_embedded_toDB_toDB_NotExistingUser.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);

            fail("Exception expected, not existing user.");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Unauthorized user foo.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getCause().getMessage());
            }
        }
    }

    //fromDB_NotExistingUser

    @Test
    public void fromDB_NotExistingUser() {
        System.out.println("fromDB_NotExistingUser");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist://foo:bar@/db/build_embedded_toDB_toDB_NotExistingUser.xml";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            fail("Exception expected, not existing collection.");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Unauthorized user foo")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    //toDB_NotAuthorized

    @Test
    public void toDB_NotAuthorized() {
        System.out.println("toDB_NotAuthorized");
        try {
            FileInputStream fis = new FileInputStream("build.xml");
            String uri = "xmldb:exist:///db/system/users.xml";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);
            fail("Exception expected, not authorized user.");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Document exists and update is not allowed for the collection.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    //fromDB_NotAuthorized

    @Test
    public void fromDB_NotAuthorized() {
        System.out.println("fromDB_NotAuthorized");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist:///db/system/users.xml";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);

            fail("Exception expected, not existing collection.");

        } catch (Exception ex) {
            if (!ex.getCause().getMessage().matches(".*Permission denied to read collection '/db/system'")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    /*
     * Additional tests on binary resources
     */
    //ToDB

    @Test
    public void toDB_binaryDoc() {
        System.out.println("toDB_binaryDoc");
        try {
            FileInputStream fis = new FileInputStream("manifest.mf");
            String uri = "xmldb:exist:///db/manifest.txt";
            XmldbURL xmldbUri = new XmldbURL(uri);
            sendDocument(xmldbUri, fis);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }

    //FromDB

    @Test
    public void fromDB_binaryDoc() {
        System.out.println("fromDB_binaryDoc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String uri = "xmldb:exist:///db/manifest.txt";

        try {
            XmldbURL xmldbUri = new XmldbURL(uri);
            getDocument(xmldbUri, baos);
            assertTrue(baos.size() > 0);
            assertEquals(85, baos.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());
        }
    }
}
