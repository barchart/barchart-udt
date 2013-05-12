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

import org.junit.Test;

import util.TestAny;

import com.barchart.udt.util.HelpUDT;

public class TestEpollConnection extends TestAny {

	static EpollUDT.Opt opt(final SocketUDT socket) throws ExceptionUDT {
		final int code = socket.getOption(OptionUDT.Epoll_Event_Mask);
		return EpollUDT.Opt.from(code);
	}

	static void logSize(final IntBuffer buffer) {
		log.info("# size read   = {}", buffer.get(SocketUDT.UDT_READ_INDEX));
		log.info("# size write  = {}", buffer.get(SocketUDT.UDT_WRITE_INDEX));
		log.info("# size except = {}", buffer.get(SocketUDT.UDT_EXCEPT_INDEX));
	}

	/**
	 * Verify how epoll reports connect/disconnect life cycle.
	 */
	@Test
	public void connectionCreateDelete() throws Exception {

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.setBlocking(false);
		accept.bind0(localSocketAddress());
		accept.listen0(1);

		socketAwait(accept, StatusUDT.LISTENING);
		log.info("accept {}", accept);

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.setBlocking(false);
		client.bind0(localSocketAddress());

		SocketUDT.epollAdd0(epollID, client.id(), EpollUDT.Opt.ALL.code);

		socketAwait(client, StatusUDT.OPENED);
		log.info("client {}", client);

		client.connect(accept.getLocalSocketAddress());

		socketAwait(client, StatusUDT.CONNECTED);
		log.info("client {}", client);

		{
			log.info("### 1 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			logSize(sizeBuffer);
			logBuffer("read ", readBuffer);
			logBuffer("write", writeBuffer);
			assertEquals("", 1, readyCount);
			assertTrue(socketPresent(client, writeBuffer));
			clear(readBuffer);
			clear(writeBuffer);
		}

		final SocketUDT server = accept.accept();
		assertNotNull(server);

		SocketUDT.epollAdd0(epollID, server.id(), EpollUDT.Opt.ALL.code);

		socketAwait(server, StatusUDT.CONNECTED);
		log.info("server {}", server);

		{
			log.info("### 2 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			logSize(sizeBuffer);
			logBuffer("read ", readBuffer);
			logBuffer("write", writeBuffer);
			assertEquals("client=write server=write", 2, readyCount);
			assertTrue(socketPresent(client, writeBuffer));
			assertTrue(socketPresent(server, writeBuffer));
			clear(readBuffer);
			clear(writeBuffer);
		}

		client.close();
		socketAwait(client, StatusUDT.CLOSED);
		socketAwait(server, StatusUDT.BROKEN);

		log.info("server opt {}", opt(server));

		log.info("client {}", client);
		log.info("server {}", server);

		{
			log.info("### 3 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			logSize(sizeBuffer);
			logBuffer("read ", readBuffer);
			logBuffer("write", writeBuffer);
			assertEquals("client=error server=write", 3, readyCount);
			assertTrue(socketPresent(client, readBuffer));
			assertTrue(socketPresent(client, writeBuffer));
			assertTrue(socketPresent(server, writeBuffer));
			clear(readBuffer);
			clear(writeBuffer);
		}

		log.info("client {}", client);
		log.info("server {}", server);

		{
			log.info("### 4 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			logSize(sizeBuffer);
			logBuffer("read ", readBuffer);
			logBuffer("write", writeBuffer);
			assertEquals("client=missing server=write", 1, readyCount);
			assertTrue(socketPresent(server, writeBuffer));
			clear(readBuffer);
			clear(writeBuffer);
		}

		server.close();
		socketAwait(server, StatusUDT.CLOSED);

		log.info("client {}", client);
		log.info("server {}", server);

		{
			log.info("### 5 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			logSize(sizeBuffer);
			logBuffer("read ", readBuffer);
			logBuffer("write", writeBuffer);
			assertEquals("server=error", 2, readyCount);
			assertTrue(socketPresent(server, readBuffer));
			assertTrue(socketPresent(server, writeBuffer));
			clear(readBuffer);
			clear(writeBuffer);
		}

		log.info("client {}", client);
		log.info("server {}", server);

		{
			log.info("### 6 ###");
			final int readyCount = SocketUDT.selectEpoll( //
					epollID, readBuffer, writeBuffer, sizeBuffer, 0);
			logSize(sizeBuffer);
			logBuffer("read ", readBuffer);
			logBuffer("write", writeBuffer);
			assertEquals("server=missing", 0, readyCount);
			clear(readBuffer);
			clear(writeBuffer);
		}

		log.info("client {}", client);
		log.info("server {}", server);

		accept.close();
		// UDT has 3 second close delay for acceptors.
		// socketAwait(accept, StatusUDT.CLOSED);

		SocketUDT.epollRelease0(epollID);

	}

}
