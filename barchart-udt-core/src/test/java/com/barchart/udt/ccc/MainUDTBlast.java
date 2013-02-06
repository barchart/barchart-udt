/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.ccc;

public class MainUDTBlast {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		try {

			final TestUDPBlast blastTest = new TestUDPBlast();

			blastTest.test();

		} catch (final Throwable e) {

			e.printStackTrace();

		}

	}

}
