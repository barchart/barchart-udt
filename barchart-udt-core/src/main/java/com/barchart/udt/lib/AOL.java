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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AOL {

	private final static Logger log = LoggerFactory.getLogger(AOL.class);

	final String arch;
	final String os;
	final String linker;

	/** The Constant OS_NAME. */
	public final static String OS_NAME = System.getProperty("os.name");

	/** The Constant OS_ARCH. */
	public final static String OS_ARCH = System.getProperty("os.arch");

	static{
		log.info("OS_NAME={}", OS_NAME);
		log.info("OS_ARCH={}", OS_ARCH);
	}

	AOL(final String line) {

		String[] entry = line.split("=");
		String[] terms = entry[0].split("\\.");

		arch = terms[0];
		os = terms[1];
		linker = terms[2];

	}

	String propertyName() {
		return arch + "." + os + "." + linker;
	}

	String resourceName() {
		return arch + "-" + os + "-" + linker;
	}

	boolean isMatchJVM() {

		// log.info("aol;    arch={} os={}", arch, os);
		// log.info("jvm;    arch={} os={}", OS_ARCH, OS_NAME);
		// log.info("filter; arch={} os={}", filterArch(), filterName());

		if (arch.equals(filterArch()) && os.equals(filterName())) {
			return true;
		}

		return false;
	}

	static String filterName() {
		if (OS_NAME.contains("Mac OS X")) {
			return "MacOSX";
		}
		if (OS_NAME.contains("Windows")) {
			return "Windows";
		}
		return OS_NAME;
	}

	static String filterArch() {
		return OS_ARCH;
	}

};