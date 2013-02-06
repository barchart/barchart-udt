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
import java.nio.IntBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

import com.barchart.udt.util.HelpUDT;

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

		peer1.setBlocking(false);
		peer2.setBlocking(false);

		peer1.setRendezvous(true);
		peer2.setRendezvous(true);

		assertTrue("non blocking", peer1.isNonBlocking());
		assertTrue("non blocking", peer2.isNonBlocking());

		assertTrue("use randezvous", peer1.isRendezvous());
		assertTrue("use randezvous", peer2.isRendezvous());

		peer1.bind(addr1);
		peer2.bind(addr2);

		socketAwait(peer1, StatusUDT.OPENED);
		socketAwait(peer2, StatusUDT.OPENED);

		log.info("state 0 - bound");
		log.info("peer1 : {}", peer1);
		log.info("peer2 : {}", peer2);

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

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final int epollID = SocketUDT.epollCreate0();

		final InetSocketAddress addr1 = localSocketAddress();
		final InetSocketAddress addr2 = localSocketAddress();

		final SocketUDT peer1 = new SocketUDT(TypeUDT.DATAGRAM);
		final SocketUDT peer2 = new SocketUDT(TypeUDT.DATAGRAM);

		peer1.setBlocking(false);
		peer2.setBlocking(false);

		peer1.setRendezvous(true);
		peer2.setRendezvous(true);

		peer1.bind(addr1);
		peer2.bind(addr2);

		socketAwait(peer1, StatusUDT.OPENED);
		socketAwait(peer2, StatusUDT.OPENED);

		log.info("state 0 - bound");
		log.info("peer1 : {}", peer1);
		log.info("peer2 : {}", peer2);

		SocketUDT.epollAdd0(epollID, peer1.id(), EpollUDT.Opt.BOTH.code);
		SocketUDT.epollAdd0(epollID, peer2.id(), EpollUDT.Opt.BOTH.code);

		peer1.connect(addr2);
		peer2.connect(addr1);

		socketAwait(peer1, StatusUDT.CONNECTED);
		socketAwait(peer2, StatusUDT.CONNECTED);

		log.info("state 1 - rendezvous");
		log.info("peer1 : {}", peer1);
		log.info("peer2 : {}", peer2);

		{
			log.info("wait one");

			clear(readBuffer);
			clear(writeBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.TIMEOUT_INFINITE);

			log.info("readyCount : {}", readyCount);
			logBuffer("read: ", readBuffer);
			logBuffer("write:", writeBuffer);

			assertEquals(2, readyCount);
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(2, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
		}

		{
			log.info("wait two");

			clear(readBuffer);
			clear(writeBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.TIMEOUT_INFINITE);

			log.info("readyCount : {}", readyCount);
			logBuffer("read: ", readBuffer);
			logBuffer("write:", writeBuffer);

			assertEquals(2, readyCount);
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(2, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
		}

		peer1.close();
		peer2.close();

		SocketUDT.epollRelease0(epollID);

	}

}
