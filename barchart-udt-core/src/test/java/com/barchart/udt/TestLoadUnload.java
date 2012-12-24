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

	class JNIClassLoader extends ClassLoader {

		private final Map<String, Class<?>> classes;

		File classLoadRoot;

		public JNIClassLoader(final File classLoadRoot) {
			super(JNIClassLoader.class.getClassLoader());
			classes = new HashMap<String, Class<?>>();
			this.classLoadRoot = classLoadRoot;
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
			final DataInputStream in = new DataInputStream(new FileInputStream(
					file));

			in.readFully(buff);
			in.close();

			return buff;
		}

		@Override
		protected void finalize() throws Throwable {
			log.info("Finalised JNI class loader");
			super.finalize();
		}

	}

	Logger log = LoggerFactory.getLogger(TestLoadUnload.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void createSocketWithClassloader() throws ClassNotFoundException,
			IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		final String currentDir = new File(".").getAbsolutePath();
		log.info(String.format("Current directory: %s", currentDir));

		// HACK - ideally we should update JNIClassLoader to
		// use the whole java classpath
		final File classPath = new File("target/classes");

		JNIClassLoader cl = new JNIClassLoader(classPath);

		Class<Enum> typeClass = (Class<Enum>) cl
				.findClass("com.barchart.udt.TypeUDT");

		Class<?> ca = cl.findClass("com.barchart.udt.SocketUDT");

		Object udt = ca.getDeclaredConstructor(typeClass).newInstance(
				Enum.valueOf(typeClass, "DATAGRAM"));

		ca.getMethod("cleanup").invoke(udt);

		log.info(String.format("Socket hash code: %s", udt.hashCode()));

		ca = null;
		typeClass = null;
		udt = null;
		cl = null;
		System.gc();
	}

	@Test
	public void testLoadUnload() {

		try {

			createSocketWithClassloader();

			createSocketWithClassloader();

		} catch (final ClassNotFoundException e) {
			fail("Failed to find SocketUDT class for load/unload test");
		} catch (final InstantiationException e) {
			log.error("Failed to create new SocketUDT class", e);
			fail("Failed to instansiate SocketUDT class for load/unload test");
		} catch (final IllegalAccessException e) {
			fail("Failed to create SocketUDT class for load/unload test");
		} catch (final IllegalArgumentException e) {
			log.error("Failed to construct SocketUDT", e);
			fail("Failed to create SocketUDT class for load/unload test");
		} catch (final SecurityException e) {
			fail("Failed to create SocketUDT class for load/unload test");
		} catch (final InvocationTargetException e) {
			fail("Failed to create SocketUDT class for load/unload test");
		} catch (final NoSuchMethodException e) {
			log.error("Failed to construct SocketUDT", e);
			fail("Failed to create SocketUDT class for load/unload test");
		} finally {
		}

	}

	public static void main(final String[] args) {

		final TestLoadUnload lul = new TestLoadUnload();
		lul.testLoadUnload();

	}

}
