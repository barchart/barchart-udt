/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;
import static util.UnitHelp.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

public class TestSocketUDT extends TestAny {

	@Test
	public void acceptListenNone() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		socket.setBlocking(false);

		socket.bind(localSocketAddress());

		socket.listen(1);

		assertNull(socket.accept());

		socket.close();

	}

	@Test(timeout = 3 * 1000)
	public void acceptListenOne() throws Exception {

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.setBlocking(false);
		accept.bind(localSocketAddress());

		socketAwait(accept, StatusUDT.OPENED);

		accept.listen(1);

		socketAwait(accept, StatusUDT.LISTENING);

		assertEquals(StatusUDT.LISTENING, accept.status());

		assertNull(accept.accept());

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.setBlocking(false);
		client.bind(localSocketAddress());

		socketAwait(client, StatusUDT.OPENED);

		client.connect(accept.getLocalSocketAddress());

		socketAwait(client, StatusUDT.CONNECTED);

		assertNotNull(accept.accept());

		assertNull(accept.accept());

		accept.close();
		client.close();

	}

	@Test(expected = ExceptionUDT.class)
	public void acceptNoListen() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		socket.accept();

		socket.close();

	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void socketOpenClose() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertTrue(socket.isOpen());

		socket.setOption(OptionUDT.Is_Receive_Synchronous, false);
		socket.setOption(OptionUDT.Is_Send_Synchronous, false);
		assertTrue(socket.isOpen());

		final InetSocketAddress localSocketAddress = localSocketAddress();

		socket.bind(localSocketAddress);
		assertTrue(socket.isOpen());

		socket.listen(1);
		assertTrue(socket.isOpen());

		final SocketUDT connector = socket.accept();
		assertNull(connector);
		assertTrue(socket.isOpen());

		socket.close();
		assertFalse(socket.isOpen());

		socket.close();
		assertFalse(socket.isOpen());

		socket.close();
		assertTrue(socket.isClosed());

		socket.close();
		assertTrue(socket.isClosed());

		socket.close();

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = ExceptionUDT.class)
	public void testInvalidClose0() throws ExceptionUDT {

		SocketUDT socket = null;

		try {

			socket = new SocketUDT(TypeUDT.DATAGRAM);

		} catch (final ExceptionUDT e) {

			fail("SocketException; " + e.getMessage());

		}

		final int realID = socket.id();

		final int fakeID = realID + 123;

		log.info("real: {} ; fake : {} ; ", realID, fakeID);

		/** must throw */
		SocketUDT.testInvalidClose0(fakeID);

	}

	@Test
	public void testToString() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		log.info("{}", socket);

	}

	/** udt uses hard coded connect timeout of 3 seconds */
	@Test(timeout = 10 * 1000)
	public void connectTimeout() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		long timeStart = 0;
		long timeFinish = 0;

		try {

			socket.bind(localSocketAddress());

			timeStart = System.currentTimeMillis();

			socket.connect(localSocketAddress());

		} catch (final ExceptionUDT e) {

			switch (e.getError()) {
			case NOSERVER:
				timeFinish = System.currentTimeMillis();
				return;
			}

		} finally {

			socket.close();

			final long timeDiff = timeFinish - timeStart;

			log.info("timeout = {} seconds", timeDiff / 1000);

		}

	}

}
