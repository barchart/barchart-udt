/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publish library version information
 */
public class VersionUDT {

	protected static final Logger log = LoggerFactory
			.getLogger(VersionUDT.class);

	//

	protected static final String PROP_JAVA_VENDOR = "java.vendor";
	protected static final String PROP_JAVA_VERSION = "java.version";
	protected static final String PROP_JAVA_VM_NAME = "java.vm.name";

	public static final String JAVA_VENDOR = System
			.getProperty(PROP_JAVA_VENDOR);
	public static final String JAVA_VERSION = System
			.getProperty(PROP_JAVA_VERSION);
	public static final String JAVA_VM_NAME = System
			.getProperty(PROP_JAVA_VM_NAME);

	//

	protected static final String PROP_OS_NAME = "os.name";
	protected static final String PROP_OS_ARCH = "os.arch";
	protected static final String PROP_OS_VERSION = "os.version";

	public static final String OS_NAME = System.getProperty(PROP_OS_NAME);
	public static final String OS_ARCH = System.getProperty(PROP_OS_ARCH);
	public static final String OS_VERSION = System.getProperty(PROP_OS_VERSION);

	//

	protected static final String PROP_FILE = "version.properties";

	protected static final String PROP_UDT_VERSION = "udt.version";

	protected static final String PROP_BARCHART_NAME = "barchart.name";
	protected static final String PROP_BARCHART_GROUP = "barchart.groupId";
	protected static final String PROP_BARCHART_ARTIFACT = "barchart.artifactId";
	protected static final String PROP_BARCHART_VERSION = "barchart.version";
	protected static final String PROP_BARCHART_TIMESTAMP = "barchart.timestamp";

	public static final String UDT_VERSION;

	public static final String BARCHART_NAME;

	public static final String BARCHART_GROUP;
	public static final String BARCHART_ARTIFACT;
	public static final String BARCHART_VERSION;

	public static final String BARCHART_TIMESTAMP;

	static final String UNKNOWN = "UNKNOWN";

	static {

		String udtVersion = UNKNOWN;
		String name = UNKNOWN;
		String group = UNKNOWN;
		String artifact = UNKNOWN;
		String version = UNKNOWN;
		String timestamp = UNKNOWN;

		try {

			final Properties props = new Properties();

			final InputStream stream = VersionUDT.class.getClassLoader()
					.getResourceAsStream(PROP_FILE);

			props.load(stream);

			udtVersion = props.getProperty(PROP_UDT_VERSION);

			name = props.getProperty(PROP_BARCHART_NAME);
			group = props.getProperty(PROP_BARCHART_GROUP);
			artifact = props.getProperty(PROP_BARCHART_ARTIFACT);
			version = props.getProperty(PROP_BARCHART_VERSION);
			timestamp = props.getProperty(PROP_BARCHART_TIMESTAMP);

		} catch (final Exception e) {
			log.error("failed to load version properties", e);
		}

		UDT_VERSION = udtVersion;

		BARCHART_NAME = barchartName(name);

		BARCHART_GROUP = group;
		BARCHART_ARTIFACT = artifact;
		BARCHART_VERSION = version;
		BARCHART_TIMESTAMP = timestamp;

	}

	private static final String SNAPSHOT = "-SNAPSHOT";

	/**
	 * FIXME needs build system change
	 * <p>
	 * current contract is to depend on NAR snapshot
	 */
	protected static String barchartName(final String name) {
		if (name.contains(SNAPSHOT)) {
			return name;
		} else {
			return name + SNAPSHOT;
		}
	}

	protected static void append(final StringBuilder text, final String EOL) {

		text.append(PROP_BARCHART_NAME);
		text.append(" = ");
		text.append(BARCHART_NAME);
		text.append(EOL);

		text.append(PROP_BARCHART_GROUP);
		text.append(" = ");
		text.append(BARCHART_GROUP);
		text.append(EOL);

		text.append(PROP_BARCHART_ARTIFACT);
		text.append(" = ");
		text.append(BARCHART_ARTIFACT);
		text.append(EOL);

		text.append(PROP_BARCHART_VERSION);
		text.append(" = ");
		text.append(BARCHART_VERSION);
		text.append(EOL);

		text.append(PROP_BARCHART_TIMESTAMP);
		text.append(" = ");
		text.append(BARCHART_TIMESTAMP);
		text.append(EOL);

		text.append(PROP_JAVA_VENDOR);
		text.append(" = ");
		text.append(JAVA_VENDOR);
		text.append(EOL);

		text.append(PROP_JAVA_VERSION);
		text.append(" = ");
		text.append(JAVA_VERSION);
		text.append(EOL);

		text.append(PROP_JAVA_VM_NAME);
		text.append(" = ");
		text.append(JAVA_VM_NAME);
		text.append(EOL);

		text.append(PROP_OS_NAME);
		text.append(" = ");
		text.append(OS_NAME);
		text.append(EOL);

		text.append(PROP_OS_ARCH);
		text.append(" = ");
		text.append(OS_ARCH);
		text.append(EOL);

		text.append(PROP_OS_VERSION);
		text.append(" = ");
		text.append(OS_VERSION);
		text.append(EOL);

	}

	public static final void log() {
		log.info("\n{}", asText());
	}

	public static final String asText() {

		final StringBuilder text = new StringBuilder(128);

		text.append("\n");
		text.append("#######################################");
		text.append("\n");

		append(text, "\n");

		text.append("#######################################");
		text.append("\n");
		text.append("\n");

		return text.toString();

	}

	public static final String asHtml() {

		final StringBuilder text = new StringBuilder(128);

		text.append("<html><pre>");

		append(text, "<br>");

		text.append("</pre></html>");

		return text.toString();

	}

}
