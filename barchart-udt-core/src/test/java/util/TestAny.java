/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package util;

import static java.lang.System.*;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base for all tests
 */
public class TestAny {

	protected static final Logger log = LoggerFactory.getLogger(TestAny.class);

	@BeforeClass
	public static void initClass() throws Exception {

		log.info("arch/os : {}/{}", //
				getProperty("os.arch"), getProperty("os.name"));

	}

}
