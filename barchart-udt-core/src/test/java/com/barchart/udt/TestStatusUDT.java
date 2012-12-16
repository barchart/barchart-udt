/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.TestHelp;

public class TestStatusUDT {

	static final Logger log = LoggerFactory.getLogger(TestStatusUDT.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@Test
	public void testFromCode() {

		assertEquals(StatusUDT.UNKNOWN, StatusUDT.fromCode(-1));

		assertEquals(StatusUDT.INIT, StatusUDT.fromCode(1));
		assertEquals(StatusUDT.OPENED, StatusUDT.fromCode(2));
		assertEquals(StatusUDT.LISTENING, StatusUDT.fromCode(3));
		assertEquals(StatusUDT.CONNECTING, StatusUDT.fromCode(4));
		assertEquals(StatusUDT.CONNECTED, StatusUDT.fromCode(5));
		assertEquals(StatusUDT.BROKEN, StatusUDT.fromCode(6));
		assertEquals(StatusUDT.CLOSING, StatusUDT.fromCode(7));
		assertEquals(StatusUDT.CLOSED, StatusUDT.fromCode(8));
		assertEquals(StatusUDT.NONEXIST, StatusUDT.fromCode(9));

	}

	@Test
	public void testSocketStatus1() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, socket.getStatus());

		final InetSocketAddress localAddress1 = TestHelp
				.getLocalSocketAddress();

		socket.bind(localAddress1);
		assertEquals(StatusUDT.OPENED, socket.getStatus());

		socket.close();

		assertEquals(StatusUDT.CLOSED, socket.getStatus());

	}

	@Test
	public void testSocketStatus2() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, socket.getStatus());

		final InetSocketAddress localAddress1 = TestHelp
				.getLocalSocketAddress();

		socket.bind(localAddress1);
		assertEquals(StatusUDT.OPENED, socket.getStatus());

		socket.listen(1);
		assertEquals(StatusUDT.LISTENING, socket.getStatus());

		socket.close();
		assertEquals(StatusUDT.BROKEN, socket.getStatus());

	}

	@Test
	public void testSocketStatus3() throws Exception {

		final InetSocketAddress clientAddress = TestHelp
				.getLocalSocketAddress();

		final InetSocketAddress serverAddress = TestHelp
				.getLocalSocketAddress();

		//

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, client.getStatus());

		final SocketUDT server = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, server.getStatus());

		//

		client.bind(clientAddress);
		assertEquals(StatusUDT.OPENED, client.getStatus());

		server.bind(serverAddress);
		assertEquals(StatusUDT.OPENED, server.getStatus());

		//

		server.listen(10);
		assertEquals(StatusUDT.LISTENING, server.getStatus());

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
