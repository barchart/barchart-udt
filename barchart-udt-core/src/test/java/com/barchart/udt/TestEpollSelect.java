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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.TestHelp;

public class TestEpollSelect {

	static final Logger log = LoggerFactory.getLogger(TestEpollSelect.class);

	@Before
	public void init() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void done() throws Exception {
	}

	/**
	 * FIXME "terminate called after throwing an instance of 'CUDTException'"
	 */
	@Ignore
	@Test
	public void testEpollSelect() {

		try {

			log.info("testEpollSelect");

			final int epollID = SocketUDT.epollCreate0();

			final InetSocketAddress bindServer = TestHelp
					.getLocalSocketAddress();

			final SocketUDT socketServer = new SocketUDT(TypeUDT.DATAGRAM);
			socketServer.configureBlocking(false);
			socketServer.bind(bindServer);
			socketServer.listen(1);
			log.info("socketServer: {}", socketServer.socketID);

			final InetSocketAddress bindClient = TestHelp
					.getLocalSocketAddress();

			final SocketUDT socketClient = new SocketUDT(TypeUDT.DATAGRAM);
			socketClient.configureBlocking(false);
			socketClient.bind(bindClient);
			log.info("socketClient: {}", socketClient.socketID);

			//

			final IntBuffer readBuffer = SocketUDT.newDirectIntBufer(10);
			final IntBuffer writeBuffer = SocketUDT.newDirectIntBufer(10);
			final IntBuffer exceptBuffer = SocketUDT.newDirectIntBufer(10);

			final IntBuffer sizeBuffer = SocketUDT
					.newDirectIntBufer(SocketUDT.UDT_SIZE_COUNT);
			final long millisTimeout = 1 * 1000;

			//

			SocketUDT.epollAdd0(epollID, socketServer.socketID);
			SocketUDT.epollAdd0(epollID, socketClient.socketID);

			socketClient.connect(bindServer);

			final long timeStart = System.currentTimeMillis();

			final int result = SocketUDT.epollWait0(epollID, readBuffer,
					writeBuffer, exceptBuffer, sizeBuffer, millisTimeout);

			final long timeFinish = System.currentTimeMillis();

			//

			final int readSize = sizeBuffer.get(SocketUDT.UDT_READ_INDEX);
			final int writeSize = sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX);
			final int exceptSize = sizeBuffer.get(SocketUDT.UDT_EXCEPT_INDEX);

			log.info("result: {}", result);
			log.info("readSize: {}", readSize);
			log.info("writeSize: {}", writeSize);
			log.info("exceptSize: {}", exceptSize);

			final long timeDiff = timeFinish - timeStart;
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

			SocketUDT.epollRelease0(epollID);

		} catch (final Throwable e) {

			log.error("", e);

			fail(e.getMessage());

		}

	}

	@Test(expected = ExceptionUDT.class)
	public void testEpollException() throws Exception {

		final int epollID = -1; // invalid

		final IntBuffer readBuffer = SocketUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = SocketUDT.newDirectIntBufer(10);
		final IntBuffer exceptBuffer = SocketUDT.newDirectIntBufer(10);

		final IntBuffer sizeBuffer = SocketUDT
				.newDirectIntBufer(SocketUDT.UDT_SIZE_COUNT);
		final long millisTimeout = 1 * 1000;

		final int result = SocketUDT.epollWait0(epollID, readBuffer,
				writeBuffer, exceptBuffer, sizeBuffer, millisTimeout);

	}

}
