/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
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
