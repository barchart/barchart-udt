/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import static java.nio.channels.SelectionKey.*;
import static org.junit.Assert.*;
import static util.UnitHelp.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import util.TestAny;

/**
 * basic single thread selector test
 */
public class TestSelectorUDT extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	protected static final int SIZE = 1460;

	protected static final int COUNT = 1000;

	final SelectorProvider provider = SelectorProviderUDT.DATAGRAM;

	volatile SelectionKey acceptKey;
	volatile SelectionKey serverKey;
	volatile SelectionKey clientKey;

	volatile ServerSocketChannel acceptChannel;
	volatile SocketChannel serverChannel;
	volatile SocketChannel clientChannel;

	volatile Selector selector;

	volatile SocketAddress acceptorAddress;
	volatile SocketAddress clientAddress;

	@Before
	public void init() throws Exception {

		selector = provider.openSelector();

		acceptChannel = provider.openServerSocketChannel();
		acceptChannel.configureBlocking(false);
		acceptorAddress = localSocketAddress();
		acceptChannel.socket().bind(acceptorAddress);

		acceptKey = acceptChannel.register(selector, OP_ACCEPT);

		clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(false);
		clientAddress = localSocketAddress();
		clientChannel.socket().bind(clientAddress);

		clientKey = clientChannel.register(selector, OP_CONNECT);

		clientChannel.connect(acceptorAddress);

		log.info("test init");

	}

	@After
	public void done() throws Exception {

		log.info("test done");

	}

	volatile boolean isTestON = true;

	/** single pass state machine */
	/** FIXME */
	@Ignore
	@Test
	public void testSelect() {
		try {

			final Set<SelectionKey> selectedKeySet = selector.selectedKeys();

			selectLoop: while (isTestON) {

				final long timeout = 100;

				final int readyCount = selector.select(timeout);

				if (readyCount == 0) {
					continue selectLoop;
				}

				keyLoop: for (final SelectionKey key : selectedKeySet) {
					if (!key.isValid()) {
						continue keyLoop;
					}
					if (key.isReadable()) {
						doRead(key);
					}
					if (key.isWritable()) {
						doWrite(key);
					}
					if (key.isAcceptable()) {
						doAccept(key);
					}
					if (key.isConnectable()) {
						doConnect(key);
					}
				}

				selectedKeySet.clear();

			}

			log.info("before close");

			selector.close();

			log.info("after close");

		} catch (final Exception e) {
			fail(e.getMessage());
		}

	}

	interface Handler {

		void handleRead();

		void handleWrite();

	}

	final Queue<byte[]> serverQueue = new ConcurrentLinkedQueue<byte[]>();

	final Handler serverHandler = new Handler() {

		@Override
		public String toString() {
			return "serverHandler;" + " serverQueue:" + serverQueue.size()
					+ " serverChannel:" + serverChannel;
		}

		final ByteBuffer readerBuffer = ByteBuffer.allocateDirect(SIZE);

		@Override
		public void handleRead() {
			try {
				while (true) {

					readerBuffer.clear();

					final int readSize = serverChannel.read(readerBuffer);

					if (readSize <= 0) {
						return;
					}

					assertEquals(readSize, SIZE);

					final byte[] array = new byte[readSize];

					readerBuffer.flip();
					readerBuffer.get(array);

					serverQueue.offer(array);

					serverKey.interestOps( //
							serverKey.interestOps() | OP_WRITE);

				}
			} catch (final Exception e) {
				fail(e.getMessage());
			}
		}

		final ByteBuffer writerBuffer = ByteBuffer.allocateDirect(SIZE);

		@Override
		public void handleWrite() {
			try {

				byte[] array;

				while ((array = serverQueue.poll()) != null) {

					writerBuffer.clear();
					writerBuffer.put(array);
					writerBuffer.flip();

					final int writeSize = serverChannel.write(writerBuffer);

					if (writeSize <= 0) {
						break;
					}

					assertEquals(writeSize, SIZE);

				}

				serverKey.interestOps(serverKey.interestOps() & ~OP_WRITE);

			} catch (final Exception e) {
				fail(e.getMessage());
			}
		}

	};

	final Queue<byte[]> clientQueue = new ConcurrentLinkedQueue<byte[]>();

	final Handler clientHandler = new Handler() {

		@Override
		public String toString() {
			return "clientHandler;" + "  clientQueue:" + clientQueue.size()
					+ " clientChannel:" + clientChannel;
		}

		final ByteBuffer readerBuffer = ByteBuffer.allocateDirect(SIZE);

		final AtomicInteger readCount = new AtomicInteger(0);

		@Override
		public void handleRead() {
			try {
				while (true) {

					readerBuffer.clear();

					final int readSize = clientChannel.read(readerBuffer);

					if (readSize <= 0) {
						return;
					}

					assertEquals(readSize, SIZE);

					final byte[] arrayRead = new byte[readSize];

					readerBuffer.flip();
					readerBuffer.get(arrayRead);

					final byte[] arrayWritten = clientQueue.poll();

					assertNotNull(arrayWritten);
					assertTrue(Arrays.equals(arrayRead, arrayWritten));

					final int count = readCount.incrementAndGet();

					if (count == COUNT) {
						clientKey.interestOps( //
								clientKey.interestOps() & ~OP_READ);
						isTestON = false;
						log.info("client read done");
						return;
					}

				}
			} catch (final Exception e) {
				fail(e.getMessage());
			}
		}

		final ByteBuffer writerBuffer = ByteBuffer.allocateDirect(SIZE);

		final Random random = new Random();

		final AtomicInteger writeCount = new AtomicInteger(0);

		@Override
		public void handleWrite() {
			try {
				while (true) {

					final byte[] array = new byte[SIZE];

					random.nextBytes(array);

					writerBuffer.clear();
					writerBuffer.put(array);
					writerBuffer.flip();

					final int writeSize = clientChannel.write(writerBuffer);

					if (writeSize <= 0) {
						return;
					}

					assertEquals(writeSize, SIZE);

					clientQueue.offer(array);

					final int count = writeCount.incrementAndGet();

					if (count == COUNT) {
						clientKey.interestOps(//
								clientKey.interestOps() & ~OP_WRITE);

						log.info("client write done");

						return;
					}
				}
			} catch (final Exception e) {
				fail(e.getMessage());
			}
		}

	};

	private void doAccept(final SelectionKey key) {
		try {

			log.info("doAccept; key={}", key);

			assertEquals(key, acceptKey);
			assertEquals(acceptChannel, key.channel());

			assertNull(serverChannel);
			assertNull(serverKey);

			serverChannel = acceptChannel.accept();
			serverChannel.configureBlocking(false);

			serverKey = serverChannel.register(selector, OP_READ);
			serverKey.attach(serverHandler);

		} catch (final Exception e) {
			fail(e.getMessage());
		}
	}

	private void doConnect(final SelectionKey key) {
		try {

			log.info("doConnect; key={}", key);

			assertEquals(key, clientKey);
			assertEquals(clientChannel, key.channel());

			assertTrue(clientChannel.finishConnect());
			assertTrue(clientChannel.isConnected());

			clientKey.interestOps(OP_READ | OP_WRITE);
			clientKey.attach(clientHandler);

		} catch (final Exception e) {
			fail(e.getMessage());
		}

	}

	private void doRead(final SelectionKey key) {

		log.info("key:\n\t{}", key);

		final Object attachment = key.attachment();

		assertTrue(attachment instanceof Handler);

		final Handler handler = (Handler) attachment;

		handler.handleRead();

	}

	private void doWrite(final SelectionKey key) {

		log.info("key:\n\t{}", key);

		final Object attachment = key.attachment();

		assertTrue(attachment instanceof Handler);

		final Handler handler = (Handler) attachment;

		handler.handleWrite();

	}

	@Test(timeout = 3000)
	public void testWakeup() throws Exception {

		final SelectorProvider provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final Thread thread = new Thread() {

			@Override
			public void run() {

				try {

					log.info("init");

					/** blocking */
					selector.select();

					log.info("done");

				} catch (final IOException e) {
					e.printStackTrace();
				}

			}

		};

		thread.start();

		Thread.sleep(300);

		selector.wakeup();

		thread.join();

	}
}
