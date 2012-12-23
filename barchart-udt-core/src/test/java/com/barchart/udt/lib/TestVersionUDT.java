/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import static com.barchart.udt.lib.VersionUDT.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

public class TestVersionUDT extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLog() {

		VersionUDT.log();

		assertTrue(true);

	}

	@Test
	public void testName() {

		assertEquals("must convert release to snapshot",
				"barchart-udt-core-2.0.0-SNAPSHOT",
				barchartName("barchart-udt-core-2.0.0"));

		assertEquals("must keep snapshot as snapshot ",
				"barchart-udt-core-2.0.0-SNAPSHOT",
				barchartName("barchart-udt-core-2.0.0-SNAPSHOT"));

	}

}
