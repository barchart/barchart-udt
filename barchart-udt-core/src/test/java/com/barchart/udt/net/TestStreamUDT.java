/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;
import util.UnitHelp;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.StatusUDT;
import com.barchart.udt.TypeUDT;

/**
 * Test for UDT socket input streams and output streams.
 */
public class TestStreamUDT extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private interface ReadStrategy {

		int read(InputStream is, byte[] bytes, int off, int len)
				throws IOException;
	}

	private static final int DATA_ARRAY_SIZE = 23456;

	private static final int DEFAULT_BUFFER_SIZE = 6789;

	private static final byte[] TEST_BYTES = testBytes();

	static final ReadStrategy bulkReadStrategy = new ReadStrategy() {
		@Override
		public int read(final InputStream is, final byte[] bytes,
				final int off, final int len) throws IOException {

			return is.read(bytes, off, len);

		}
	};

	static final ReadStrategy singleReadStrategy = new ReadStrategy() {
		@Override
		public int read(final InputStream is, final byte[] bytes,
				final int off, final int len) throws IOException {

			final byte val = (byte) is.read();
			bytes[off] = val;

			// log.info("READ: val={}", val);

			return 1;

		}
	};

	@Test
	public void testBulkRead() throws Exception {

		genericInputOutputTest(bulkReadStrategy);

	}

	@Test
	public void testSingleRead() throws Exception {

		genericInputOutputTest(singleReadStrategy);

	}

	private void genericInputOutputTest(final ReadStrategy readStrategy)
			throws Exception {

		log.info("STARTED");

		final InetSocketAddress serverAddress = UnitHelp.localSocketAddress();

		startThreadedServer(serverAddress, readStrategy);

		//

		final SocketUDT clientSocket = new SocketUDT(TypeUDT.STREAM);

		final InetSocketAddress clientAddress = UnitHelp.localSocketAddress();

		clientSocket.bind(clientAddress);
		assertTrue("Socket not bound!!", clientSocket.isBound());

		clientSocket.connect(serverAddress);
		assertTrue("Socket not connected!", clientSocket.isConnected());

		final InputStream socketIn = new NetInputStreamUDT(clientSocket);
		final OutputStream socketOut = new NetOutputStreamUDT(clientSocket);

		// Thread.sleep(1000);

		//

		log.info("### COPY START");

		final InputStream dataIn = new ByteArrayInputStream(TEST_BYTES.clone());

		copy(dataIn, socketOut);

		// dataIn.close();

		log.info("### COPY OUT DONE");

		final ByteArrayOutputStream dataOut = new ByteArrayOutputStream();

		copy(socketIn, dataOut, TEST_BYTES.length, readStrategy);

		// dataOut.close();

		log.info("### COPY IN DONE");

		final byte[] bytesCopy = dataOut.toByteArray();

		assertTrue(Arrays.equals(TEST_BYTES, bytesCopy));

		// clientSocket.close();

	}

	private static byte[] testBytes() {

		final byte[] data = new byte[DATA_ARRAY_SIZE];

		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (i % 127);
			// data[i] = (byte) i;
		}

		return data;

	}

	static int copy(final InputStream is, final OutputStream os)
			throws IOException {

		if (is == null) {
			throw new NullPointerException("null input stream.");
		}

		if (os == null) {
			throw new NullPointerException("null output stream.");
		}

		final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

		int countTotal = 0;

		while (true) {

			final int countRead = is.read(buffer);

			if (countRead < 0) {
				break;
			}

			assert countRead > 0;

			os.write(buffer, 0, countRead);
			//
			// log.info("buffer[0]={}", buffer[0]);
			// log.info("buffer[1]={}", buffer[1]);
			// log.info("buffer[2]={}", buffer[2]);
			// log.info("countRead={}", countRead);

			countTotal += countRead;

		}

		log.info("### Wrote " + countTotal + " bytes.");

		return countTotal;

	}

	static long copy(final InputStream is, final OutputStream os,
			final int copyTotal, final ReadStrategy readStrategy)
			throws Exception {

		if (copyTotal < 0) {
			throw new IllegalArgumentException("Invalid byte count: "
					+ copyTotal);
		}

		final int arraySize;
		if (copyTotal < DEFAULT_BUFFER_SIZE) {
			arraySize = copyTotal;
		} else {
			arraySize = DEFAULT_BUFFER_SIZE;
		}

		final byte array[] = new byte[arraySize];

		int writeCount = 0;

		int pendingCount = copyTotal;

		try {

			while (pendingCount > 0) {

				// log.info("IN LOOP; pendingCount={}", pendingCount);

				final int readCount, readLimit;

				if (pendingCount < arraySize) {
					readLimit = pendingCount;
				} else {
					readLimit = arraySize;
				}
				readCount = readStrategy.read(is, array, 0, readLimit);
				assert readCount > 0;
				assert readCount <= readLimit;

				// log.info("DATA IN readCount={}", readCount);
				// log.info("@@@ array[0]={}", array[0]);
				// log.info("@@@ array[1]={}", array[1]);
				// log.info("@@@ array[2]={}", array[2]);

				pendingCount -= readCount;

				// log.info("Decrementing; readCount=" + readCount
				// + " pendingCount=" + pendingCount);

				os.write(array, 0, readCount);
				// log.info("DATA OUT");

				writeCount += readCount;

			}

			return writeCount;

		} catch (final Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			// os.flush();
		}

	}

	private void runTestServer(final InetSocketAddress serverAddress,
			final ReadStrategy readStrategy, final AtomicBoolean readyToAccept)
			throws Exception {

		log.info("STARTED");

		final SocketUDT acceptorSocket = new SocketUDT(TypeUDT.STREAM);

		acceptorSocket.bind(serverAddress);
		assertTrue("Acceptor should be bound", acceptorSocket.isBound());

		acceptorSocket.listen(1);
		assertEquals("Acceptor should be listenin", acceptorSocket.status(),
				StatusUDT.LISTENING);

		readyToAccept.set(true);
		synchronized (readyToAccept) {
			readyToAccept.notifyAll();
		}
		final SocketUDT connectorSocket = acceptorSocket.accept();
		assertTrue(connectorSocket.isBound());
		assertTrue(connectorSocket.isConnected());

		echo(connectorSocket, readStrategy);

	}

	private void startThreadedServer(final InetSocketAddress serverAddress,
			final ReadStrategy readStrategy) throws Exception {

		final AtomicBoolean readyToAccept = new AtomicBoolean(false);
		final Runnable runner = new Runnable() {
			@Override
			public void run() {
				// startServer();
				try {
					runTestServer(serverAddress, readStrategy, readyToAccept);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		};

		final Thread t = new Thread(runner, "### starter");
		t.setDaemon(true);
		t.start();

		synchronized (readyToAccept) {
			if (!readyToAccept.get()) {
				readyToAccept.wait(4000);
			}
		}
		assertTrue("Not ready to accept?", readyToAccept.get());
		Thread.yield();
		Thread.yield();
		Thread.yield();
		Thread.yield();
	}

	private void echo(final SocketUDT connectorSocket,
			final ReadStrategy readStrategy) {

		final InputStream is = new NetInputStreamUDT(connectorSocket);
		final OutputStream os = new NetOutputStreamUDT(connectorSocket);

		final Runnable runner = new Runnable() {
			@Override
			public void run() {
				try {

					log.info("### ECHO: START");

					copy(is, os, TEST_BYTES.length, readStrategy);

					log.info("### ECHO: FINISH");

					// os.close();

				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		};

		final Thread dt = new Thread(runner, "### server");
		dt.setDaemon(true);
		dt.start();

	}

}
