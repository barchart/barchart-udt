/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * maven-nar-plugin properties
 */
class NAR {

	/** The log. */
	private final static Logger log = LoggerFactory.getLogger(NAR.class);

	static String readFileAsString(final String filePath) throws Exception {

		final StringBuffer fileData = new StringBuffer(1024 * 4);

		final InputStream stream = NAR.class.getResourceAsStream(filePath);

		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				stream));

		char[] buf = new char[1024];

		int numRead = 0;

		while ((numRead = reader.read(buf)) != -1) {
			final String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}

		reader.close();

		return fileData.toString();

	}

	enum SupportedLinker {

		UNKNOWN(""), //

		GPP("g++"), //

		;

		final String name;

		SupportedLinker(final String name) {
			this.name = name;
		}

		static SupportedLinker fromName(final String name) {
			for (final SupportedLinker linker : values()) {
				if (linker.name.equals(name)) {
					return linker;
				}
			}
			return UNKNOWN;
		}

		boolean isKnown() {
			return this != UNKNOWN;
		}

	}

	static final String NAR_AOL = "/nar-aol.properties";

}
