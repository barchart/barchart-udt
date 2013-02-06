/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.libs;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class ClassScope {

	private static final String[] EMPTY_LIBRARY_ARRAY = new String[] {};
	private static final Throwable CVF_FAILURE; // set in <clinit>

	static {

		Throwable failure = null;
		Field field = null;

		try {
			// this can fail if this is not a Sun-compatible JVM
			// or if the security is too tight:

			field = ClassLoader.class.getDeclaredField("loadedLibraryNames");

			if (field.getType() != Vector.class) {
				throw new RuntimeException("not of type java.util.Vector: "
						+ field.getType().getName());
			}

			field.setAccessible(true);

		} catch (Throwable t) {
			failure = t;
		}

		LIBRARIES_VECTOR_FIELD = field;
		CVF_FAILURE = failure;

		failure = null;

	}

	/**
	 * Given a class loader instance, returns all native libraries currently
	 * loaded by that class loader.
	 * 
	 * @param defining
	 *            class loader to inspect [may not be null]
	 * @return Libraries loaded in this class loader [never null, may be empty]
	 * 
	 * @throws RuntimeException
	 *             if the "loadedLibraryNames" field hack is not possible in
	 *             this JRE
	 */
	@SuppressWarnings("unchecked")
	public static String[] getLoadedLibraries(final ClassLoader loader) {

		if (loader == null) {
			throw new IllegalArgumentException("null input: loader");
		}

		if (LIBRARIES_VECTOR_FIELD == null) {
			throw new RuntimeException(
					"ClassScope::getLoadedLibraries() cannot be used in this JRE",
					CVF_FAILURE);
		}

		try {

			final Vector<String> libraries = (Vector<String>) LIBRARIES_VECTOR_FIELD
					.get(loader);

			if (libraries == null) {
				return EMPTY_LIBRARY_ARRAY;
			}

			final String[] result;

			// note: Vector is synchronized in Java 2, which helps us make
			// the following into a safe critical section:
			synchronized (libraries) {
				result = libraries.toArray(new String[] {});
			}

			return result;

		}

		// this should not happen if <clinit> was successful:
		catch (IllegalAccessException e) {
			e.printStackTrace(System.out);
			return EMPTY_LIBRARY_ARRAY;
		}
	}

	/**
	 * A convenience multi-loader version of
	 * {@link #getLoadedLibraries(ClassLoader)}.
	 * 
	 * @param an
	 *            array of defining class loaders to inspect [may not be null]
	 * @return String array [never null, may be empty]
	 * 
	 * @throws RuntimeException
	 *             if the "loadedLibrayNames" field hack is not possible in this
	 *             JRE
	 */
	public static String[] getLoadedLibraries(final ClassLoader[] loaders) {
		if (loaders == null) {
			throw new IllegalArgumentException("null input: loaders");
		}
		final List<String> resultList = new LinkedList<String>();

		for (int l = 0; l < loaders.length; ++l) {
			final ClassLoader loader = loaders[l];
			if (loader != null) {
				final String[] libraries = getLoadedLibraries(loaders[l]);
				resultList.addAll(Arrays.asList(libraries));
			}
		}

		final String[] result = new String[resultList.size()];
		resultList.toArray(result);

		return result;

	}

	// this class is not extendible
	private ClassScope() {
	}

	// set in <clinit> [can be null]
	private static final Field LIBRARIES_VECTOR_FIELD;

}
