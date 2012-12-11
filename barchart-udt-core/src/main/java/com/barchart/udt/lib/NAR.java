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
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NAR {

	/** The log. */
	private final static Logger log = LoggerFactory.getLogger(NAR.class);

	static String readFileAsString(String filePath) throws Exception {

		StringBuffer fileData = new StringBuffer(1024 * 4);

		InputStream stream = NAR.class.getResourceAsStream(filePath);

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));

		char[] buf = new char[1024];

		int numRead = 0;

		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
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

		SupportedLinker(String name) {
			this.name = name;
		}

		static SupportedLinker fromName(String name) {
			for (SupportedLinker linker : values()) {
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

	public static void main(String... args) throws Exception {

		log.info("started");

		String filePath = NAR_AOL;

		String narAol = readFileAsString(filePath);

		log.info("\n{}", narAol);

		String[] lines = narAol.split("\n");

		log.info("lines.length={}", lines.length);

		Set<String> set = new HashSet<String>();

		for (String line : lines) {

			line = line.trim();

			if (line.startsWith("#") || line.length() < 1) {
				continue;
			}

			AOL aol = new AOL(line);

			if (aol.linker.contains("linker")) {
				continue;
			}

			if (!SupportedLinker.fromName(aol.linker).isKnown()) {
				continue;
			}

			String name = aol.resourceName();

			set.add(name);

		}

		for (String line : set.toArray(new String[] {})) {

			String find = AOL.filterArch() + "-" + AOL.filterName();

			if (line.contains(find)) {
				log.info("{}", line);
			}

		}

	}

}
