/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

public class MainOptionCC {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		try {
			final TestOption option = new TestOption();

			option.testOptionCC();
		} catch (final Throwable e) {
			e.printStackTrace();
		}

	}

}
