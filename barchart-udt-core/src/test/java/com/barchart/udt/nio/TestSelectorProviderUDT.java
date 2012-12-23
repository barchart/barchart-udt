/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
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

import util.UnitHelp;

import com.barchart.udt.StatusUDT;

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

		final ServerSocketChannel acceptChannel = provider
				.openServerSocketChannel();
		acceptChannel.configureBlocking(false);
		acceptChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT acceptKey = (SelectionKeyUDT) acceptChannel
				.register(selector, OP_ACCEPT);

		assertNotNull(acceptKey);

		final int readyCount = selector.select(1000);

		assertEquals(0, readyCount);

		final Set<SelectionKey> readySet = selector.selectedKeys();

		assertTrue(readySet.isEmpty());

		logSet(readySet);

		selector.close();

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

		assertEquals(StatusUDT.LISTENING, acceptKey.socketUDT().getStatus());

		final SocketChannel clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(false);
		clientChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT clientKey = (SelectionKeyUDT) clientChannel
				.register(selector, OP_CONNECT);

		assertEquals(StatusUDT.OPENED, clientKey.socketUDT().getStatus());

		assertNull("nothing to accept", acceptKey.socketUDT().accept());

		{
			log.info("### state 0");
			final int readyCount = selector.select(1000);
			final Set<SelectionKey> readySet = selector.selectedKeys();
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			UnitHelp.logSet(readySet);
			assertEquals(0, readySet.size());
			assertEquals(0, readyCount);
			readySet.clear();
			assertTrue(selector.selectedKeys().isEmpty());
		}

		clientChannel.connect(acceptChannel.socket().getLocalSocketAddress());

		socketAwait(clientKey.socketUDT(), StatusUDT.CONNECTED);

		{
			log.info("### state 1");
			final int readyCount = selector.select();
			final Set<SelectionKey> readySet = selector.selectedKeys();
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			logSet(readySet);
			assertEquals(2, readyCount);
			assertEquals(2, readySet.size());
			assertTrue(readySet.contains(acceptKey));
			assertTrue(readySet.contains(clientKey));
			readySet.clear();
			assertTrue(selector.selectedKeys().isEmpty());
		}

		{
			log.info("### state 2");
			final int readyCount = selector.select();
			final Set<SelectionKey> readySet = selector.selectedKeys();
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			logSet(readySet);
			assertEquals(2, readySet.size());
			assertEquals(2, readyCount);
			assertTrue(readySet.contains(acceptKey));
			assertTrue(readySet.contains(clientKey));
			readySet.clear();
			assertTrue(selector.selectedKeys().isEmpty());
		}

		final SocketChannel serverChannel = acceptChannel.accept();
		assertNotNull("first accept valid", serverChannel);
		assertNull("second accept invalid", acceptChannel.accept());

		serverChannel.configureBlocking(false);

		final SelectionKeyUDT serverKey = (SelectionKeyUDT) serverChannel
				.register(selector, 0);

		socketAwait(serverKey.socketUDT(), StatusUDT.CONNECTED);

		{
			log.info("### state 3");
			final int readyCount = selector.select();
			final Set<SelectionKey> readySet = selector.selectedKeys();
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			UnitHelp.logSet(readySet);
			assertEquals(1, readyCount);
			assertEquals(1, readySet.size());
			assertTrue(readySet.contains(clientKey));
			readySet.clear();
			assertTrue(selector.selectedKeys().isEmpty());
		}

		assertFalse(serverChannel.isConnectionPending());

		assertTrue(clientChannel.isConnectionPending());
		assertTrue(clientChannel.finishConnect());
		assertFalse(clientChannel.isConnectionPending());

		{
			log.info("### state 4");
			final int readyCount = selector.select();
			final Set<SelectionKey> readySet = selector.selectedKeys();
			log.info("acceptKey={}", acceptKey);
			log.info("clientKey={}", clientKey);
			UnitHelp.logSet(readySet);
			assertEquals(0, readyCount);
			assertEquals(0, readySet.size());
		}

		serverChannel.close();
		clientChannel.close();
		acceptChannel.close();

		selector.close();

	}

	@Test
	public void testSelectCycle() throws Exception {

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
				.register(selector, OP_CONNECT | OP_READ | OP_WRITE);

		clientSocket.connect(acceptSocket.getLocalSocketAddress());

		socketAwait(clientKey.socketUDT(), StatusUDT.CONNECTED);

		log.info("connect");

		{

			log.info("### state 0");

			final int readyCount = selector.select();
			log.info("readyCount={}", readyCount);

			assertEquals(2, readyCount);

			final Set<SelectionKey> keySet = selector.selectedKeys();
			UnitHelp.logSet(keySet);

			assertEquals(2, keySet.size());

			assertTrue(keySet.contains(clientKey));
			assertTrue(keySet.contains(acceptKey));

			assertTrue(acceptKey.isValid());
			assertTrue(clientKey.isValid());

			assertTrue("accept has accept", acceptKey.isAcceptable());
			assertFalse("accept w/o conn", acceptKey.isConnectable());
			assertFalse("accept w/o read", acceptKey.isReadable());
			assertFalse("accept w/o write", acceptKey.isWritable());

			assertFalse("no accept", clientKey.isAcceptable());
			assertTrue("connection", clientKey.isConnectable());
			assertFalse("no read", clientKey.isReadable());
			assertFalse("no write", clientKey.isWritable());

			assertTrue(clientChannel.isConnected());

			keySet.clear();

		}

		final SocketChannel serverChannel = acceptChannel.accept();

		assertTrue(serverChannel.isConnected());

		serverChannel.configureBlocking(false);

		final SelectionKey serverKey = serverChannel.register( //
				selector, OP_READ | OP_WRITE);

		assertTrue("consumer must ack", clientChannel.finishConnect());

		{

			log.info("### state 1");

			final int readyCount = selector.select();
			log.info("readyCount={}", readyCount);

			assertEquals(2, readyCount);

			final Set<SelectionKey> keySet = selector.selectedKeys();
			UnitHelp.logSet(keySet);

			assertEquals(2, keySet.size());

			assertTrue(keySet.contains(serverKey));
			assertTrue(keySet.contains(clientKey));
			assertFalse("key is not reported", keySet.contains(acceptKey));
			assertTrue("yet key is not cleared", acceptKey.isAcceptable());

			assertTrue("has write", serverKey.isWritable());
			assertTrue("has write", clientKey.isWritable());

			keySet.clear();

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

			assertEquals(SIZE, writeCount);

			Thread.sleep(1000); // let it send

			final int readyCount = selector.select();
			log.info("readyCount={}", readyCount);

			assertEquals(2, readyCount);

			final Set<SelectionKey> keySet = selector.selectedKeys();
			UnitHelp.logSet(keySet);

			assertEquals(2, keySet.size());

			assertTrue(keySet.contains(serverKey));
			assertTrue(keySet.contains(clientKey));

			assertFalse("client w/o accept", clientKey.isAcceptable());
			assertFalse("client w/o conn", clientKey.isConnectable());
			assertFalse("client w/o read", clientKey.isReadable());
			assertTrue("client has write", clientKey.isWritable());

			assertFalse("server w/o accept", serverKey.isAcceptable());
			assertFalse("server w/o conn", serverKey.isConnectable());
			assertTrue("server has read", serverKey.isReadable());
			assertTrue("server has write", serverKey.isWritable());

			keySet.clear();

		}

		{

			log.info("### state 3");

			final ByteBuffer buffer = ByteBuffer.allocate(SIZE);

			final int readSize = serverChannel.read(buffer);

			assertEquals(SIZE, readSize);

			final int readyCount = selector.select();
			log.info("readyCount={}", readyCount);

			final Set<SelectionKey> keySet = selector.selectedKeys();
			UnitHelp.logSet(keySet);

			assertFalse("server w/o accept", serverKey.isAcceptable());
			assertFalse("server w/o conn", serverKey.isConnectable());
			assertTrue("server w/o read", serverKey.isReadable());
			assertTrue("server has write", serverKey.isWritable());

			keySet.clear();

		}

		serverChannel.close();
		clientChannel.close();
		acceptChannel.close();

		assertFalse(serverChannel.isOpen());
		assertFalse(clientChannel.isOpen());
		assertFalse(acceptChannel.isOpen());

	}

}
