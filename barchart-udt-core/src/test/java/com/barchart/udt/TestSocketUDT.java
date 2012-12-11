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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.TestHelp;

public class TestSocketUDT {

	static final Logger log = LoggerFactory.getLogger(TestSocketUDT.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSelectEx0() {

		log.info("testSelectEx0");

		try {

			InetSocketAddress localAddress1 = TestHelp
					.getLocalSocketAddress();

			InetSocketAddress localAddress2 = TestHelp
					.getLocalSocketAddress();

			SocketUDT socketServer = new SocketUDT(TypeUDT.DATAGRAM);
			socketServer.setOption(OptionUDT.UDT_RCVSYN, false);
			socketServer.setOption(OptionUDT.UDT_SNDSYN, false);
			socketServer.bind(localAddress1);
			socketServer.listen(1);
			// socketServer.accept();

			SocketUDT socketClient = new SocketUDT(TypeUDT.DATAGRAM);
			socketClient.setOption(OptionUDT.UDT_RCVSYN, false);
			socketClient.setOption(OptionUDT.UDT_SNDSYN, false);
			socketClient.bind(localAddress2);
			socketClient.listen(1);
			// socketClient.accept();

			long timeout = 1 * 1000 * 1000;

			SocketUDT[] selectArray = new SocketUDT[] { socketServer,
					socketClient };

			socketServer.clearError();

			long timeStart = System.currentTimeMillis();

			// SocketUDT.selectExtended(selectArray, timeout);

			long timeFinish = System.currentTimeMillis();

			long timeDiff = timeFinish - timeStart;
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

		} catch (Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	@Test(expected = ExceptionUDT.class)
	public void testInvalidClose0() throws ExceptionUDT {

		SocketUDT socket = null;

		try {

			socket = new SocketUDT(TypeUDT.DATAGRAM);

		} catch (ExceptionUDT e) {

			fail("SocketException; " + e.getMessage());

		}

		final int realID = socket.socketID;

		final int fakeID = realID + 123;

		log.info("real: {} ; fake : {} ; ", realID, fakeID);

		// throws exception
		socket.testInvalidClose0(fakeID);

	}

	@Test
	public void testIsOpen() throws Exception {

		SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertTrue(socket.isOpen());

		socket.setOption(OptionUDT.Is_Receive_Synchronous, false);
		socket.setOption(OptionUDT.Is_Send_Synchronous, false);
		assertTrue(socket.isOpen());

		final InetSocketAddress localSocketAddress = TestHelp
				.getLocalSocketAddress();

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

	@Test
	// no exceptions is pass
	public void testEpollCreate() throws Exception {

		int epollID = SocketUDT.epollCreate();
		SocketUDT.epollRelease(epollID);

	}

	@Test(expected = ExceptionUDT.class)
	public void testEpollRelease() throws Exception {

		SocketUDT.epollRelease(-1);

	}

	@Test
	// no exceptions is pass
	public void testEpollAddRemove() throws Exception {

		SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		//

		final int epollID = SocketUDT.epollCreate();

		SocketUDT.epollAdd(epollID, socket.socketID);

		SocketUDT.epollRemove(epollID, socket.socketID);

		SocketUDT.epollRelease(epollID);

	}

	@Test(expected = ExceptionUDT.class)
	public void testEpollAddRemoveException1() throws Exception {

		final int epollID = SocketUDT.epollCreate();

		SocketUDT.epollAdd(epollID, -1);

		SocketUDT.epollRelease(epollID);

	}

	@Test
	// (expected = ExceptionUDT.class)
	public void testEpollAddRemoveException2() throws Exception {

		final int epollID = SocketUDT.epollCreate();

		SocketUDT.epollRemove(epollID, -1);

		SocketUDT.epollRelease(epollID);

	}

}
