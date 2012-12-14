/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * library naming conventions
 */
enum LibraryUDT {

	UNKNOWN("xxx.xxx.xxx", null), //

	I386_LINUX_GPP("i386.Linux.gpp", null), //
	AMD64_LINUX_GPP("amd64.Linux.gpp", null), //

	I386_MACOSX_GPP("i386.MacOSX.gpp", null), //
	X86_64_MACOSX_GPP("x86_64.MacOSX.gpp", null), //

	// X86_WINDOWS_MSVC("x86.Windows.msvc","msvcr90.dll,msvcp90.dll"), //
	// AMD64_WINDOWS_MSVC("amd64.Windows.msvc","msvcr90.dll,msvcp90.dll"), //

	/*
	 * NOTE: mingw dependency libraries must come from the same build of mingw
	 * compiler
	 */
	X86_WINDOWS_GPP("x86.Windows.gpp", //
			"libgcc_s_sjlj-1.dll,libstdc++-6.dll"), //
	AMD64_WINDOWS_GPP("amd64.Windows.gpp", //
			"libgcc_s_sjlj_64-1.dll,libstdc++_64-6.dll"), //

	;

	private final static Logger log = LoggerFactory.getLogger(LibraryUDT.class);

	static final String DOT = ".";
	static final String COMMA = ",";
	static final String SLASH = "/";
	static final String DASH = "-";
	static final String LIB = "lib";
	static final String JNI = "jni";
	static final String AOL = "aol";

	/** The Constant DEFAULT_EXTRACT_FOLDER_NAME. */
	public final static String DEFAULT_EXTRACT_FOLDER_NAME = DOT + SLASH + LIB;

	private final String[] depsList;

	private final AOL aol;

	LibraryUDT(final String aolKey, final String depNames) {

		this.depsList = getDeps(depNames);

		this.aol = new AOL(aolKey);

	}

	String[] getDeps(final String depsNames) {
		if (depsNames == null) {
			return null;
		}
		final String[] depsList = depsNames.split(COMMA);
		if (depsList == null || depsList.length == 0) {
			return null;
		}
		return depsList;
	}

	static LibraryUDT detectLibrary() {
		for (final LibraryUDT lib : values()) {
			if (lib.aol.isMatchJVM()) {
				return lib;
			}
		}
		return UNKNOWN;
	}

	/** load using default extract location */
	public static void load() throws Exception {
		load(null);
	}

	/** load using provided extract location */
	public static void load(/* non-final */String targetFolder)
			throws Exception {

		if (targetFolder == null || targetFolder.length() == 0) {
			targetFolder = DEFAULT_EXTRACT_FOLDER_NAME;
			log.warn("using default targetFolder={}", targetFolder);
		}

		final LibraryUDT library = detectLibrary();

		library.loadDeps(targetFolder);

		library.loadCore(targetFolder);

	}

	void loadDeps(final String targetFolder) throws Exception {

		if (depsList == null) {
			return;
		}

		for (final String depName : depsList) {

			log.info("\n\t dependency={}", depName);

			final String targetPath = targetResPath(targetFolder, depName);

			try {
				log.info("dependency source: NAR production");
				final String sourcePath = sourceDepRealNAR(depName);
				RES.systemLoad(sourcePath, targetPath);
				continue;
			} catch (final Exception e) {
				log.warn("\n\t {} {}", e.getClass().getSimpleName(), e
						.getMessage());
			}

			try {
				log.info("dependency source: NAR testing");
				final String sourcePath = sourceDepTestNAR(depName);
				RES.systemLoad(sourcePath, targetPath);
				continue;
			} catch (final Exception e) {
				log.warn("\n\t {} {}", e.getClass().getSimpleName(), e
						.getMessage());
			}

			throw new Exception("dependency library load failed");

		}

	}

	void loadCore(final String targetFolder) throws Exception {

		final String targetPath = targetResPath(targetFolder, coreName());

		try {
			log.info("library source: NAR production");
			final String sourcePath = sourceLibRealNAR();
			RES.systemLoad(sourcePath, targetPath);
			return;
		} catch (final Exception e) {
			log
					.warn("\n\t {} {}", e.getClass().getSimpleName(), e
							.getMessage());
		}

		try {
			log.info("library source: CDT testing");
			final String sourcePath = sourceLibTestCDT();
			RES.systemLoad(sourcePath, targetPath);
			return;
		} catch (final Exception e) {
			log
					.warn("\n\t {} {}", e.getClass().getSimpleName(), e
							.getMessage());
		}

		try {
			log.info("library source: NAR testing");
			final String sourcePath = sourceLibTestNAR();
			RES.systemLoad(sourcePath, targetPath);
			return;
		} catch (final Exception e) {
			log
					.warn("\n\t {} {}", e.getClass().getSimpleName(), e
							.getMessage());
		}

		throw new Exception("core library load failed");

	}

	/**
	 * testing: custom CDT name convention; produced by CDT interactive build
	 * and moved to the target/test-classes/ to make it part of test classpath
	 */
	// example:
	// /libbarchart-udt4-i386-Linux-g++.so
	String sourceLibTestCDT() {
		final String classifier = aol.resourceName();
		final String name = VersionUDT.BARCHART_ARTIFACT + DASH + classifier;
		final String library = System.mapLibraryName(name);
		final String path = //
		SLASH + library;
		return path;

	}

	/**
	 * testing: maven-nar-plugin name convention; custom location:
	 * target/test-classes; part of java test classpath
	 */
	// example:
	// /libbarchart-i386-Linux-g++-jni//lib/i386-Linux-g++/jni/libbarchart-1.0.2-SNAPSHOT.so
	String sourceLibTestNAR() {
		final String classifier = aol.resourceName();
		final String name = VersionUDT.BARCHART_NAME;
		final String folder = name + DASH + classifier + DASH + JNI;
		final String path = //
		SLASH + folder + SLASH + sourceLibRealNAR();
		return path;
	}

	/**
	 * production: maven-nar-plugin name convention; production location; part
	 * of production java classpath
	 */
	String sourceLibRealNAR() {
		return sourceResRealNAR(coreName());
	}

	// example:
	// /lib/i386-Linux-g++/jni/libbarchart-1.0.2-SNAPSHOT.so
	String sourceResRealNAR(final String resName) {
		final String classifier = aol.resourceName();
		final String path = //
		SLASH + LIB + SLASH + classifier + SLASH + JNI + SLASH + resName;
		return path;
	}

	/**
	 * both testing and production: location and naming for extracted library;
	 * note that source artifact in jar (or on classpath) and the extracted
	 * artifact (on file system) will have different names
	 */
	// example:
	// ./lib/i386-Linux-g++/libbarchart-1.0.2-SNAPSHOT.so
	String targetResPath(final String targetFolder, final String targetFile) {
		final String classifier = aol.resourceName();
		final String path = //
		targetFolder + SLASH + classifier + SLASH + targetFile;
		return path;
	}

	/** @see System#mapLibraryName */
	static String coreName() {
		return System.mapLibraryName(VersionUDT.BARCHART_NAME); // XXX
	}

	/**  */
	String sourceDepRealNAR(final String depName) {
		return sourceResRealNAR(depName);
	}

	/**  */
	String sourceDepTestNAR(final String depName) {
		return SLASH + AOL + SLASH + aol.resourceName() + //
				SLASH + LIB + SLASH + depName;
	}

}
