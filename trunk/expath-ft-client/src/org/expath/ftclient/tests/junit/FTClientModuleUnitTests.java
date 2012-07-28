package org.expath.ftclient.tests.junit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.expath.ftclient.Connect;
import org.expath.ftclient.DeleteResource;
import org.expath.ftclient.Disconnect;
import org.expath.ftclient.GetResourceMetadata;
import org.expath.ftclient.InputStream2Base64String;
import org.expath.ftclient.ListResources;
import org.expath.ftclient.RetrieveResource;
import org.expath.ftclient.StoreResource;
import org.expath.ftclient.FTP.FTP;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.jcraft.jsch.Session;

public class FTClientModuleUnitTests {
	@Rule
    public TestName name= new TestName();

	public static FTPClient initializeFtpConnection(String URIstring) throws URISyntaxException, Exception {
		FTPClient remoteConnection = Connect.connect(new URI(URIstring));
		return remoteConnection;
	}

	public static Session initializeSftpConnection(String URIstring, String clientPrivateKey) throws URISyntaxException,
			Exception {
		Session remoteConnection = Connect.connect(new URI(URIstring), clientPrivateKey);
		return remoteConnection;
	}

	@Test
	public void test01() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights";
		StreamResult resources = ListResources.listResources(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		String resourcesString = resources.getWriter().toString();
		System.out.println(resourcesString);
		Assert.assertTrue(resourcesString.contains("image-with-rights.gif"));
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test02() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/image-with-rights.gif";
		StreamResult resource = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		String resourceString = resource.getWriter().toString();
		System.out.println(resourceString);
		String sampleResourceAsString = "<?xml version=\"1.0\" ?><ft-client:resource xmlns:ft-client=\"http://expath.org/ns/ft-client\" name=\"image-with-rights.gif\" type=\"file\" absolute-path=\"/dir-with-rights/image-with-rights.gif\" last-modified=\"2012-05-14T15:28:00+03:00\" size=\"1010\" human-readable-size=\"1010 bytes\" user=\"1001\" user-group=\"1001\" permissions=\"-rw-rw-rw-\">"
				+ InputStream2Base64String.convert((InputStream) getClass()
						.getResourceAsStream("data/image-with-rights.gif")) + "</ft-client:resource>";
		Assert.assertTrue(sampleResourceAsString.equals(resourceString));
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test03() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/tmp/image-with-rights" + System.currentTimeMillis() + ".gif";
		InputStream resourceInputStream = getClass().getResourceAsStream("data/image-with-rights.gif");
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, resourceInputStream);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(stored);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test04() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights";
		StreamResult resources = ListResources.listResources(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		String resourcesString = resources.getWriter().toString();
		System.out.println(resourcesString);
		Assert.assertTrue(resourcesString.contains("image-with-rights.gif"));
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test05() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/image-with-rights.gif";
		StreamResult resource = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		String resourceString = resource.getWriter().toString();
		String sampleResourceAsString = "<?xml version=\"1.0\" ?><ft-client:resource xmlns:ft-client=\"http://expath.org/ns/ft-client\" name=\"image-with-rights.gif\" type=\"file\" last-modified=\"2012-05-14T18:28:14+03:00\" size=\"1010\" human-readable-size=\"1010 bytes\" user=\"ftp-user\" user-group=\"ftp-user\" permissions=\"-rw-rw-rw-\">"
				+ InputStream2Base64String.convert((InputStream) getClass()
						.getResourceAsStream("data/image-with-rights.gif")) + "</ft-client:resource>";
		Assert.assertTrue(sampleResourceAsString.equals(resourceString));
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test06() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/tmp/image-with-rights" + System.currentTimeMillis()
				+ ".gif";
		InputStream resourceInputStream = getClass().getResourceAsStream("data/image-with-rights.gif");
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, resourceInputStream);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(stored);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test07() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/tmp/test" + System.currentTimeMillis() + ".txt";
		InputStream resourceInputStream = getClass().getResourceAsStream("data/test.txt");
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, resourceInputStream);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(stored);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test08() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/test.txt";
		StreamResult resource = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		String resourceString = resource.getWriter().toString();
		String sampleResourceAsString = "<?xml version=\"1.0\" ?><ft-client:resource xmlns:ft-client=\"http://expath.org/ns/ft-client\" name=\"test.txt\" type=\"file\" absolute-path=\"/dir-with-rights/test.txt\" last-modified=\"2012-05-14T15:28:00+03:00\" size=\"64\" human-readable-size=\"64 bytes\" user=\"1001\" user-group=\"1001\" permissions=\"-rw-rw-rw-\">"
				+ InputStream2Base64String.convert((InputStream) getClass().getResourceAsStream("data/test.txt"))
				+ "</ft-client:resource>";
		Assert.assertTrue(sampleResourceAsString.equals(resourceString));
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test09() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/test.txt";
		StreamResult resource = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		String resourceString = resource.getWriter().toString();
		System.out.println(resourceString);
		String sampleResourceAsString = "<?xml version=\"1.0\" ?><ft-client:resource xmlns:ft-client=\"http://expath.org/ns/ft-client\" name=\"test.txt\" type=\"file\" last-modified=\"2012-05-14T18:28:14+03:00\" size=\"64\" human-readable-size=\"64 bytes\" user=\"ftp-user\" user-group=\"ftp-user\" permissions=\"-rw-rw-rw-\">"
				+ InputStream2Base64String.convert((InputStream) getClass().getResourceAsStream("data/test.txt"))
				+ "</ft-client:resource>";
		System.out.println(sampleResourceAsString);
		Assert.assertTrue(sampleResourceAsString.equals(resourceString));
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test10() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/tmp/test" + System.currentTimeMillis() + ".txt";
		InputStream resourceInputStream = getClass().getResourceAsStream("data/test.txt");
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, resourceInputStream);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(stored);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test11() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Assert.assertTrue(true);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test12() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/image-no-rights.gif";
		try {
			RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals(
					"err:FTC004: The user has no rights to access the remote resource."));
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test13() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/non-existing-directory";
		try {
			ListResources.listResources(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage(),
					e.getLocalizedMessage().equals("err:FTC004: The user has no rights to access the remote resource."));
			// TODO: add correct error message: err:FTC003: The remote resource
			// does not exist.
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test14() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/non-existing-directory";
		try {
			ListResources.listResources(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage(),
					e.getLocalizedMessage().equals("err:FTC003: The remote resource does not exist."));
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test15() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/dir-without-rights";
		try {
			ListResources.listResources(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals(
					"err:FTC004: The user has no rights to access the remote resource."));
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test16() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/dir-without-rights";
		try {
			ListResources.listResources(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals(
					"err:FTC004: The user has no rights to access the remote resource."));
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test17() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/non-existing-image.gif";
		try {
			RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals(
					"err:FTC004: The user has no rights to access the remote resource."));
			// TODO: add correct error message: err:FTC003: The remote resource does not exist.
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test18() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/non-existing-image.gif";
		try {
			RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals("err:FTC003: The remote resource does not exist."));
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test19() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1",
				IOUtils.toString(getClass().getResourceAsStream("data/Open-Private-Key"), "UTF-8"));
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/image-no-rights.gif";
		try {
			RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals(
					"err:FTC004: The user has no rights to access the remote resource."));
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test20() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/wrong-path/image-with-rights" + System.currentTimeMillis() + ".gif";
		InputStream resourceInputStream = getClass().getResourceAsStream("data/image-with-rights.gif");
		try {
			StoreResource.storeResource(remoteConnection, remoteResourcePath, resourceInputStream);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals(
					"err:FTC004: The user has no rights to access the remote resource."));
			// TODO: add correct error message: err:FTC003: The remote resource does not exist.
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test21() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/dir-without-rights/image-with-rights"
				+ System.currentTimeMillis() + ".gif";
		InputStream resourceInputStream = getClass().getResourceAsStream("data/image-with-rights.gif");
		try {
			StoreResource.storeResource(remoteConnection, remoteResourcePath, resourceInputStream);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals(
					"err:FTC004: The user has no rights to access the remote resource."));
		} finally {
			Disconnect.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test22() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		Disconnect.disconnect(remoteConnection);
		String remoteResourcePath = "/";
		try {
			ListResources.listResources(remoteConnection, remoteResourcePath);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(e.getLocalizedMessage().equals("err:FTC002: The connection was closed by server."));
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test23() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/tmp/tempFolder" + System.currentTimeMillis() + "/";
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, null);
		Assert.assertTrue(stored);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
		Boolean deleted = DeleteResource.deleteResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(deleted);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test24() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/tmp/test" + System.currentTimeMillis() + ".txt";
		(new File("/home/ftp-user/" + remoteResourcePath)).createNewFile();
		Boolean deleted = DeleteResource.deleteResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(deleted);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test25() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/tmp/tempFolder" + System.currentTimeMillis() + "/";
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, null);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(stored);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test26() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/tmp/tempFolder" + System.currentTimeMillis() + "/";
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, null);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(stored);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test27() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/tmp/tempFolder" + System.currentTimeMillis() + "/";
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, null);
		Assert.assertTrue(stored);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test28() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath = "/dir-with-rights/image-with-rights.gif";
		StreamResult resourceMetadata = GetResourceMetadata.getResourceMetadata(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		String resourceMetadataString = resourceMetadata.getWriter().toString();
		System.out.println(resourceMetadataString);
		String sampleResourceMetadataAsString = "<?xml version=\"1.0\" ?><ft-client:resource xmlns:ft-client=\"http://expath.org/ns/ft-client\" name=\"image-with-rights.gif\" type=\"file\" absolute-path=\"/dir-with-rights/image-with-rights.gif\" last-modified=\"2012-05-14T15:28:00+03:00\" size=\"1010\" human-readable-size=\"1010 bytes\" user=\"1001\" user-group=\"1001\" permissions=\"-rw-rw-rw-\"></ft-client:resource>";
		Assert.assertTrue(resourceMetadataString
				.equals(sampleResourceMetadataAsString));
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void test29() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		String remoteResourcePath1 = "/dir-with-rights";
		StreamResult resources = ListResources.listResources(remoteConnection, remoteResourcePath1);
		String resourcesString = resources.getWriter().toString();
		System.out.println(resourcesString);
		Assert.assertTrue(resourcesString.contains("image-with-rights.gif"));
		String remoteResourcePath2 = "/dir-with-rights/image-with-rights.gif";
		StreamResult resource1 = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath2);
		StreamResult resource2 = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath2);
		StreamResult resource3 = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath2);
		StreamResult resource4 = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath2);
		StreamResult resource5 = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath2);
		StreamResult resource6 = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath2);
		StreamResult resource7 = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath2);
		Disconnect.disconnect(remoteConnection);
		String resource1String = resource1.getWriter().toString();
		String resource2String = resource2.getWriter().toString();
		String resource3String = resource3.getWriter().toString();
		String resource4String = resource4.getWriter().toString();
		String resource5String = resource5.getWriter().toString();
		String resource6String = resource6.getWriter().toString();
		String resource7String = resource7.getWriter().toString();
		System.out.println(resource1String);
		System.out.println(resource2String);
		System.out.println(resource3String);
		System.out.println(resource4String);
		System.out.println(resource5String);
		System.out.println(resource6String);
		System.out.println(resource7String);
		String sampleResourceAsString = "<?xml version=\"1.0\" ?><ft-client:resource xmlns:ft-client=\"http://expath.org/ns/ft-client\" name=\"image-with-rights.gif\" type=\"file\" absolute-path=\"/dir-with-rights/image-with-rights.gif\" last-modified=\"2012-05-14T15:28:00+03:00\" size=\"1010\" human-readable-size=\"1010 bytes\" user=\"1001\" user-group=\"1001\" permissions=\"-rw-rw-rw-\">"
				+ InputStream2Base64String.convert((InputStream) getClass()
						.getResourceAsStream("data/image-with-rights.gif")) + "</ft-client:resource>";
		Assert.assertTrue(sampleResourceAsString.equals(resource1String));
		Assert.assertTrue(sampleResourceAsString.equals(resource2String));
		Assert.assertTrue(sampleResourceAsString.equals(resource3String));
		Assert.assertTrue(sampleResourceAsString.equals(resource4String));
		Assert.assertTrue(sampleResourceAsString.equals(resource5String));
		Assert.assertTrue(sampleResourceAsString.equals(resource6String));
		Assert.assertTrue(sampleResourceAsString.equals(resource7String));
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void retrieveLargeResource() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp.mozilla.org");
		String remoteResourcePath = "/pub/firefox/releases/9.0b6/linux-i686/en-US/firefox-9.0b6.tar.bz2";
		StreamResult resource = RetrieveResource.retrieveResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		String resourceString = resource.getWriter().toString();
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}
	
	@Test
	public void deleteDirectoryWithSftp() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1", "");
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/tmp/tempFolder" + System.currentTimeMillis() + "/";
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, null);
		Assert.assertTrue(stored);
		System.out.println("Stored resource: " + remoteResourcePath + ".\n");
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
		Boolean deleted = DeleteResource.deleteResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(deleted);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}
	
	@Test
	public void deleteFileWithSftp() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		Session remoteConnection = initializeSftpConnection("sftp://ftp-user:ftp-pass@127.0.0.1", "");
		String remoteResourcePath = "/home/ftp-user/dir-with-rights/tmp/tempFile" + System.currentTimeMillis() + ".txt";
		InputStream resourceInputStream = getClass().getResourceAsStream("data/image-with-rights.gif");
		Boolean stored = StoreResource.storeResource(remoteConnection, remoteResourcePath, resourceInputStream);
		Assert.assertTrue(stored);
		System.out.println("Stored resource: " + remoteResourcePath + ".\n");
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
		Boolean deleted = DeleteResource.deleteResource(remoteConnection, remoteResourcePath);
		Disconnect.disconnect(remoteConnection);
		Assert.assertTrue(deleted);
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void _checkDirectoryWithRightsTest() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		try {
			_checkResourcePath(remoteConnection, "/dir-with-rights/");
			// Assert.assertTrue(false);
			FTP.disconnect(remoteConnection);
		} catch (Exception e) {
			// Assert.assertTrue(e.getLocalizedMessage().equals(
			// "err:FTC003: The remote resource does not exist."));
			FTP.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void _checkDirectoryWithoutRightsTest() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		try {
			_checkResourcePath(remoteConnection, "/dir-with-rights/dir-without-rights/");
			// Assert.assertTrue(false);
			FTP.disconnect(remoteConnection);
		} catch (Exception e) {
			// Assert.assertTrue(e.getLocalizedMessage().equals(
			// "err:FTC003: The remote resource does not exist."));
			FTP.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void _checkDirectoryNonExistingTest() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		try {
			_checkResourcePath(remoteConnection, "/non-existing-dir/");
			// Assert.assertTrue(false);
			FTP.disconnect(remoteConnection);
		} catch (Exception e) {
			// Assert.assertTrue(e.getLocalizedMessage().equals(
			// "err:FTC003: The remote resource does not exist."));
			FTP.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void _checkFileWithRightsTest() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		try {
			_checkResourcePath(remoteConnection, "/dir-with-rights/image-with-rights.gif");
			// Assert.assertTrue(false);
			FTP.disconnect(remoteConnection);
		} catch (Exception e) {
			// Assert.assertTrue(e.getLocalizedMessage().equals(
			// "err:FTC003: The remote resource does not exist."));
			FTP.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void _checkFileWithoutRightsTest() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		try {
			_checkResourcePath(remoteConnection, "/dir-with-rights/image-no-rights.gif");
			// Assert.assertTrue(false);
			FTP.disconnect(remoteConnection);
		} catch (Exception e) {
			// Assert.assertTrue(e.getLocalizedMessage().equals(
			// "err:FTC003: The remote resource does not exist."));
			FTP.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	@Test
	public void _checkFileNonExistingTest() throws URISyntaxException, Exception {
		System.out.println("Starting test '" + name.getMethodName() + "'...");
		long startTime = new Date().getTime();
		FTPClient remoteConnection = initializeFtpConnection("ftp://ftp-user:ftp-pass@127.0.0.1");
		try {
			_checkResourcePath(remoteConnection, "/dir-with-rights/non-existing-image.gif");
			// Assert.assertTrue(false);
			FTP.disconnect(remoteConnection);
		} catch (Exception e) {
			// Assert.assertTrue(e.getLocalizedMessage().equals(
			// "err:FTC003: The remote resource does not exist."));
			FTP.disconnect(remoteConnection);
		}
		System.out.println("Duration of test: " + (new Date().getTime() - startTime) + " ms.\n");
	}

	public static List _checkResourcePath(FTPClient FTPconnection, String remoteResourcePath) throws IOException,
			Exception {
		List FTPconnectionObject = new LinkedList();
		boolean resourceIsDirectory = remoteResourcePath.endsWith("/");
		if (resourceIsDirectory) {
			System.out.println("FTPconnection.listFiles(remoteResourcePath) == null: "
					+ Boolean.toString(FTPconnection.listFiles(remoteResourcePath) == null));
			boolean remoteDirectoryExists = FTPconnection.changeWorkingDirectory(remoteResourcePath);
			FTPconnectionObject.add(remoteDirectoryExists);
			if (!remoteDirectoryExists) {
				System.out.println("\n====================" + remoteResourcePath + "====================");
				// System.out.println("FTPconnection.getReplyString(): "
				// + FTPconnection.getReplyString());
				// System.out
				// .println("FTPconnection.getStatus(remoteResourcePath): "
				// + FTPconnection.getStatus(remoteResourcePath));
				System.out.println("FTPconnection.listFiles(remoteResourcePath): "
						+ FTPconnection.listFiles("/").length);

				if (FTPconnection.getStatus(remoteResourcePath) == null) {
					System.out.println("err:FTC004: The user has no rights to access the remote resource.");
					// throw new Exception(
					// "err:FTC004: The user has no rights to access the remote resource.");
				}
				// throw new Exception(
				// "err:FTC003: The remote resource does not exist.");
				System.out.println("err:FTC003: The remote resource does not exist.");
				// throw new Exception(
				// "err:FTC003: The remote resource does not exist.");
			}

		} else {
			if (FTPconnection.listNames(remoteResourcePath).length == 0) {
				System.out.println("err:FTC003: The remote resource does not exist.");
				// throw new Exception(
				// "err:FTC003: The remote resource does not exist.");
			} else {
				InputStream is = FTPconnection.retrieveFileStream(remoteResourcePath);
				if (is == null) {
					System.out.println("err:FTC004: The user has no rights to access the remote resource.");
					// throw new Exception(
					// "err:FTC004: The user has no rights to access the remote resource.");
				}
				FTPconnectionObject.add(is);
			}
		}

		return FTPconnectionObject;
	}

	public static void main(String[] args) throws Exception {

	}
}
