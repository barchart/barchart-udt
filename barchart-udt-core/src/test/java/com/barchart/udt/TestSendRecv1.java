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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSendRecv1 extends TestSendRecvAbstract<byte[]> {

	final static Logger log = LoggerFactory.getLogger(TestSendRecv1.class);

	final static int POSITION = 1234;
	final static int LIMIT = POSITION + SIZE;
	final static int CAPACITY = 3 * 1024;

	@Override
	protected void doClientReader() throws Exception {

		// blocks here
		byte[] arraySent = clientQueue.take();

		byte[] arrayReceived = new byte[CAPACITY];

		// blocks here
		int size = client.receive(arrayReceived, POSITION, LIMIT);
		assertEquals(size, SIZE);

		byte[] dataSent = new byte[SIZE];
		byte[] dataReceived = new byte[SIZE];

		System.arraycopy(//
				arraySent, POSITION, dataSent, 0, SIZE);
		System.arraycopy(//
				arrayReceived, POSITION, dataReceived, 0, SIZE);

		assertTrue(Arrays.equals(dataSent, dataReceived));
	}

	@Override
	protected void doClientWriter() throws Exception {

		byte[] array = new byte[CAPACITY];

		generator.nextBytes(array);

		// blocks here
		int size = client.send(array, POSITION, LIMIT);
		assertEquals(size, SIZE);

		clientQueue.put(array);

	}

	@Override
	protected void doServerReader() throws Exception {

		byte[] array = new byte[CAPACITY];

		// blocks here
		int size = connector.receive(array, POSITION, LIMIT);
		assertEquals(size, SIZE);

		serverQueue.put(array);

	}

	@Override
	protected void doServerWriter() throws Exception {

		// blocks here
		byte[] array = serverQueue.take();

		// blocks here
		int size = connector.send(array, POSITION, LIMIT);
		assertEquals(size, SIZE);

	}

}
