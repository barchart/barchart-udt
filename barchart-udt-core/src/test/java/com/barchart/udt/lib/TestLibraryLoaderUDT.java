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
