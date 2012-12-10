/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HelperUDT {

	private static final Logger log = LoggerFactory.getLogger(HelperUDT.class);

	static long md5sum(String text) {

		byte[] defaultBytes = text.getBytes();

		try {

			MessageDigest algorithm = MessageDigest.getInstance("MD5");

			algorithm.reset();

			algorithm.update(defaultBytes);

			byte digest[] = algorithm.digest();

			ByteBuffer buffer = ByteBuffer.wrap(digest);

			return buffer.getLong();

		} catch (NoSuchAlgorithmException e) {

			log.error("md5 failed", e);

			return 0;

		}

	}

}
