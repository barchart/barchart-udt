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
	@Test
	public void epollWait0_Accept0() throws Exception {

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.configureBlocking(true);
		accept.bind0(localSocketAddress());

		SocketUDT.epollAdd0(epollID, accept.socketID, EpollUDT.Opt.BOTH.code);

		socketAwait(accept, StatusUDT.OPENED);
		log.info("accept OPENED");

		{
			// no events
		}

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.configureBlocking(true);
		client.bind0(localSocketAddress());

		SocketUDT.epollAdd0(epollID, client.socketID, EpollUDT.Opt.BOTH.code);

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
			clear(readBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.INFINITE_TIMEOUT);

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
		server.configureBlocking(true);
		SocketUDT.epollAdd0(epollID, server.socketID, EpollUDT.Opt.BOTH.code);

		socketAwait(server, StatusUDT.CONNECTED);
		log.info("server CONNECTED");

		{

			// accept: w
			// client: w
			// server: w

			clear(readBuffer);
			clear(readBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.INFINITE_TIMEOUT);

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
			clear(readBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.INFINITE_TIMEOUT);

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
	@Test
	public void epollWait0_Accept1() throws Exception {

		final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.configureBlocking(true);
		accept.bind0(localSocketAddress());
		accept.listen0(1);
		socketAwait(accept, StatusUDT.LISTENING);
		log.info("accept listen : {}", accept.getSocketId());

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.configureBlocking(true);
		client.bind0(localSocketAddress());

		SocketUDT.epollAdd0(epollID, accept.socketID, EpollUDT.Opt.READ.code);
		SocketUDT.epollAdd0(epollID, client.socketID, EpollUDT.Opt.NONE.code);

		// SocketUDT
		// .epollUpdate0(epollID, accept.socketID, EpollUDT.Opt.NONE.code);
		// SocketUDT
		// .epollUpdate0(epollID, client.socketID, EpollUDT.Opt.NONE.code);

		client.connect0(accept.getLocalSocketAddress());

		socketAwait(client, StatusUDT.CONNECTED);
		log.info("client connect : {}", client.getSocketId());

		{
			// accept : r
			// client : none

			clear(readBuffer);
			clear(readBuffer);

			final int readyCount = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, sizeBuffer, SocketUDT.INFINITE_TIMEOUT);

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
		server.configureBlocking(true);
		SocketUDT.epollAdd0(epollID, server.socketID, EpollUDT.Opt.NONE.code);

		socketAwait(server, StatusUDT.CONNECTED);
		log.info("server connect : {}", server.getSocketId());

		{

			// accept : none
			// client : none
			// server : none

			clear(readBuffer);
			clear(readBuffer);

			try {

				final int readyCount = SocketUDT.epollWait0(epollID,
						readBuffer, writeBuffer, sizeBuffer, 1000);

				fail("must throw");

			} catch (final ExceptionUDT e) {

				assertEquals(e.getError(), ErrorUDT.ETIMEOUT);

			}

		}

		server.close();
		client.close();
		accept.close();

		SocketUDT.epollRelease0(epollID);

	}

	/**
	 * NOT TRUE
	 * 
	 * "Finally, for epoll_wait, negative timeout value will make the function
	 * to waituntil an event happens. If the timeout value is 0, then the
	 * function returns immediately with any sockets associated an IO event. If
	 * timeout occurs before any event happens, the function returns 0."
	 * 
	 * @throws Exception
	 */
	@Test
	public void epollWait0_ZeroTimeout() throws Exception {

		try {

			final int epollID = SocketUDT.epollCreate0();

			final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(10);
			final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(10);
			final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(10);
			final long millisTimeout = 0;

			SocketUDT.epollWait0(epollID, readBuffer, writeBuffer, sizeBuffer,
					millisTimeout);

			SocketUDT.epollRelease0(epollID);

		} catch (final ExceptionUDT e) {

			if (e.getError() == ErrorUDT.ETIMEOUT) {
				return;
			} else {
				throw e;
			}

		}

	}

	@Test(expected = ExceptionUDT.class)
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

}
