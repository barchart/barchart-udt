/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
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
 * class path resource extractor and system loader
 */
public class ResourceManagerUDT {

	protected final static Logger log = LoggerFactory
			.getLogger(ResourceManagerUDT.class);

	protected static boolean isSameResource(final URLConnection conONE,
			final URLConnection conTWO) throws Exception {

		final long timeONE = conONE.getLastModified();
		final long sizeONE = conONE.getContentLength();

		final long timeTWO = conTWO.getLastModified();
		final long sizeTWO = conTWO.getContentLength();

		return sizeONE == sizeTWO && timeONE == timeTWO;

	}

	protected static URLConnection fileConnection(final File file)
			throws Exception {

		final URL url = file.toURI().toURL();

		final URLConnection connection = url.openConnection();

		return connection;

	}

	protected final static int EOF = -1;

	/** will use time stamp of jar file */
	protected static long timeStamp(final URLConnection connIN) {
		return connIN.getLastModified();
	}

	/**
	 * extract resource from class path into local file system
	 */
	protected static void extractResource(final String sourcePath,
			final String targetPath) throws Exception {

		final URL sourceUrl = ResourceManagerUDT.class.getResource(sourcePath);

		if (sourceUrl == null) {
			log.warn("classpath resource not found: {}", sourcePath);
			throw new IllegalArgumentException("resource not found");
		}

		log.debug("sourceURL={} ", sourceUrl);

		final URLConnection sourceConn = sourceUrl.openConnection();

		if (sourceConn == null) {
			log.warn("classpath resource connection not available: {}",
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
			log.debug("already extracted; sourcePath={} targetPath={}",
					sourcePath, targetPath);
			return;
		} else {
			log.debug("make new extraction destination for targetPath={}",
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

		/** synchronize target time stamp with source to avoid repeated copy */
		targetFile.setLastModified(sourceTime);

		log.debug("extracted OK; sourcePath={} targetPath={}", sourcePath,
				targetPath);

	}

	protected static void ensureTargetFolder(final File folder)
			throws Exception {
		if (folder.exists()) {
			if (folder.isDirectory()) {
				log.debug("found folder={}", folder);
			} else {
				log.error("not a directory; folder={}", folder);
				throw new IllegalArgumentException(
						"extract destination exists, but as a file and not a folder");
			}
		} else {
			final boolean isSuccess = folder.mkdirs();
			if (isSuccess) {
				log.debug("mkdirs : folder={}", folder);
			} else {
				log.error("mkdirs failure; folder={}", folder);
				throw new IllegalStateException(
						"failed to make extract destination folder");
			}
		}
	}

	protected static void ensureTargetFolder(final String targetFolder)
			throws Exception {

		final File folder = new File(targetFolder).getAbsoluteFile();

		ensureTargetFolder(folder);

	}

	/**
	 * load library using absolute file path
	 */
	protected static void systemLoad(final String targetPath) throws Exception {

		final File loadFile = new File(targetPath);

		final String loadPath = loadFile.getAbsolutePath();

		System.load(loadPath);

	}

	/**
	 * load library using absolute file path
	 */
	protected static void systemLoad(final String sourcePath,
			final String targetPath) throws Exception {

		extractResource(sourcePath, targetPath);

		systemLoad(targetPath);

	}

}
