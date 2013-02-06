/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestSendRecv2 extends TestSendRecvAbstract<ByteBuffer> {

	final static int POSITION = 1234;
	final static int LIMIT = POSITION + SIZE;
	final static int CAPACITY = 3 * 1024;

	@Override
	protected void doClientReader() throws Exception {

		// blocks here
		final ByteBuffer bufferSent = clientQueue.take();

		final ByteBuffer bufferReceived = ByteBuffer.allocateDirect(CAPACITY);
		bufferReceived.position(POSITION);
		bufferReceived.limit(LIMIT);

		// blocks here
		final int size = client.receive(bufferReceived);
		assertEquals(size, SIZE);

		final byte[] dataSent = new byte[SIZE];
		final byte[] dataReceived = new byte[SIZE];

		bufferSent.position(POSITION);
		bufferSent.get(dataSent);

		bufferReceived.position(POSITION);
		bufferReceived.get(dataReceived);

		assertTrue(Arrays.equals(dataSent, dataReceived));

	}

	@Override
	protected void doClientWriter() throws Exception {

		final byte[] array = new byte[SIZE];

		generator.nextBytes(array);

		final ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);

		buffer.position(POSITION);
		buffer.put(array);

		buffer.position(POSITION);
		buffer.limit(LIMIT);

		// blocks here
		final int size = client.send(buffer);
		assertEquals(size, SIZE);

		assertEquals(buffer.position(), buffer.limit());
		assertEquals(LIMIT, buffer.limit());

		clientQueue.put(buffer);

	}

	@Override
	protected void doServerReader() throws Exception {

		final ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
		buffer.position(POSITION);
		buffer.limit(LIMIT);

		// blocks here
		final int size = connector.receive(buffer);
		assertEquals(size, SIZE);

		assertEquals(buffer.position(), buffer.limit());
		assertEquals(LIMIT, buffer.limit());

		serverQueue.put(buffer);

	}

	@Override
	protected void doServerWriter() throws Exception {

		// blocks here
		final ByteBuffer buffer = serverQueue.take();
		buffer.position(POSITION);
		buffer.limit(LIMIT);

		// blocks here
		final int size = connector.send(buffer);
		assertEquals(size, SIZE);

		assertEquals(buffer.position(), buffer.limit());
		assertEquals(LIMIT, buffer.limit());

	}

}
