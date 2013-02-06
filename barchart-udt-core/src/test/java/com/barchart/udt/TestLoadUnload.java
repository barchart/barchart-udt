/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLoadUnload {

	private static class JNIClassLoader extends ClassLoader {

		final Map<String, Class<?>> classes;

		File classLoadRoot;

		JNIClassLoader(final File classLoadRoot) {

			super(JNIClassLoader.class.getClassLoader());

			classes = new HashMap<String, Class<?>>();

			this.classLoadRoot = classLoadRoot;

			TestLoadUnload.isLoaderPresent = true;

		}

		@Override
		public String toString() {
			return JNIClassLoader.class.getName();
		}

		@Override
		public Class<?> loadClass(final String name)
				throws ClassNotFoundException {
			return loadClass(name, false);
		}

		@Override
		protected Package getPackage(final String name) {
			if (name.equals("com.barchart.udt"))
				return this.getClass().getPackage();
			else
				return super.getPackage(name);
		}

		@Override
		protected synchronized Class<?> loadClass(final String name,
				final boolean resolve) throws ClassNotFoundException {

			if (name.startsWith("com.barchart.udt."))
				return findClass(name);
			else
				return super.loadClass(name, resolve);
		}

		@Override
		public Class<?> findClass(final String name)
				throws ClassNotFoundException {

			log.info(String.format("Attempting to find class %s", name));

			if (classes.containsKey(name)) {
				return classes.get(name);
			}

			final String path = name.replace('.', File.separatorChar)
					+ ".class";

			byte[] b = null;

			try {
				b = loadClassData(path);
			} catch (final IOException e) {
				throw new ClassNotFoundException("Class not found at path: "
						+ new File(name).getAbsolutePath(), e);
			}

			final Class<?> c = defineClass(name, b, 0, b.length);
			resolveClass(c);
			classes.put(name, c);

			return c;

		}

		private byte[] loadClassData(final String name) throws IOException {

			final File file = new File(classLoadRoot, name);
			final int size = (int) file.length();
			final byte buff[] = new byte[size];

			final DataInputStream in = //
			new DataInputStream(new FileInputStream(file));

			in.readFully(buff);
			in.close();

			return buff;

		}

		@Override
		protected void finalize() throws Throwable {

			log.info("Finalised {}", this.getClass());

			super.finalize();

			TestLoadUnload.isLoaderPresent = false;

		}

	}

	private static final Logger log = LoggerFactory
			.getLogger(TestLoadUnload.class);

	private static volatile boolean isLoaderPresent;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int createSocketWithClassloader() throws ClassNotFoundException,
			IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			InterruptedException {

		final String currentDir = new File(".").getAbsolutePath();
		log.info(String.format("Current directory: %s", currentDir));

		// HACK - ideally we should update JNIClassLoader to
		// use the whole java classpath
		final File classPath = new File("target/classes");

		JNIClassLoader loader = new JNIClassLoader(classPath);

		Class<Enum> typeClass = (Class<Enum>) loader
				.findClass("com.barchart.udt.TypeUDT");

		Class<?> socketClass = loader.findClass("com.barchart.udt.SocketUDT");

		Object socketInstance = socketClass.getDeclaredConstructor(typeClass)
				.newInstance(Enum.valueOf(typeClass, "DATAGRAM"));

		socketClass.getMethod("cleanup").invoke(socketInstance);

		final int classHashCode = socketClass.hashCode();

		log.info(String.format("socketClass hashCode : %s", classHashCode));

		typeClass = null;
		socketClass = null;
		socketInstance = null;
		loader = null;

		/** waiting loader to finalize */
		while (isLoaderPresent) {
			System.gc();
			Thread.sleep(100);
		}

		/** waiting jvm to unlink class */
		System.gc();
		Thread.sleep(100);

		return classHashCode;

	}

	@Test(timeout = 15 * 1000)
	public void testLoadUnload() throws Exception {

		int lastClassHash = 0;

		{
			final int nextClassHash = createSocketWithClassloader();
			assertTrue(nextClassHash != lastClassHash);
			lastClassHash = nextClassHash;
		}

		{
			final int nextClassHash = createSocketWithClassloader();
			assertTrue(nextClassHash != lastClassHash);
			lastClassHash = nextClassHash;
		}

		{
			final int nextClassHash = createSocketWithClassloader();
			assertTrue(nextClassHash != lastClassHash);
			lastClassHash = nextClassHash;
		}

	}

	public static void main(final String[] args) throws Throwable {

		final TestLoadUnload test = new TestLoadUnload();

		test.testLoadUnload();

	}

}
