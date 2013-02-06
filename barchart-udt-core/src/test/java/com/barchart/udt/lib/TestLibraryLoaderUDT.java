/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import static org.junit.Assert.*;

import org.junit.Test;

import util.UnitHelp;

public class TestLibraryLoaderUDT {

	static {

		UnitHelp.logOsArch();
		UnitHelp.logClassPath();
		UnitHelp.logLibraryPath();

	}

	@Test
	public void testLoad() throws Exception {

		final String targetFolder = UnitHelp.randomSuffix("./target/testLoad");

		new LibraryLoaderUDT().load(targetFolder);

		assertTrue(true);

	}

}
