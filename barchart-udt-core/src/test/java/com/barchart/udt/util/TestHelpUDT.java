/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHelpUDT {

	private static final Logger log = LoggerFactory
			.getLogger(TestHelpUDT.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMd5sum() {
		final long signature = HelpUDT.md5sum("0123456789-ABCDEF?-");
		assertEquals(9050253258997952554L, signature);
		log.info("signature={}", signature);
	}

}
