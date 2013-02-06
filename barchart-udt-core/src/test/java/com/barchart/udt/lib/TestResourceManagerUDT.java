/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

import util.TestAny;
import util.UnitHelp;

public class TestResourceManagerUDT extends TestAny {

	@Test
	public void testFileConnection() throws Exception {

		final String curDir = System.getProperty("user.dir");
		log.info("curDir: {}", curDir);

		final String path = "src/test/resources/test-connection.txt";

		final File file = new File(path);
		log.info("exists : {}", file.exists());

		log.info("getAbsolutePath : {}", file.getAbsolutePath());

		final URLConnection conn = ResourceManagerUDT.fileConnection(file);

		assertTrue(conn.getLastModified() > 0);
		log.info("getLastModified : {}", conn.getLastModified());

		assertTrue(conn.getContentLength() > 0);
		log.info("getContentLength : {}", conn.getContentLength());

	}

	@Test
	public void testExtractResource() throws Exception {

		log.info("java.class.path = {}", System.getProperty("java.class.path"));

		// path inside jar, relative to the root of java.class.path
		final String sourcePath = "/lib/bin/test-resource.txt";

		log.info("user.dir = {}", System.getProperty("user.dir"));

		final String targetFolder = UnitHelp
				.randomSuffix("./target/testExtractResource");

		ResourceManagerUDT.ensureTargetFolder(targetFolder);

		// path outside of jar, in file system, relative to user.dir
		final String targetPath = targetFolder + "/test-extracted.txt";

		final File targetFile = new File(targetPath).getAbsoluteFile();

		targetFile.delete();

		ResourceManagerUDT.extractResource(sourcePath, targetPath);

		assertTrue(targetFile.exists());

		final URL sourceURL = TestResourceManagerUDT.class
				.getResource(sourcePath);

		final URL targetURL = targetFile.toURI().toURL();

		final URLConnection sourceCONN = sourceURL.openConnection();

		final URLConnection targetCONN = targetURL.openConnection();

		assertEquals(//
				sourceCONN.getContentLength(), targetCONN.getContentLength());

		assertEquals(//
				sourceCONN.getLastModified(), targetCONN.getLastModified());

		targetFile.delete();

		// TODO does not work on windows
		// assertEquals(0, targetCONN.getContentLength());
		// assertEquals(0, targetCONN.getLastModified());

	}

}
