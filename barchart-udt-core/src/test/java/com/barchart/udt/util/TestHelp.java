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
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHelp {

	private static Logger log = LoggerFactory.getLogger(TestHelp.class);

	public static String getProperty(final String name) {

		final String value = System.getProperty(name);

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

	static int[] randomIntArray(final int length, final int range) {
		final int[] array = new int[length];
		final Random generator = new Random();
		// for each item in the list
		for (int i = 0; i < array.length; i++) {
			// create a new random number and populate the
			// current location in the list with it
			array[i] = generator.nextInt(range);
		}
		return array;
	}

	public static void logClassPath() {

		final String classPath = System.getProperty("java.class.path");

		final String[] entries = classPath.split(File.pathSeparator);

		final StringBuilder text = new StringBuilder(1024);

		for (final String item : entries) {
			text.append("\n\t");
			text.append(item);
		}

		log.info("\n\t[java.class.path]{}", text);

	}

	public static void logLibraryPath() {

		final String classPath = System.getProperty("java.library.path");

		final String[] entries = classPath.split(File.pathSeparator);

		final StringBuilder text = new StringBuilder(1024);

		for (final String item : entries) {
			text.append("\n\t");
			text.append(item);
		}

		log.info("\n\t[java.library.path]{}", text);

	}

	public static void logOsArch() {

		final StringBuilder text = new StringBuilder(1024);

		text.append("\n\t");
		text.append(System.getProperty("os.name"));

		text.append("\n\t");
		text.append(System.getProperty("os.arch"));

		log.info("\n\t[os/arch]{}", text);

	}

	public static String randomString() {
		return "" + System.currentTimeMillis();
	}

	public static String randomSuffix(final String name) {
		return name + "-" + System.currentTimeMillis();
	}

}
