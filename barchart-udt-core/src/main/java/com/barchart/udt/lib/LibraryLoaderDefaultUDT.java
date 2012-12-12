/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

/**
 * default loader implementation
 */
public class LibraryLoaderDefaultUDT implements LibraryLoaderUDT {

	@Override
	public void load(final String location) throws Exception {

		LibraryUDT.load(location);

	}

}
