/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;
import util.UnitHelp;

public class TestStatusUDT extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFromCode() {

		assertEquals(StatusUDT.UNKNOWN, StatusUDT.from(-1));

		assertEquals(StatusUDT.INIT, StatusUDT.from(1));
		assertEquals(StatusUDT.OPENED, StatusUDT.from(2));
		assertEquals(StatusUDT.LISTENING, StatusUDT.from(3));
		assertEquals(StatusUDT.CONNECTING, StatusUDT.from(4));
		assertEquals(StatusUDT.CONNECTED, StatusUDT.from(5));
		assertEquals(StatusUDT.BROKEN, StatusUDT.from(6));
		assertEquals(StatusUDT.CLOSING, StatusUDT.from(7));
		assertEquals(StatusUDT.CLOSED, StatusUDT.from(8));
		assertEquals(StatusUDT.NONEXIST, StatusUDT.from(9));

	}

	@Test
	public void testSocketStatus1() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, socket.status());

		final InetSocketAddress localAddress1 = UnitHelp.localSocketAddress();

		socket.bind(localAddress1);
		assertEquals(StatusUDT.OPENED, socket.status());

		socket.close();

		assertEquals(StatusUDT.CLOSED, socket.status());

	}

	@Test
	public void testSocketStatus2() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, socket.status());

		final InetSocketAddress localAddress1 = UnitHelp.localSocketAddress();

		socket.bind(localAddress1);
		assertEquals(StatusUDT.OPENED, socket.status());

		socket.listen(1);
		assertEquals(StatusUDT.LISTENING, socket.status());

		socket.close();
		assertEquals(StatusUDT.BROKEN, socket.status());

	}

	@Test
	public void testSocketStatus3() throws Exception {

		final InetSocketAddress clientAddress = UnitHelp.localSocketAddress();

		final InetSocketAddress serverAddress = UnitHelp.localSocketAddress();

		//

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, client.status());

		final SocketUDT server = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, server.status());

		//

		client.bind(clientAddress);
		assertEquals(StatusUDT.OPENED, client.status());

		server.bind(serverAddress);
		assertEquals(StatusUDT.OPENED, server.status());

		//

		server.listen(10);
		assertEquals(StatusUDT.LISTENING, server.status());

		//

		// final SocketUDT accept = server.accept();

		//

		final Thread serverThread = new Thread() {
			@Override
			public void run() {
				try {

					// final SocketUDT accept = server.accept();
					// assertEquals(StatusUDT.LISTENING, accept.getStatus());

				} catch (final Exception e) {
					fail(e.getMessage());
				}
			}
		};

		serverThread.start();

		serverThread.join();

		// Thread.sleep(10 * 1000);

	}

}
