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

public class TestRendezvous extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(timeout = 5 * 1000)
	public void rendezvousConnect() throws Exception {

		final InetSocketAddress addr1 = localSocketAddress();
		final InetSocketAddress addr2 = localSocketAddress();

		final SocketUDT peer1 = new SocketUDT(TypeUDT.DATAGRAM);
		final SocketUDT peer2 = new SocketUDT(TypeUDT.DATAGRAM);

		peer1.configureBlocking(false);
		peer2.configureBlocking(false);

		peer1.setRendezvous(true);
		peer2.setRendezvous(true);

		assertTrue("non blocking", peer1.isNonBlocking());
		assertTrue("non blocking", peer2.isNonBlocking());

		assertTrue("use randezvous", peer1.isRendezvous());
		assertTrue("use randezvous", peer2.isRendezvous());

		peer1.bind(addr1);
		peer2.bind(addr2);

		log.info("state 0 - bound");
		log.info("peer1 : {}", peer1);
		log.info("peer2 : {}", peer2);

		socketAwait(peer1, StatusUDT.OPENED);
		socketAwait(peer2, StatusUDT.OPENED);

		peer1.connect(addr2);
		peer2.connect(addr1);

		log.info("state 1 - connecting");
		log.info("peer1 : {}", peer1);
		log.info("peer2 : {}", peer2);

		socketAwait(peer1, StatusUDT.CONNECTED);
		socketAwait(peer2, StatusUDT.CONNECTED);

		log.info("state 2 - rendezvous");
		log.info("peer1 : {}", peer1);
		log.info("peer2 : {}", peer2);

		peer1.close();
		peer2.close();

		log.info("state 3 - closed");
		log.info("peer1 : {}", peer1);
		log.info("peer2 : {}", peer2);

	}

	@Test(timeout = 5 * 1000)
	public void rendezvousSelect() throws Exception {

	}

}
