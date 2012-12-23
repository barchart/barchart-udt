/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package util;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.StatusUDT;

public class UnitHelp {

	private static Logger log = LoggerFactory.getLogger(UnitHelp.class);

	public static String property(final String name) {

		final String value = System.getProperty(name);

		if (value == null) {
			log.error("property '{}' not defined; terminating", name);
			System.exit(1);
		}

		return value;

	}

	/** allocate available local address / port */
	public synchronized static InetSocketAddress localSocketAddress()
			throws Exception {

		final int port = findLocalPort();

		final InetSocketAddress address = //
		new InetSocketAddress("localhost", port);

		log.info("\n\t### allocated address={} ###", address);

		return address;

	}

	public static int[] randomIntArray(final int length, final int range) {

		final int[] array = new int[length];

		final Random generator = new Random(0);

		for (int i = 0; i < array.length; i++) {
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
	public synchronized static int findLocalPort() throws Exception {

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

	public static Set<Integer> socketIndexSet(final IntBuffer buffer) {

		final Set<Integer> set = new HashSet<Integer>();

		while (buffer.hasRemaining()) {
			set.add(buffer.get());
		}

		return set;

	}

	public static void logSet(final Set<?> set) {
		for (final Object item : set) {
			log.info("--- {}", item);
		}
	}

	public static void logBuffer(final String title, final IntBuffer buffer) {
		for (int index = 0; index < buffer.capacity(); index++) {
			final int value = buffer.get(index);
			if (value == 0) {
				continue;
			}
			log.info("{} : {}", title, value);
		}
	}

	public static void socketAwait(final SocketUDT socket,
			final StatusUDT status) throws Exception {
		while (true) {
			if (socket.getStatus() == status) {
				return;
			} else {
				Thread.sleep(50);
			}
		}
	}

	public static boolean socketPresent(final SocketUDT socket,
			final IntBuffer buffer) {
		for (int index = 0; index < buffer.capacity(); index++) {
			if (buffer.get(index) == socket.getSocketId()) {
				return true;
			}
		}
		return false;
	}

	public static void clear(final IntBuffer buffer) {
		for (int index = 0; index < buffer.capacity(); index++) {
			buffer.put(index, 0);
		}
	}

	private static final ConcurrentMap<Integer, SocketUDT> //
	socketMap = new ConcurrentHashMap<Integer, SocketUDT>();

}
