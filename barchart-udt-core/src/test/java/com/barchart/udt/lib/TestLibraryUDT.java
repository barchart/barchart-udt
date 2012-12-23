/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import static com.barchart.udt.lib.LibraryUDT.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;
import util.UnitHelp;

import com.barchart.udt.ResourceUDT;

public class TestLibraryUDT extends TestAny {

	static {

		UnitHelp.logOsArch();
		UnitHelp.logClassPath();
		UnitHelp.logLibraryPath();

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoadWithProps() throws Exception {

		ResourceUDT.setLibraryLoaderClassName( //
				LibraryLoaderDefaultUDT.class.getName());

		ResourceUDT.setLibraryExtractLocation(UnitHelp
				.randomSuffix("./target/testLoadWithProps"));

		LibraryUDT.load();

		assertTrue(true);

	}

	@Test
	public void testLoadWithTarget() throws Exception {

		final String targetFolder = UnitHelp
				.randomSuffix("./target/testLoadWithTarget");

		LibraryUDT.load(targetFolder);

		assertTrue(true);

	}

	@Test
	public void testPath() {

		assertEquals(//
				I386_LINUX_GPP.sourceLibRealNAR(), //
				"/lib/i386-Linux-gpp/jni/" + coreName());

		assertEquals(//
				I386_LINUX_GPP.targetResPath("./lib", coreName()), //
				"./lib/i386-Linux-gpp/" + coreName());

		//

		assertEquals( //
				X86_WINDOWS_GPP.sourceDepTestNAR("nar.dll"), //
				"/aol/x86-Windows-gpp/lib/nar.dll");

	}

}
