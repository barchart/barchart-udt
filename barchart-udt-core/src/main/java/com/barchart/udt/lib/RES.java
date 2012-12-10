/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
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

	private static long timeStamp(URLConnection connIN) {
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

		final File targetFile = new File(targetPath);
		log.debug("targetFile={} ", targetFile.getAbsolutePath());

		final File targetFolder = targetFile.getParentFile();
		log.debug("targetFolder={} ", targetFolder.getAbsolutePath());

		makeTargetFolder(targetFolder);

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

	public static void makeTargetFolder(final File folder) throws Exception {
		if (folder.exists()) {
			if (folder.isDirectory()) {
				log.warn("found folder={}", folder);
			} else {
				log.warn("not a directory; folder={}", folder);
				throw new IllegalArgumentException(
						"extract destination exists, but as a file and not a folder");
			}
		} else {
			final boolean isSuccess = folder.mkdirs();
			if (isSuccess) {
				log.info("made folder={}", folder);
			} else {
				throw new IllegalStateException(
						"failed to make extract destination  folder");
			}
		}
	}

	public static void makeTargetFolder(final String targetFolder)
			throws Exception {
		final File folder = new File(targetFolder);
		makeTargetFolder(folder);
	}

	public static void systemLoad(final String sourcePath,
			final String targetPath) throws Exception {
		extractResource(sourcePath, targetPath);
		final String loadPath = (new File(targetPath)).getAbsolutePath();
		System.load(loadPath);
	}

}
