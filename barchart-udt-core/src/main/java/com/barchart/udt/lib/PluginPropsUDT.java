/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * maven-nar-plugin build-time properties which determine library name and path
 * conventions; AOL stands for "arch/os/linker"
 */
public class PluginPropsUDT {

	/**
	 * suffix for c++ compiler configured for platform in
	 * {@link #NAR_AOL_PROPERTIES} file
	 */
	protected static final String AOL_CPP_COMPILER = "cpp.compiler";

	/**
	 * suffix for JNI library file extension as defined in
	 * {@link #NAR_AOL_PROPERTIES} file
	 */
	protected static final String AOL_JNI_EXTENSION = "jni.extension";

	/**
	 * suffix for dependency libraries as defined in {@link #NAR_AOL_PROPERTIES}
	 * file
	 */
	protected static final String AOL_LINKER_DEPENDENCY = "linker.dependency";

	/**
	 * suffix for shared library file prefix as defined in
	 * {@link #NAR_AOL_PROPERTIES} file
	 */
	protected static final String AOL_SHARED_PREFIX = "shared.prefix";

	/**
	 * missing / invalid property value representation
	 */
	protected static final String EMPTY_VALUE = "";

	protected static final Logger log = LoggerFactory
			.getLogger(PluginPropsUDT.class);

	/**
	 * NAR properties file; should be present in project root folder during
	 * build time for plug-in configuration and as a class path resource during
	 * run time for properties lookup.
	 */
	protected static final String NAR_AOL_PROPERTIES = "aol.properties";

	/**
	 * current architecture reported by JVM
	 */
	protected static final String OS_ARCH = System.getProperty("os.arch");

	/**
	 * current operating system reported by JVM
	 */
	protected static final String OS_NAME = System.getProperty("os.name");

	/**
	 * properties from {@link #NAR_AOL_PROPERTIES} file
	 */
	protected static final Properties props = new Properties();

	static {

		try {

			log.info("ARCH/OS/LINK = {}/{}/{}", narARCH(), narOS(), narLINK());

			log.debug("Loading aol.properties.");

			final InputStream input = PluginPropsUDT.class.getClassLoader()
					.getResourceAsStream(NAR_AOL_PROPERTIES);

			props.load(input);

		} catch (final Throwable e) {

			log.error("Failed to load aol.properties.", e);

		}

	}

	/**
	 * dependency libraries specified in {@link #NAR_AOL_PROPERTIES} file
	 */
	protected static List<String> currentDependencyLibraries() {

		final List<String> list = new ArrayList<String>();

		final String entryText = property(currentNarKeyLinkerDependency());

		if (entryText == null || entryText.length() == 0) {
			return list;
		}

		final String[] entryArray = entryText.split("\\s");

		for (final String entry : entryArray) {
			list.add(entry.trim());
		}

		return list;

	}

	/**
	 * nar key prefix for current arch/os/link
	 */
	protected static String currentNarKey() {
		return formatNarKey(narARCH(), narOS(), narLINK());
	}

	/**
	 * full nar key built from arch/os/link prefix and the key suffix
	 */
	protected static String currentNarKey(final String suffix) {
		return currentNarKey() + "." + suffix;
	}

	/**
	 * CPP compiler for the platform
	 */
	protected static String currentNarKeyCppCompiler() {
		return currentNarKey(AOL_CPP_COMPILER);
	}

	/**
	 * JNI extension for the platform
	 */
	protected static String currentNarKeyJniExtension() {
		return currentNarKey(AOL_JNI_EXTENSION);
	}

	/**
	 * list of dependencies for the platform
	 */
	protected static String currentNarKeyLinkerDependency() {
		return currentNarKey(AOL_LINKER_DEPENDENCY);
	}

	/**
	 * library name prefix for the platform
	 */
	protected static String currentNarKeySharedPrefix() {
		return currentNarKey(AOL_SHARED_PREFIX);
	}

	/**
	 * nar aol path entry for current arch/os/link
	 */
	protected static String currentNarPath() {
		return formatNarPath(narARCH(), narOS(), narLINK());
	}

	/**
	 * list of release library paths expected to be resent for current platform;
	 * must be packaged in the jar; used during release delivery
	 */
	protected static List<String> currentReleaseLibraries(final String coreName) {

		final List<String> list = new ArrayList<String>();

		/** dependency */
		for (final String name : currentDependencyLibraries()) {
			final String path = formatMainReleasePath(name);
			list.add(path);
		}

		/** main */
		{
			final String name = formatMainLibraryName(coreName);
			final String path = formatMainReleasePath(name);
			list.add(path);
		}

		return list;

	}

	/**
	 * list of staging library paths expected to be resent for current platform;
	 * must be located on test class path; used during NAR build
	 */
	protected static List<String> currentStagingLibraries(final String coreName) {

		final List<String> list = new ArrayList<String>();

		/** dependency */
		for (final String name : currentDependencyLibraries()) {
			final String path = formatTestingDependencyPath(name);
			list.add(path);
		}

		/** main */
		{
			final String path = formatMainStagingPath(coreName);
			list.add(path);
		}

		return list;

	}

	/**
	 * list of testing library paths expected to be resent for current platform;
	 * must be located on test class path; used during CDT build
	 */
	protected static List<String> currentTestingLibraries(final String coreName) {

		final List<String> list = new ArrayList<String>();

		/** dependency */
		for (final String name : currentDependencyLibraries()) {
			final String path = formatTestingDependencyPath(name);
			list.add(path);
		}

		/** main */
		{
			final String path = formatMainTestingPath(coreName);
			list.add(path);
		}

		return list;

	}

	/**
	 * make main library name with {@link #AOL_SHARED_PREFIX} and
	 * {@link #AOL_JNI_EXTENSION} convention
	 */
	protected static String formatMainLibraryName(final String coreName) {
		final String prefix = property(currentNarKeySharedPrefix());
		final String extension = property(currentNarKeyJniExtension());
		return String.format("%s%s.%s", prefix, coreName, extension);
	}

	/**
	 * location of release main library and dependency libraries in release jar
	 */
	protected static String formatMainReleasePath(final String name) {
		final String aol = currentNarPath();
		return String.format("/lib/%s/jni/%s", aol, name);
	}

	/**
	 * location of staging main library on the test class path
	 */
	protected static String formatMainStagingPath(final String coreName) {
		final String aol = currentNarPath();
		final String mainName = formatMainLibraryName(coreName);
		return String.format("/%s-%s-jni/lib/%s/jni/%s", //
				coreName, aol, aol, mainName);
	}

	/**
	 * location of testing main library on the test class path
	 */
	protected static String formatMainTestingPath(final String coreName) {
		final String mainName = formatMainLibraryName(coreName);
		return String.format("/%s", //
				mainName);
	}

	/**
	 * {@link #NAR_AOL_PROPERTIES} property key prefix
	 */
	protected static String formatNarKey(final String arch, final String os,
			final String link) {
		return String.format("%s.%s.%s", arch, os, link);
	}

	/**
	 * {@link #NAR_AOL_PROPERTIES} aol path element
	 */
	protected static String formatNarPath(final String arch, final String os,
			final String link) {
		return String.format("%s-%s-%s", arch, os, link);
	}

	/**
	 * location of dependency libraries on test class path
	 */
	protected static String formatTestingDependencyPath(final String name) {
		final String aol = currentNarPath();
		return String.format("/aol/%s/lib/%s", aol, name);
	}

	/**
	 * is current platform defined in {@link #NAR_AOL_PROPERTIES} file?
	 */
	protected static boolean isSupportedPlatform() {
		return !EMPTY_VALUE.equals(property(currentNarKeyCppCompiler()));
	}

	/**
	 * map JVM arch name to NAR arch name
	 */
	protected static String narARCH() {
		return OS_ARCH;
	}

	/**
	 * map JVM arch/os name to NAR tool chain name
	 */
	protected static String narLINK() {
		return "gpp";
	}

	/**
	 * map JVM os name to NAR os name
	 */
	protected static String narOS() {
		if (OS_NAME.contains("Mac OS X")) {
			return "MacOSX";
		}
		if (OS_NAME.contains("Windows")) {
			return "Windows";
		}
		return OS_NAME;
	}

	/**
	 * find property entry in the {@link #NAR_AOL_PROPERTIES} file
	 */
	protected static String property(final String key) {
		final String value = props.getProperty(key);
		if (value instanceof String) {
			return value;
		} else {
			return EMPTY_VALUE;
		}
	}

}
