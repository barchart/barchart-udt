/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * default library loader implementation;
 * <p>
 * tries to load native libraries by extracting them from from 3 possible class
 * path locations, in the following order:
 * <p>
 * 1) release : JAR packaged library
 * <p>
 * 2) staging : NAR exploded class path library
 * <p>
 * 3) testing : CDT exploded class path library
 */
public class LibraryLoaderUDT implements LibraryLoader {

	private final static Logger log = LoggerFactory
			.getLogger(LibraryLoaderUDT.class);

	/**
	 * load using provided extract location
	 */
	@Override
	public void load(final String targetFolder) throws Exception {

		if (PluginPropsUDT.isSupportedPlatform()) {
			log.info("Platform supported.");
		} else {
			throw new IllegalStateException("Unsupported platform.");
		}

		if (targetFolder == null || targetFolder.length() == 0) {
			throw new IllegalStateException("Invalid extract location.");
		}

		try {
			log.info("Loading release libraries.");
			loadRelease(targetFolder);
			log.info("Release libraries loaded.");
			return;
		} catch (final Throwable e) {
			log.warn("Release libraries missing: {}", e.getMessage());
		}

		try {
			log.info("Loading staging libraries.");
			loadStaging(targetFolder);
			log.info("Staging libraries loaded.");
			return;
		} catch (final Throwable e) {
			log.warn("Staging libraries missing: {}", e.getMessage());
		}

		try {
			log.info("Loading testing libraries.");
			loadTesting(targetFolder);
			log.info("Testing libraries loaded.");
			return;
		} catch (final Throwable e) {
			log.warn("Testing libraries missing: {}", e.getMessage());
		}

		throw new IllegalStateException("Fatal: library load failed.");

	}

	protected void loadAll(final List<String> sourceList,
			final String targetFolder) throws Exception {

		/** extract all libraries or fail */
		for (final String sourcePath : sourceList) {
			final String targetPath = targetFolder + sourcePath;
			ResourceManagerUDT.extractResource(sourcePath, targetPath);
		}

		/** try to load only if all are extracted */
		for (final String sourcePath : sourceList) {
			final String targetPath = targetFolder + sourcePath;
			ResourceManagerUDT.systemLoad(targetPath);
		}

	}

	/** try to load from JAR class path library */
	protected void loadRelease(final String targetFolder) throws Exception {

		final String coreName = VersionUDT.BARCHART_NAME;

		final List<String> sourceList = PluginPropsUDT
				.currentReleaseLibraries(coreName);

		loadAll(sourceList, targetFolder);

	}

	/** try to load from NAR exploded class path library */
	protected void loadStaging(final String targetFolder) throws Exception {

		final String coreName = VersionUDT.BARCHART_NAME;

		final List<String> sourceList = PluginPropsUDT
				.currentStagingLibraries(coreName);

		loadAll(sourceList, targetFolder);

	}

	/** try to load from CDT exploded class path library */
	protected void loadTesting(final String targetFolder) throws Exception {

		final String coreName = VersionUDT.BARCHART_ARTIFACT + "-"
				+ PluginPropsUDT.currentNarPath();

		final List<String> sourceList = PluginPropsUDT
				.currentTestingLibraries(coreName);

		loadAll(sourceList, targetFolder);

	}

}
