/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import static com.barchart.udt.util.TestHelp.*;
import static java.nio.channels.SelectionKey.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.StatusUDT;
import com.barchart.udt.util.TestHelp;

public class TestSelectorProviderUDT {

	public static final Logger log = LoggerFactory
			.getLogger(TestSelectorProviderUDT.class);

	@Before
	public void init() throws Exception {
	}

	@After
	public void done() throws Exception {
	}

	static final int SIZE = 20 * 1024;

	@Test
	public void testSelectInfinite() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final AtomicBoolean isSelected = new AtomicBoolean(false);

		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					selector.select(0);
					isSelected.set(true);
				} catch (final IOException e) {
					fail("");
				}
			}
		};

		final long timeStart = System.currentTimeMillis();

		thread.start();

		final long timeDelay = 100;

		thread.join(timeDelay);

		final long timeFinish = System.currentTimeMillis();

		final long timeDiff = timeFinish - timeStart;

		assertTrue(timeDiff >= timeDelay);

		assertFalse(isSelected.get());

	}

	@Test
	public void testSelectImmediate() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final long timeStart = System.currentTimeMillis();

		final int readyCount = selector.selectNow();

		final long timeFinish = System.currentTimeMillis();

		final long timeDiff = timeFinish - timeStart;

		assertTrue(timeDiff < 3);

		assertEquals(0, readyCount);

	}

	@Test
	public void testSelectDelayed() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final long timeDelay = 100;

		final long timeStart = System.currentTimeMillis();

		final int readyCount = selector.select(timeDelay);

		final long timeFinish = System.currentTimeMillis();

		final long timeDiff = timeFinish - timeStart;

		assertTrue(timeDiff >= timeDelay);

		assertEquals(0, readyCount);

	}

	@Test
	public void testAcceptNone() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final ServerSocketChannel acceptorChannel = provider
				.openServerSocketChannel();
		acceptorChannel.configureBlocking(false);
		acceptorChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT acceptorKey = (SelectionKeyUDT) acceptorChannel
				.register(selector, OP_ACCEPT);

		assertNotNull(acceptorKey);

		final int readyCount = selector.select(100);

		assertEquals(0, readyCount);

		final Set<SelectionKey> readySet = selector.selectedKeys();

		assertTrue(readySet.isEmpty());

		TestHelp.logSet(readySet);

	}

	@Test
	public void testAcceptOne() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final ServerSocketChannel acceptChannel = provider
				.openServerSocketChannel();
		acceptChannel.configureBlocking(false);
		acceptChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT acceptKey = (SelectionKeyUDT) acceptChannel
				.register(selector, OP_ACCEPT);

		final SocketChannel clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(false);
		clientChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT clientKey = (SelectionKeyUDT) clientChannel
				.register(selector, OP_CONNECT);

		assertEquals(StatusUDT.LISTENING, acceptKey.socketUDT().getStatus());
		assertEquals(StatusUDT.OPENED, clientKey.socketUDT().getStatus());

		assertNull("nothing to accept", acceptKey.socketUDT().accept());

		{
			log.info("### state 0");
			Thread.sleep(50);
			final int readyCount = selector.select(50);
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			assertEquals(0, readyCount);
			final Set<SelectionKey> readySet = selector.selectedKeys();
			assertEquals(0, readySet.size());
			TestHelp.logSet(readySet);
		}

		clientChannel.connect(acceptChannel.socket().getLocalSocketAddress());

		{
			log.info("### state 1");
			Thread.sleep(50);
			final int readyCount = selector.select(50);
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			assertEquals(2, readyCount);
			final Set<SelectionKey> readySet = selector.selectedKeys();
			assertEquals(2, readySet.size());
			TestHelp.logSet(readySet);
		}

		assertEquals(StatusUDT.LISTENING, acceptKey.socketUDT().getStatus());
		assertEquals(StatusUDT.CONNECTED, clientKey.socketUDT().getStatus());

		{
			log.info("### state 2");
			Thread.sleep(50);
			final int readyCount = selector.select(50);
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			assertEquals(2, readyCount);
			final Set<SelectionKey> readySet = selector.selectedKeys();
			assertEquals(2, readySet.size());
			TestHelp.logSet(readySet);
		}

		final SocketChannel server = acceptChannel.accept();
		assertNotNull("first accept valid", server);
		assertNull("second accept invalid", acceptChannel.accept());

		{
			log.info("### state 3");
			Thread.sleep(100);
			final int readyCount = selector.select(100);
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			assertEquals(1, readyCount);
			final Set<SelectionKey> readySet = selector.selectedKeys();
			assertEquals(1, readySet.size());
			TestHelp.logSet(readySet);
		}

	}

	@Test
	public void testSelect() throws Exception {

		final int millisTimeout = 1 * 1000;

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final ServerSocketChannel acceptChannel = provider
				.openServerSocketChannel();
		acceptChannel.configureBlocking(false);

		final SocketChannel clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(false);

		final ServerSocket acceptSocket = acceptChannel.socket();
		acceptSocket.bind(localSocketAddress());

		final Socket clientSocket = clientChannel.socket();
		clientSocket.bind(localSocketAddress());

		final SelectionKeyUDT acceptKey = (SelectionKeyUDT) acceptChannel
				.register(selector, OP_ACCEPT);

		final SelectionKeyUDT clientKey = (SelectionKeyUDT) clientChannel
				.register(selector, OP_READ | OP_WRITE);

		clientSocket.connect(acceptSocket.getLocalSocketAddress());

		log.info("connect");

		{

			log.info("### state 0");

			Thread.sleep(50);

			final long timeStart = System.currentTimeMillis();
			final int readyCount = selector.select(millisTimeout);
			final long timeFinish = System.currentTimeMillis();
			log.info("readyCount={}", readyCount);

			assertEquals(2, readyCount);

			final long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			assertTrue(timeDiff < 10);

			final Set<SelectionKey> keySet = selector.selectedKeys();
			TestHelp.logSet(keySet);

			assertEquals(2, keySet.size());

			assertTrue(keySet.contains(clientKey));
			assertTrue(keySet.contains(acceptKey));

			assertTrue(acceptKey.isValid());
			assertTrue(clientKey.isValid());

			assertTrue("accept has accept", acceptKey.isAcceptable());
			assertFalse("accept w/o read", acceptKey.isReadable());
			assertFalse("accept w/o write", acceptKey.isWritable());

			assertFalse("no accept", clientKey.isAcceptable());
			assertFalse("no read", clientKey.isReadable());
			assertTrue("has write", clientKey.isWritable());

			assertTrue(clientChannel.isConnected());

		}

		final SocketChannel serverChannel = acceptChannel.accept();

		assertTrue(serverChannel.isConnected());

		serverChannel.configureBlocking(false);

		final SelectionKey serverKey = serverChannel.register(//
				selector, OP_READ | OP_WRITE);

		{

			log.info("### state 1");

			Thread.sleep(50);

			final long timeStart = System.currentTimeMillis();
			final int readyCount = selector.select(millisTimeout);
			final long timeFinish = System.currentTimeMillis();
			log.info("readyCount={}", readyCount);

			assertEquals(2, readyCount);

			final long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			assertTrue(timeDiff < 10);

			final Set<SelectionKey> keySet = selector.selectedKeys();
			TestHelp.logSet(keySet);

			assertTrue(keySet.contains(serverKey));
			assertTrue(keySet.contains(clientKey));
			assertFalse("nothing to accept", keySet.contains(acceptKey));
			assertFalse("nothing to accept", acceptKey.isAcceptable());

			assertEquals(OP_WRITE, serverKey.readyOps());
			assertEquals(OP_WRITE, clientKey.readyOps());

			assertTrue("has write", clientKey.isWritable());
			assertTrue("has write", serverKey.isWritable());

		}

		//

		final Random random = new Random(0);
		final byte[] writeArray = new byte[SIZE];
		random.nextBytes(writeArray);

		{

			log.info("### state 2");

			final ByteBuffer buffer = ByteBuffer.allocate(SIZE);
			buffer.put(writeArray);
			buffer.flip();

			final int writeCount = clientChannel.write(buffer);

			Thread.sleep(50);

			assertEquals(SIZE, writeCount);

			final long timeStart = System.currentTimeMillis();
			final int readyCount = selector.select(millisTimeout);
			final long timeFinish = System.currentTimeMillis();
			log.info("readyCount={}", readyCount);

			assertEquals(2, readyCount);

			final long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			assertTrue(timeDiff < 10);

			final Set<SelectionKey> keySet3 = selector.selectedKeys();
			TestHelp.logSet(keySet3);

			assertEquals(2, keySet3.size());

			assertTrue(keySet3.contains(serverKey));
			assertTrue(keySet3.contains(clientKey));

			assertFalse("accept w/o accept", acceptKey.isAcceptable());
			assertFalse("accept w/o read", acceptKey.isReadable());
			assertFalse("accept w/o write", acceptKey.isWritable());

			assertFalse("client w/o accept", clientKey.isAcceptable());
			assertFalse("client w/o read", clientKey.isReadable());
			assertTrue("client has write", clientKey.isWritable());

			assertFalse("server w/o accept", clientKey.isAcceptable());
			assertTrue("server has read", serverKey.isReadable());
			assertTrue("server has write", serverKey.isWritable());

		}

		{

			final ByteBuffer buffer = ByteBuffer.allocate(SIZE);

			final int readSize = serverChannel.read(buffer);

			assertEquals(SIZE, readSize);

			/** FIXME review select contract */
			// assertFalse("server has no read", serverKey.isReadable());

		}

		serverChannel.close();
		clientChannel.close();
		acceptChannel.close();

		assertFalse(serverChannel.isOpen());
		assertFalse(clientChannel.isOpen());
		assertFalse(acceptChannel.isOpen());

		Thread.sleep(50);

	}

}
