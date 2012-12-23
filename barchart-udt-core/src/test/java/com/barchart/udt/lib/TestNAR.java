/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

import com.barchart.udt.lib.NAR.SupportedLinker;

public class TestNAR extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {

		log.info("started");

		final String filePath = NAR.NAR_AOL;

		final String narAol = NAR.readFileAsString(filePath);

		// log.info("\n{}", narAol);

		final String[] lines = narAol.split("\n");

		// log.info("lines.length={}", lines.length);

		final Set<String> set = new HashSet<String>();

		for (String line : lines) {

			line = line.trim();

			if (line.startsWith("#") || line.length() < 1) {
				continue;
			}

			final AOL aol = new AOL(line);

			if (aol.linker.contains("linker")) {
				continue;
			}

			if (!SupportedLinker.fromName(aol.linker).isKnown()) {
				continue;
			}

			final String name = aol.resourceName();

			set.add(name);

		}

		for (final String line : set.toArray(new String[] {})) {

			final String find = AOL.filterArch() + "-" + AOL.filterName();

			if (line.contains(find)) {

				// log.info("{}", line);

			}

		}

	}

}
