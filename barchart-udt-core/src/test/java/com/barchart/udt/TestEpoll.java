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
import java.nio.IntBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.TestHelp;

public class TestEpoll {

	static final Logger log = LoggerFactory.getLogger(TestEpoll.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * TODO produces: "terminate called after throwing an instance of
	 * 'CUDTException'" when running in with maven-invoker-plugin
	 */
	// @Test
	public void testEpoll() {

		try {

			log.info("testEpoll");

			int epollID = SocketUDT.epollCreate();

			InetSocketAddress bindServer = TestHelp.getLocalSocketAddress();

			SocketUDT socketServer = new SocketUDT(TypeUDT.DATAGRAM);
			socketServer.configureBlocking(false);
			socketServer.bind(bindServer);
			socketServer.listen(1);
			log.info("socketServer: {}", socketServer.socketID);

			InetSocketAddress bindClient = TestHelp.getLocalSocketAddress();

			SocketUDT socketClient = new SocketUDT(TypeUDT.DATAGRAM);
			socketClient.configureBlocking(false);
			socketClient.bind(bindClient);
			log.info("socketClient: {}", socketClient.socketID);

			//

			IntBuffer readBuffer = SocketUDT.newDirectIntBufer(10);
			IntBuffer writeBuffer = SocketUDT.newDirectIntBufer(10);
			IntBuffer exceptBuffer = SocketUDT.newDirectIntBufer(10);

			IntBuffer sizeBuffer = SocketUDT
					.newDirectIntBufer(SocketUDT.UDT_SIZE_COUNT);
			long millisTimeout = 1 * 1000;

			//

			SocketUDT.epollAdd(epollID, socketServer.socketID);
			SocketUDT.epollAdd(epollID, socketClient.socketID);

			socketClient.connect(bindServer);

			long timeStart = System.currentTimeMillis();

			int result = SocketUDT.epollWait(epollID, readBuffer, writeBuffer,
					exceptBuffer, sizeBuffer, millisTimeout);

			long timeFinish = System.currentTimeMillis();

			//

			int readSize = sizeBuffer.get(SocketUDT.UDT_READ_INDEX);
			int writeSize = sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX);
			int exceptSize = sizeBuffer.get(SocketUDT.UDT_EXCEPT_INDEX);
			log.info("result: {}", result);
			log.info("readSize: {}", readSize);
			log.info("writeSize: {}", writeSize);
			log.info("exceptSize: {}", exceptSize);

			long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			for (int k = 0; k < readSize; k++) {
				log.info("read ready: {}", readBuffer.get(k));
			}

			for (int k = 0; k < writeSize; k++) {
				log.info("write ready: {}", writeBuffer.get(k));
			}

			for (int k = 0; k < exceptSize; k++) {
				log.info("except ready: {}", exceptBuffer.get(k));
			}

			socketServer.close();
			socketClient.close();

			SocketUDT.epollRelease(epollID);

		} catch (Throwable e) {

			log.error("", e);

			fail(e.getMessage());

		}

	}

	@Test(expected = ExceptionUDT.class)
	public void testEpollException() throws Exception {

		int epollID = -1; // invalid

		IntBuffer readBuffer = SocketUDT.newDirectIntBufer(10);
		IntBuffer writeBuffer = SocketUDT.newDirectIntBufer(10);
		IntBuffer exceptBuffer = SocketUDT.newDirectIntBufer(10);

		IntBuffer sizeBuffer = SocketUDT
				.newDirectIntBufer(SocketUDT.UDT_SIZE_COUNT);
		long millisTimeout = 1 * 1000;

		int result = SocketUDT.epollWait(epollID, readBuffer, writeBuffer,
				exceptBuffer, sizeBuffer, millisTimeout);

	}

}
