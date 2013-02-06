/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

public class MainSelectorUDT {

	public static void main(final String[] args) throws Exception {

		final TestSelectorUDT test = new TestSelectorUDT();

		test.init();

		test.testSelect();

		test.done();

	}

}
