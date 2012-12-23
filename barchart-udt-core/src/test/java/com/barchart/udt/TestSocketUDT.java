/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
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

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSelectEx0() {

		log.info("testSelectEx0");

		try {

			final InetSocketAddress localAddress1 = localSocketAddress();

			final InetSocketAddress localAddress2 = localSocketAddress();

			final SocketUDT socketServer = new SocketUDT(TypeUDT.DATAGRAM);
			socketServer.setOption(OptionUDT.UDT_RCVSYN, false);
			socketServer.setOption(OptionUDT.UDT_SNDSYN, false);
			socketServer.bind(localAddress1);
			socketServer.listen(1);
			// socketServer.accept();

			final SocketUDT socketClient = new SocketUDT(TypeUDT.DATAGRAM);
			socketClient.setOption(OptionUDT.UDT_RCVSYN, false);
			socketClient.setOption(OptionUDT.UDT_SNDSYN, false);
			socketClient.bind(localAddress2);
			socketClient.listen(1);
			// socketClient.accept();

			final long timeout = 1 * 1000 * 1000;

			final SocketUDT[] selectArray = new SocketUDT[] { socketServer,
					socketClient };

			socketServer.clearError();

			final long timeStart = System.currentTimeMillis();

			// SocketUDT.selectExtended(selectArray, timeout);

			final long timeFinish = System.currentTimeMillis();

			final long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			// log.info("isSelectedRead={}", socketServer.isSelectedRead());
			// log.info("isSelectedWrite={}", socketServer.isSelectedWrite());
			// log.info("isSelectedException={}", socketServer
			// .isSelectedException());

			log.info("getError={}", socketServer.getError());
			log.info("getErrorCode={}", socketServer.getErrorCode());
			log.info("getgetErrorMessage={}", socketServer.getErrorMessage());

			socketServer.close();
			socketClient.close();

		} catch (final Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	@Test(expected = ExceptionUDT.class)
	public void testInvalidClose0() throws ExceptionUDT {

		SocketUDT socket = null;

		try {

			socket = new SocketUDT(TypeUDT.DATAGRAM);

		} catch (final ExceptionUDT e) {

			fail("SocketException; " + e.getMessage());

		}

		final int realID = socket.socketID;

		final int fakeID = realID + 123;

		log.info("real: {} ; fake : {} ; ", realID, fakeID);

		/** must throw */
		socket.testInvalidClose0(fakeID);

	}

	@Test
	public void testIsOpen() throws Exception {

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

		// log.info("sleep 1");
		// Thread.sleep(10 * 1000);

		socket.close();
		assertTrue(socket.isClosed());

		// log.info("sleep 2");
		// Thread.sleep(10 * 1000);

		socket.close();
		assertTrue(socket.isClosed());

		log.info("isOpen pass");

	}

	@Test(expected = ExceptionUDT.class)
	public void testAcceptNoListen() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		socket.accept();

	}

	@Test
	public void testAcceptListenNone() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		socket.configureBlocking(false);

		socket.bind(localSocketAddress());

		socket.listen(1);

		assertNull(socket.accept());

	}

	@Test
	public void testAcceptListenOne() throws Exception {

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.configureBlocking(false);
		accept.bind(localSocketAddress());

		assertEquals(StatusUDT.OPENED, accept.getStatus());

		accept.listen(1);

		assertEquals(StatusUDT.LISTENING, accept.getStatus());

		assertNull(accept.accept());

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.configureBlocking(false);
		client.bind(localSocketAddress());

		client.connect(accept.getLocalSocketAddress());

		Thread.sleep(100);

		assertNotNull(accept.accept());

		Thread.sleep(100);

		assertNull(accept.accept());

	}

	@Test
	public void testToString() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		log.info("{}", socket);

	}

}
