package com.barchart.udt.lib;

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
			return;
		} catch (final Throwable e) {
			log.warn("Release libraries missing: {}", e.getMessage());
		}

		try {
			log.info("Loading staging libraries.");
			loadStaging(targetFolder);
			return;
		} catch (final Throwable e) {
			log.warn("Staging libraries missing: {}", e.getMessage());
		}

		try {
			log.info("Loading testing libraries.");
			loadTesting(targetFolder);
			return;
		} catch (final Throwable e) {
			log.warn("Testing libraries missing: {}", e.getMessage());
		}

		throw new IllegalStateException("Fatal: library load failed.");

	}

	/** try to load from JAR class path library */
	protected void loadRelease(final String targetFolder) throws Exception {

		final String coreName = VersionUDT.BARCHART_NAME;

		for (final String sourcePath : PluginPropsUDT
				.currentReleaseLibraries(coreName)) {

			final String targetPath = targetFolder + "/" + sourcePath;

			ResourceManagerUDT.systemLoad(sourcePath, targetPath);

		}

	}

	/** try to load from NAR exploded class path library */
	protected void loadStaging(final String targetFolder) throws Exception {

		final String coreName = VersionUDT.BARCHART_NAME;

		for (final String sourcePath : PluginPropsUDT
				.currentStagingLibraries(coreName)) {

			final String targetPath = targetFolder + "/" + sourcePath;

			ResourceManagerUDT.systemLoad(sourcePath, targetPath);

		}

	}

	/** try to load from CDT exploded class path library */
	protected void loadTesting(final String targetFolder) throws Exception {

		final String coreName = VersionUDT.BARCHART_ARTIFACT + "-"
				+ PluginPropsUDT.currentNarPath();

		for (final String sourcePath : PluginPropsUDT
				.currentTestingLibraries(coreName)) {

			final String targetPath = targetFolder + "/" + sourcePath;

			ResourceManagerUDT.systemLoad(sourcePath, targetPath);

		}

	}

}
