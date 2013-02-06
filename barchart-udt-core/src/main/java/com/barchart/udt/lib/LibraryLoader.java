/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.lib;

import com.barchart.udt.ResourceUDT;

/**
 * generic library loader service; alternative implementation can be provided
 * with {@link ResourceUDT}
 */
public interface LibraryLoader {

	/**
	 * load library with extract to provided location
	 */
	void load(String location) throws Exception;

}
