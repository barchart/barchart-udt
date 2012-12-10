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
