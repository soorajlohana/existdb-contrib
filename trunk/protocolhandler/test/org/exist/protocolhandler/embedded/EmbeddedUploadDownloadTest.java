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

import java.io.BufferedInputStream;
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
 *
 * @author Dannes Wessels
 */
public class EmbeddedUploadDownloadTest extends GenericTestcase {

    private static Logger LOG = Logger.getLogger(EmbeddedURLsTest.class);

    @Test
    public void toDB() {
        System.out.println("toDB");

        try {
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/build_testEmbeddedUploadToDB.xml");
            InputStream is = new BufferedInputStream(new FileInputStream("build.xml"));
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());

        }
    }

    @Test
    public void fromDB() {
        System.out.println("fromDB");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/build_testEmbeddedUploadToDB.xml");

            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, baos);

            baos.close();

            assertTrue(baos.size() > 0);

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());

        }
    }

    //ToDB_NotExistingCollection

    @Test
    public void toDB_NotExistingCollection() {
        System.out.println("toDB_NotExistingCollection");

        try {
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/foobar/toDB_NotExistingCollection.xml");
            InputStream is = new BufferedInputStream(new FileInputStream("build.xml"));
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();

            fail("Not existing collection: Exception expected");

        } catch (Exception ex) {
            if (!ex.getMessage().matches(".*Resource /db/foobar is not a collection.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    //FromDB_NotExistingCollection

    @Test
    public void fromDB_NotExistingCollection() {
        System.out.println("fromDB_NotExistingCollection");

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {

            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/foobar/toDB_NotExistingCollection.xml");

            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);

            os.close();

            fail("Not existing collection: Exception expected");

        } catch (Exception ex) {
            if (!ex.getMessage().matches(".*Resource .* not found.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }
    }

    //ToDB_NotExistingUser

    @Test
    public void toDB_NotExistingUser() {
        System.out.println("toDB_NotExistingUser");

        try {
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist://foo:bar@/db/toDB_NotExistingUser.xml");
            InputStream is = new BufferedInputStream(new FileInputStream("build.xml"));
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();

            fail("Not existing user: Exception expected");

        } catch (Exception ex) {
            if (!ex.getMessage().matches(".*Unauthorized .* foo.*")) {
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

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist://foo:bar@/db/foobar/fromDB_NotExistingUser.xml");

            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);

            os.close();

            fail("Not existing user: Exception expected");

        } catch (Exception ex) {
            if (!ex.getMessage().matches(".*Unauthorized .* foo.*")) {
                ex.printStackTrace();
                LOG.error(ex);
                fail(ex.getMessage());
            }
        }

    }

    //ToDB_NotAuthorized

    @Test
    public void toDB_NotAuthorized() {
        System.out.println("toDB_NotAuthorized");

        try {
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/system/toDB_NotAuthorized.xml");
            InputStream is = new BufferedInputStream(new FileInputStream("build.xml"));
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();

            fail("User not authorized: Exception expected");

        } catch (Exception ex) {

            if (!ex.getCause().getMessage().matches(".*User .* not allowed to write to collection.*")) {
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

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {

            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/system/users.xml");

            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, os);

            os.close();

            fail("User not authorized: Exception expected");

        } catch (Exception ex) {
            if (!ex.getMessage().matches(".*Permission denied to read collection .*")) {
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

    /*
     * Additional tests binary documents
     */
    @Test
    public void toDB_binaryDoc() {
        System.out.println("toDB_binaryDoc");

        try {
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/manifest.txt");
            InputStream is = new BufferedInputStream(new FileInputStream("manifest.mf"));
            EmbeddedUpload instance = new EmbeddedUpload();
            instance.stream(xmldbURL, is);
            is.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());

        }
    }

    @Test
    public void fromDB_binaryDoc() {
        System.out.println("fromDB_binaryDoc");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            XmldbURL xmldbURL = new XmldbURL("xmldb:exist:///db/manifest.txt");

            EmbeddedDownload instance = new EmbeddedDownload();
            instance.stream(xmldbURL, baos);

            baos.close();

            assertTrue(baos.size() > 0);
            assertEquals(85, baos.size());

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            fail(ex.getMessage());

        }
    }
}
