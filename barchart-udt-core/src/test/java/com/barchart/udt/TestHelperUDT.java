package com.barchart.udt;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHelperUDT {

	private static final Logger log = LoggerFactory
			.getLogger(TestHelperUDT.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMd5sum() {
		long signature = HelperUDT.md5sum("0123456789-ABCDEF?-");
		assertEquals(9050253258997952554L, signature);
		log.info("signature={}", signature);
	}

}
