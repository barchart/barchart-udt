/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.libs;

/***
 * This class extracts all native libraries loaded in the JVM
 */
public class AllLoadedNativeLibrariesInJVM {

	public static void listAllLoadedNativeLibrariesFromJVM() {

		ClassLoader appLoader = ClassLoader.getSystemClassLoader();

		ClassLoader currentLoader = AllLoadedNativeLibrariesInJVM.class
				.getClassLoader();

		ClassLoader[] loaders = new ClassLoader[] { appLoader, currentLoader };

		final String[] libraries = ClassScope.getLoadedLibraries(loaders);

		for (String library : libraries) {
			System.out.println(library);
		}

	}

	public static void main(String[] args) throws Exception {
		listAllLoadedNativeLibrariesFromJVM();
	}

}