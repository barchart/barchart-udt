/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_23-19-15'
 *
 * Copyright (C) 2009, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
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

import com.barchart.udt.util.HelperUtils;

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

			InetSocketAddress bindServer = HelperUtils.getLocalSocketAddress();

			SocketUDT socketServer = new SocketUDT(TypeUDT.DATAGRAM);
			socketServer.configureBlocking(false);
			socketServer.bind(bindServer);
			socketServer.listen(1);
			log.info("socketServer: {}", socketServer.socketID);

			InetSocketAddress bindClient = HelperUtils.getLocalSocketAddress();

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
