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

import java.nio.IntBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;
import util.UnitHelp;

import com.barchart.udt.util.HelpUDT;

public class TestEpollClose extends TestAny {

	/**
	 * Verify how closed socket is reported by epoll.
	 */
	@Test
	public void epollWait0_Close() throws Exception {

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.setBlocking(true);
		accept.bind0(localSocketAddress());
		accept.listen0(1);

		socketAwait(accept, StatusUDT.LISTENING);
		log.info("accept {}", accept);

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.setBlocking(true);
		client.bind0(localSocketAddress());

		SocketUDT.epollAdd0(epollID, client.id(), EpollUDT.Opt.BOTH.code);

		socketAwait(client, StatusUDT.OPENED);
		log.info("client {} {}", client,
				client.getOption(OptionUDT.Epoll_Event_Mask));

		client.connect(accept.getLocalSocketAddress());

		socketAwait(client, StatusUDT.CONNECTED);
		log.info("client {} {}", client,
				client.getOption(OptionUDT.Epoll_Event_Mask));

		{
			log.info("### 1 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			assertEquals("", 1, readyCount);
			UnitHelp.logBuffer("read ", readBuffer);
			UnitHelp.logBuffer("write", writeBuffer);
			UnitHelp.clear(readBuffer);
			UnitHelp.clear(writeBuffer);
		}

		client.close();

		socketAwait(client, StatusUDT.CLOSED);
		log.info("client {} {}", client, 0);

		SocketUDT.epollRemove0(epollID, client.id());

		{
			log.info("### 2 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			assertEquals("", 0, readyCount);
			UnitHelp.logBuffer("read ", readBuffer);
			UnitHelp.logBuffer("write", writeBuffer);
			UnitHelp.clear(readBuffer);
			UnitHelp.clear(writeBuffer);
		}

		socketAwait(client, StatusUDT.NONEXIST);
		log.info("client {} {}", client, 0);

		{
			log.info("### 3 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			assertEquals("", 0, readyCount);
			UnitHelp.logBuffer("read ", readBuffer);
			UnitHelp.logBuffer("write", writeBuffer);
			UnitHelp.clear(readBuffer);
			UnitHelp.clear(writeBuffer);
		}

		accept.close();

		SocketUDT.epollRelease0(epollID);

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
