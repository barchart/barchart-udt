/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.util;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.nio.TestSelectorProviderUDT;

public class TestHelp {

	private static Logger log = LoggerFactory.getLogger(TestHelp.class);

	public static String property(final String name) {

		final String value = System.getProperty(name);

		if (value == null) {
			log.error("property '{}' not defined; terminating", name);
			System.exit(1);
		}

		return value;

	}

	/** allocate available local address / port */
	public static InetSocketAddress localSocketAddress() throws Exception {

		final int port = findLocalPort();

		final InetSocketAddress address = //
		new InetSocketAddress("localhost", port);

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

	/**
	 */
	public static int findLocalPort() throws Exception {

		ServerSocket socket = null;

		try {

			/**
			 * "A port of <code>0</code> creates a socket on any free port."
			 */
			socket = new ServerSocket(0);

			return socket.getLocalPort();

		} catch (final Exception e) {

		} finally {

			if (socket != null) {
				try {
					socket.close();
				} catch (final IOException e) {
				}
			}

		}

		throw new Exception("can not allocate port");

	}

	public static Set<Integer> socketSet(final IntBuffer buffer) {

		final Set<Integer> set = new HashSet<Integer>();

		while (buffer.hasRemaining()) {
			set.add(buffer.get());
		}

		return set;

	}

	public static void logSet(final Set<?> set) {
		for (final Object item : set) {
			TestSelectorProviderUDT.log.info("-- {}", item);
		}
	}

}
