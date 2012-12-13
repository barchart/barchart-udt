/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class TestDummy {

	@Test
	public void testLibraryLoad() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		assertTrue(socket.isOpen());

	}

}
