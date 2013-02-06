/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import static org.junit.Assert.*;

import org.junit.Test;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class TestSocketChannelUDT {

	@Test
	public void blocking() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;
		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		final SocketChannelUDT channel = new SocketChannelUDT(provider, socket);

		assertTrue(socket.isOpen());
		assertTrue(channel.isOpen());

		assertTrue(socket.isBlocking());
		assertTrue(channel.isBlocking());

		channel.configureBlocking(false);

		assertFalse(socket.isBlocking());
		assertFalse(channel.isBlocking());

		channel.close();

		assertFalse(socket.isOpen());
		assertFalse(channel.isOpen());

	}

}
