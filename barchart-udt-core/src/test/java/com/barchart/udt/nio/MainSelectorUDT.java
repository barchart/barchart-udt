/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

public class MainSelectorUDT {

	public static void main(String[] args) throws Exception {

		TestSelectorUDT test = new TestSelectorUDT();

		test.setUp();

		test.testSelect();

		test.tearDown();

	}

}
