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

import com.barchart.udt.util.HelpUDT;

public class TestEpollWait extends TestAny {

	/** explore read/write */
	@Test(timeout = 5 * 1000)
	public void epollWait0_Accept0() throws Exception {

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.setBlocking(true);
		accept.bind0(localSocketAddress());

		SocketUDT.epollAdd0(epollID, accept.id(), EpollUDT.Opt.BOTH.code);

		socketAwait(accept, StatusUDT.OPENED);
		log.info("accept OPENED");

		{
			// no events
		}

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.setBlocking(true);
		client.bind0(localSocketAddress());

		SocketUDT.epollAdd0(epollID, client.id(), EpollUDT.Opt.BOTH.code);

		socketAwait(client, StatusUDT.OPENED);
		log.info("client OPENED");

		{
			// no events
		}

		accept.listen0(1);

		socketAwait(accept, StatusUDT.LISTENING);
		log.info("accept LISTENING");

		{
			// no events
		}

		client.connect0(accept.getLocalSocketAddress());

		socketAwait(client, StatusUDT.CONNECTED);
		log.info("client CONNECTED");

		{

			// accept: r/w
			// client: w

			clear(readBuffer);
			clear(writeBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.TIMEOUT_INFINITE);

			log.info("readyCount : {}", readyCount);

			assertEquals(3, readyCount);
			assertEquals(1, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(2, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
			assertTrue(socketPresent(accept, readBuffer));
			assertTrue(socketPresent(accept, writeBuffer));
			assertTrue(socketPresent(client, writeBuffer));

		}

		final SocketUDT server = accept.accept0();
		assertNotNull(server);
		server.setBlocking(true);
		SocketUDT.epollAdd0(epollID, server.id(), EpollUDT.Opt.BOTH.code);

		socketAwait(server, StatusUDT.CONNECTED);
		log.info("server CONNECTED");

		{

			// accept: w
			// client: w
			// server: w

			clear(readBuffer);
			clear(writeBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.TIMEOUT_INFINITE);

			log.info("readyCount : {}", readyCount);

			assertEquals(3, readyCount);
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(3, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
			assertTrue(socketPresent(accept, writeBuffer));
			assertTrue(socketPresent(client, writeBuffer));
			assertTrue(socketPresent(server, writeBuffer));

		}

		final int testSize = 3;
		final int sendCount = client.send(new byte[testSize]);
		assertEquals(testSize, sendCount);

		Thread.sleep(1000); // FIXME test can time out

		{

			// accept: w
			// client: w
			// server: r/w

			clear(readBuffer);
			clear(writeBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.TIMEOUT_INFINITE);

			log.info("readyCount : {}", readyCount);

			assertEquals(4, readyCount);
			assertEquals(1, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(3, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
			assertTrue(socketPresent(server, readBuffer));
			assertTrue(socketPresent(accept, writeBuffer));
			assertTrue(socketPresent(client, writeBuffer));
			assertTrue(socketPresent(server, writeBuffer));

			logBuffer("read ", readBuffer);
			logBuffer("write", writeBuffer);

		}

		final int recvCount = server.receive(new byte[10]);
		assertEquals(testSize, recvCount);

		server.close();
		client.close();
		accept.close();

		SocketUDT.epollRelease0(epollID);

	}

	/** explore read only */
	@Test(timeout = 5 * 1000)
	public void epollWait0_Accept1() throws Exception {

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.setBlocking(true);
		accept.bind0(localSocketAddress());
		accept.listen0(1);
		socketAwait(accept, StatusUDT.LISTENING);
		log.info("accept listen : {}", accept.id());

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.setBlocking(true);
		client.bind0(localSocketAddress());

		SocketUDT.epollAdd0(epollID, accept.id(), EpollUDT.Opt.READ.code);
		SocketUDT.epollAdd0(epollID, client.id(), EpollUDT.Opt.NONE.code);

		// SocketUDT
		// .epollUpdate0(epollID, accept.socketID, EpollUDT.Opt.NONE.code);
		// SocketUDT
		// .epollUpdate0(epollID, client.socketID, EpollUDT.Opt.NONE.code);

		client.connect0(accept.getLocalSocketAddress());

		socketAwait(client, StatusUDT.CONNECTED);
		log.info("client connect : {}", client.id());

		{
			// accept : r
			// client : none

			clear(readBuffer);
			clear(readBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.TIMEOUT_INFINITE);

			log.info("readyCount : {}", readyCount);
			logBuffer("read: ", readBuffer);
			logBuffer("write:", writeBuffer);

			assertEquals(1, readyCount);
			assertEquals(1, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
			assertTrue(socketPresent(accept, readBuffer));

		}

		final SocketUDT server = accept.accept0();
		assertNotNull(server);
		server.setBlocking(true);
		SocketUDT.epollAdd0(epollID, server.id(), EpollUDT.Opt.ERROR.code);

		socketAwait(server, StatusUDT.CONNECTED);
		log.info("server connect : {}", server.id());

		{

			// accept : none
			// client : none
			// server : none

			clear(readBuffer);
			clear(readBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, 1000);

			assertEquals(0, readyCount);
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));

		}

		server.close();
		client.close();
		accept.close();

		SocketUDT.epollRelease0(epollID);

	}

	/**
	 * fixed in SocketuUDT.cpp
	 * <p>
	 * http://udt.sourceforge.net/udt4/doc/epoll.htm
	 * <p>
	 * "Finally, for epoll_wait, negative timeout value will make the function
	 * to waituntil an event happens. If the timeout value is 0, then the
	 * function returns immediately with any sockets associated an IO event. If
	 * timeout occurs before any event happens, the function returns 0."
	 * 
	 */
	@Test
	public void epollWait0_ZeroTimeout() throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final long millisTimeout = 0;

		final int readyCount = SocketUDT.epollWait0( //
				epollID, readBuffer, writeBuffer, sizeBuffer, millisTimeout);

		assertEquals(0, readyCount);

		assertEquals(0, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
		assertEquals(0, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
		assertEquals(0, sizeBuffer.get(SocketUDT.UDT_EXCEPT_INDEX));

		SocketUDT.epollRelease0(epollID);

	}

	@Test(expected = ExceptionUDT.class, timeout = 3 * 1000)
	public void epollWati0_Exception() throws Exception {

		final int epollID = -1; // invalid

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);

		final IntBuffer sizeBuffer = HelpUDT
				.newDirectIntBufer(SocketUDT.UDT_SIZE_COUNT);

		final long millisTimeout = 1 * 1000;

		SocketUDT.epollWait0( //
				epollID, readBuffer, writeBuffer, sizeBuffer, millisTimeout);

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/** emulate jdk selector */
	@Test(timeout = 5 * 1000)
	public void epollWait0_Accept2() throws Exception {

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.setBlocking(true);
		accept.bind0(localSocketAddress());
		accept.listen0(10);
		socketAwait(accept, StatusUDT.LISTENING);
		log.info("accept : {}", accept);

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.setBlocking(true);
		client.bind0(localSocketAddress());

		client.connect0(accept.getLocalSocketAddress());

		socketAwait(client, StatusUDT.CONNECTED);
		log.info("client {}", client);

		final SocketUDT server = accept.accept();
		socketAwait(server, StatusUDT.CONNECTED);
		log.info("server {}", server);

		SocketUDT.epollAdd0(epollID, accept.id(), EpollUDT.Opt.READ.code);

		SocketUDT.epollAdd0(epollID, client.id(), EpollUDT.Opt.BOTH.code);
		SocketUDT.epollAdd0(epollID, server.id(), EpollUDT.Opt.BOTH.code);

		{

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

		SocketUDT.epollRemove0(epollID, client.id());
		SocketUDT.epollRemove0(epollID, server.id());

		{

			clear(readBuffer);
			clear(writeBuffer);

			final int readyCount = SocketUDT.selectEpoll(epollID, readBuffer,
					writeBuffer, sizeBuffer, 0);

			log.info("readyCount : {}", readyCount);
			logBuffer("read: ", readBuffer);
			logBuffer("write:", writeBuffer);

			assertEquals(0, readyCount);
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));

		}

		SocketUDT.epollAdd0(epollID, client.id(), EpollUDT.Opt.BOTH.code);
		SocketUDT.epollAdd0(epollID, server.id(), EpollUDT.Opt.BOTH.code);

		{

			clear(readBuffer);
			clear(writeBuffer);

			final int readyCount = SocketUDT.selectEpoll(epollID, readBuffer,
					writeBuffer, sizeBuffer, 0);

			log.info("readyCount : {}", readyCount);
			logBuffer("read: ", readBuffer);
			logBuffer("write:", writeBuffer);

			assertEquals(2, readyCount);
			assertEquals(0, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
			assertEquals(2, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));

		}

		server.close();
		client.close();
		accept.close();

		SocketUDT.epollRelease0(epollID);

	}

}
