/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_23-19-15'
 *
 * Copyright (C) 2009, Barchart, Inc. (http://www.barchart.com/)
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
package com.barchart.udt.util;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelperUtils {

	private static Logger log = LoggerFactory
			.getLogger(HelperUtils.class);

	public static String getProperty(String name) {

		String value = System.getProperty(name);

		if (value == null) {
			log.error("property '{}' not defined; terminating", name);
			System.exit(1);
		}

		return value;

	}

	private static final AtomicInteger portCounter = new AtomicInteger(12345);

	public static InetSocketAddress getLocalSocketAddress() {

		final InetSocketAddress address = new InetSocketAddress("localhost",
				portCounter.getAndIncrement());

		log.info("\n\t### allocated address={} ###", address);

		return address;

	}

	static int[] randomIntArray(int length, int range) {
		int[] array = new int[length];
		Random generator = new Random();
		// for each item in the list
		for (int i = 0; i < array.length; i++) {
			// create a new random number and populate the
			// current location in the list with it
			array[i] = generator.nextInt(range);
		}
		return array;
	}

	public static void logClassPath() {

		String classPath = System.getProperty("java.class.path");

		String[] entries = classPath.split(File.pathSeparator);

		StringBuilder text = new StringBuilder(1024);

		for (String item : entries) {
			text.append("\n\t");
			text.append(item);
		}

		log.info("{}", text);

	}

	public static void logLibraryPath() {

		String classPath = System.getProperty("java.library.path");

		String[] entries = classPath.split(File.pathSeparator);

		StringBuilder text = new StringBuilder(1024);

		for (String item : entries) {
			text.append("\n\t");
			text.append(item);
		}

		log.info("{}", text);

	}

	public static void logOsArch() {

		StringBuilder text = new StringBuilder(1024);

		text.append("\n\t");
		text.append(System.getProperty("os.name"));

		text.append("\n\t");
		text.append(System.getProperty("os.arch"));

		log.info("{}", text);

	}

	public static String getRandomString(){
		return UUID.randomUUID().toString();
	}

}
