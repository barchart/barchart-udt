/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * resource extract
 */
class RES {

	private final static Logger log = LoggerFactory.getLogger(RES.class);

	public static boolean isSameResource(final URLConnection conONE,
			final URLConnection conTWO) throws Exception {

		final long timeONE = conONE.getLastModified();
		final long sizeONE = conONE.getContentLength();

		final long timeTWO = conTWO.getLastModified();
		final long sizeTWO = conTWO.getContentLength();

		return sizeONE == sizeTWO && timeONE == timeTWO;

	}

	public static URLConnection fileConnection(final File file)
			throws Exception {

		final URL url = file.toURI().toURL();

		final URLConnection connection = url.openConnection();

		return connection;

	}

	private final static int EOF = -1;

	private static long timeStamp(final URLConnection connIN) {
		// will use time stamp of jar file
		return connIN.getLastModified();
	}

	/** from class path into file system */
	public static void extractResource(final String sourcePath,
			final String targetPath) throws Exception {

		// final ClassLoader classLoader = RES.class.getClassLoader();

		// if (classLoader == null) {
		// log.warn("\n\t resource classLoader not available: {}", sourcePath);
		// throw new IllegalArgumentException("resource not found");
		// }

		// final URL sourceUrl = classLoader.getResource(sourcePath);
		final URL sourceUrl = RES.class.getResource(sourcePath);

		if (sourceUrl == null) {
			log.warn("\n\t classpath resource not found: {}", sourcePath);
			throw new IllegalArgumentException("resource not found");
		}

		log.debug("sourceURL={} ", sourceUrl);

		final URLConnection sourceConn = sourceUrl.openConnection();

		if (sourceConn == null) {
			log.warn("\n\t classpath resource connection not available: {}",
					sourcePath);
			throw new IllegalArgumentException("resource not found");
		}

		final File targetFile = new File(targetPath).getAbsoluteFile();
		log.debug("targetFile={} ", targetFile);

		final File targetFolder = targetFile.getParentFile().getAbsoluteFile();
		log.debug("targetFolder={} ", targetFolder);

		ensureTargetFolder(targetFolder);

		final URLConnection targetConn = fileConnection(targetFile);

		if (isSameResource(sourceConn, targetConn)) {
			log.info("\n\t already extracted;" + "\n\t sourcePath={}"
					+ "\n\t targetPath={}", sourcePath, targetPath);
			return;
		} else {
			log.warn("\n\t make new extraction destination for targetPath={}",
					targetPath);
			targetFile.delete();
			targetFile.createNewFile();
		}

		final long sourceTime = timeStamp(sourceConn);

		final InputStream sourceStream = new BufferedInputStream(//
				sourceUrl.openStream());

		final OutputStream targetStream = new BufferedOutputStream(//
				new FileOutputStream(targetFile));

		final byte[] array = new byte[64 * 1024];

		int readCount = 0;

		while ((readCount = sourceStream.read(array)) != EOF) {
			targetStream.write(array, 0, readCount);
		}

		targetStream.flush();

		sourceStream.close();
		targetStream.close();

		// synchronize target time stamp with source to avoid repeated copy
		targetFile.setLastModified(sourceTime);

		log.info("\n\t extracted OK;" + "\n\t sourcePath={}"
				+ "\n\t targetPath={}", sourcePath, targetPath);

	}

	public static void ensureTargetFolder(final File folder) throws Exception {
		if (folder.exists()) {
			if (folder.isDirectory()) {
				log.info("found folder={}", folder);
			} else {
				log.error("not a directory; folder={}", folder);
				throw new IllegalArgumentException(
						"extract destination exists, but as a file and not a folder");
			}
		} else {
			final boolean isSuccess = folder.mkdirs();
			if (isSuccess) {
				log.info("mkdirs : folder={}", folder);
			} else {
				log.error("mkdirs failure; folder={}", folder);
				throw new IllegalStateException(
						"failed to make extract destination folder");
			}
		}
	}

	public static void ensureTargetFolder(final String targetFolder)
			throws Exception {

		final File folder = new File(targetFolder).getAbsoluteFile();

		ensureTargetFolder(folder);

	}

	public static void systemLoad(final String sourcePath,
			final String targetPath) throws Exception {

		extractResource(sourcePath, targetPath);

		final String loadPath = (new File(targetPath)).getAbsolutePath();

		System.load(loadPath);

	}

}
