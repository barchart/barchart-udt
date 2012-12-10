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

import static com.barchart.udt.util.HelperUtils.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestOption {

	Logger log = LoggerFactory.getLogger(TestOption.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOptionBasic() {

		try {

			SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			OptionUDT option;

			option = OptionUDT.UDT_SNDSYN;
			boolean booleanValue;
			booleanValue = true;
			socket.setOption(option, booleanValue);
			assertEquals(booleanValue, socket.getOption(option));
			booleanValue = false;
			socket.setOption(option, booleanValue);
			assertEquals(booleanValue, socket.getOption(option));

			log.info("pass: boolean");

			option = OptionUDT.UDP_RCVBUF;
			int intValue;
			intValue = 123456789;
			socket.setOption(option, intValue);
			assertEquals(intValue, socket.getOption(option));
			intValue = 987654321;
			socket.setOption(option, intValue);
			assertEquals(intValue, socket.getOption(option));

			log.info("pass: int");

			option = OptionUDT.UDT_MAXBW;
			long longValue;
			longValue = 1234567890123456789L;
			socket.setOption(option, longValue);
			assertEquals(longValue, socket.getOption(option));
			longValue = 8765432109876543210L;
			socket.setOption(option, longValue);
			assertEquals(longValue, socket.getOption(option));

			log.info("pass: long");

		} catch (Exception e) {
			fail("SocketException; " + e.getMessage());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOptionLingerRange1() throws ExceptionUDT {
		new LingerUDT(12345678);
	}

	@Test
	public void testOptionLingerRange2() throws ExceptionUDT {
		new LingerUDT(-12345678);
		new LingerUDT(0);
		new LingerUDT(65535);
	}

	@Test
	public void testOptionLinger() {

		try {

			SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			OptionUDT option = OptionUDT.UDT_LINGER;

			LingerUDT linger1 = new LingerUDT(65432);
			socket.setOption(option, linger1);
			assertEquals(linger1, socket.getOption(option));

			LingerUDT linger2 = new LingerUDT(-12345);
			socket.setOption(option, linger2);
			assertEquals(LingerUDT.LINGER_ZERO, socket.getOption(option));

			log.info("pass: linger");

		} catch (Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	@Test
	public void testOptionsPrint() {
		try {

			SocketUDT serverSocket = new SocketUDT(TypeUDT.DATAGRAM);
			InetSocketAddress serverAddress = getLocalSocketAddress();
			serverSocket.bind(serverAddress);
			serverSocket.listen(1);
			assertTrue(serverSocket.isBound());

			SocketUDT clientSocket = new SocketUDT(TypeUDT.DATAGRAM);
			InetSocketAddress clientAddress = getLocalSocketAddress();
			clientSocket.bind(clientAddress);
			assertTrue(clientSocket.isBound());

			clientSocket.connect(serverAddress);
			assertTrue(clientSocket.isConnected());

			SocketUDT acceptSocket = serverSocket.accept();
			assertTrue(acceptSocket.isConnected());

			log.info("client options:{}", clientSocket.toStringOptions());
			log.info("accept options:{}", acceptSocket.toStringOptions());

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

}
