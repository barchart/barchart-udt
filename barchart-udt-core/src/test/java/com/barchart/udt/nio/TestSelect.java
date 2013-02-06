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

import util.TestAny;
import util.UnitHelp;

import com.barchart.udt.StatusUDT;

public class TestSelect extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
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

	@Test(timeout = 5 * 1000)
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

	@Test(timeout = 3 * 1000)
	public void testAcceptOne() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final ServerSocketChannel acceptChannel = provider
				.openServerSocketChannel();
		acceptChannel.configureBlocking(false);
		acceptChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT acceptKey = (SelectionKeyUDT) acceptChannel
				.register(selector, OP_ACCEPT);

		socketAwait(acceptKey.socketUDT(), StatusUDT.LISTENING);

		final SocketChannel clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(false);
		clientChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT clientKey = (SelectionKeyUDT) clientChannel
				.register(selector, OP_CONNECT);

		socketAwait(clientKey.socketUDT(), StatusUDT.OPENED);

		log.info("acceptKey={}", acceptKey);
		log.info("clientKey={}", clientKey);

		{
			log.info("state 0 - nothing to accept");
			final int readyCount = selector.select(500);
			final Set<SelectionKey> readySet = selector.selectedKeys();
			UnitHelp.logSet(readySet);
			assertEquals(0, readySet.size());
			assertEquals(0, readyCount);
			readySet.clear();
		}

		clientChannel.connect(acceptChannel.socket().getLocalSocketAddress());

		socketAwait(clientKey.socketUDT(), StatusUDT.CONNECTED);

		log.info("acceptKey={}", acceptKey);
		log.info("clientKey={}", clientKey);

		{
			log.info("state 1 - both accept and client have interest");
			final int readyCount = selector.select();
			final Set<SelectionKey> readySet = selector.selectedKeys();
			logSet(readySet);
			assertEquals(2, readyCount);
			assertEquals(2, readySet.size());
			assertTrue(readySet.contains(acceptKey));
			assertTrue(readySet.contains(clientKey));
			assertTrue(acceptKey.isAcceptable());
			assertTrue(clientKey.isConnectable());
			readySet.clear();
		}

		log.info("acceptKey={}", acceptKey);
		log.info("clientKey={}", clientKey);

		{
			log.info("state 2 - verify same interest reported");
			final int readyCount = selector.select();
			final Set<SelectionKey> readySet = selector.selectedKeys();
			logSet(readySet);
			assertEquals(2, readySet.size());
			assertEquals(2, readyCount);
			assertTrue(readySet.contains(acceptKey));
			assertTrue(readySet.contains(clientKey));
			assertTrue(acceptKey.isAcceptable());
			assertTrue(clientKey.isConnectable());
			readySet.clear();
		}

		final SocketChannel serverChannel = acceptChannel.accept();
		assertNotNull("first accept valid", serverChannel);
		assertNull("second accept invalid", acceptChannel.accept());

		serverChannel.configureBlocking(false);

		final SelectionKeyUDT serverKey = (SelectionKeyUDT) serverChannel
				.register(selector, 0);

		socketAwait(serverKey.socketUDT(), StatusUDT.CONNECTED);

		log.info("acceptKey={}", acceptKey);
		log.info("clientKey={}", clientKey);

		{
			log.info("state 3 - post accept interest");
			final int readyCount = selector.select();
			final Set<SelectionKey> readySet = selector.selectedKeys();
			UnitHelp.logSet(readySet);
			assertEquals(1, readyCount);
			assertEquals(1, readySet.size());
			assertTrue(readySet.contains(clientKey));
			assertTrue(acceptKey.isAcceptable()); // XXX
			assertTrue(clientKey.isConnectable()); // XXX
			assertFalse(clientKey.isReadable());
			assertFalse(clientKey.isWritable());
			readySet.clear();
		}

		assertFalse(serverChannel.isConnectionPending());

		assertTrue(clientChannel.isConnectionPending());
		assertTrue(clientChannel.finishConnect());
		assertFalse(clientChannel.isConnectionPending());

		log.info("acceptKey={}", acceptKey);
		log.info("clientKey={}", clientKey);

		clientKey.interestOps(OP_READ | OP_WRITE);

		{
			log.info("state 4 - client is connected");
			final int readyCount = selector.select();
			final Set<SelectionKey> readySet = selector.selectedKeys();
			UnitHelp.logSet(readySet);
			assertEquals(1, readyCount);
			assertEquals(1, readySet.size());
			assertFalse(clientKey.isReadable());
			assertTrue(clientKey.isWritable());
			readySet.clear();
		}

		log.info("acceptKey={}", acceptKey);
		log.info("clientKey={}", clientKey);

		clientKey.interestOps(0);

		{
			log.info("state 5 - client is connected");
			final int readyCount = selector.select(500);
			final Set<SelectionKey> readySet = selector.selectedKeys();
			UnitHelp.logSet(readySet);
			assertEquals(0, readyCount);
			assertEquals(0, readySet.size());
			readySet.clear();
		}

		log.info("acceptKey={}", acceptKey);
		log.info("clientKey={}", clientKey);

		clientKey.interestOps(OP_WRITE);

		{
			log.info("state 6 - client is connected");
			final int readyCount = selector.select(500);
			final Set<SelectionKey> readySet = selector.selectedKeys();
			UnitHelp.logSet(readySet);
			assertEquals(1, readyCount);
			assertEquals(1, readySet.size());
			assertFalse(clientKey.isReadable());
			assertTrue(clientKey.isWritable());
			readySet.clear();
		}

		log.info("acceptKey={}", acceptKey);
		log.info("clientKey={}", clientKey);

		serverChannel.close();
		clientChannel.close();
		acceptChannel.close();

		selector.close();

	}

	@Test(timeout = 5 * 1000)
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

		socketAwait(acceptKey.socketUDT(), StatusUDT.LISTENING);

		final SelectionKeyUDT clientKey = (SelectionKeyUDT) clientChannel
				.register(selector, OP_CONNECT);

		clientSocket.connect(acceptSocket.getLocalSocketAddress());

		socketAwait(clientKey.socketUDT(), StatusUDT.CONNECTED);

		log.info("accept : {}", acceptKey);
		log.info("client : {}", clientKey);

		{

			log.info("state 0 - accept / client setup");

			final int readyCount = selector.select();
			log.info("readyCount={}", readyCount);

			assertEquals(2, readyCount);

			final Set<SelectionKey> keySet = selector.selectedKeys();
			UnitHelp.logSet(keySet);

			assertEquals(2, keySet.size());

			assertTrue(keySet.contains(acceptKey));
			assertTrue(keySet.contains(clientKey));

			assertTrue(acceptKey.isValid());
			assertTrue(clientKey.isValid());

			assertTrue("accept has accept", acceptKey.isAcceptable());
			assertFalse("accept w/o conn", acceptKey.isConnectable());
			assertFalse("accept w/o read", acceptKey.isReadable());
			assertFalse("accept w/o write", acceptKey.isWritable());

			assertFalse("client w/o accept", clientKey.isAcceptable());
			assertTrue("client has connect", clientKey.isConnectable());
			assertFalse("client w/o read", clientKey.isReadable());
			assertFalse("client w/o write", clientKey.isWritable());

			assertTrue(clientChannel.isConnected());

			keySet.clear();

		}

		final SocketChannel serverChannel = acceptChannel.accept();
		serverChannel.configureBlocking(false);
		final SelectionKey serverKey = serverChannel.register( //
				selector, OP_READ | OP_WRITE);
		assertTrue("server connect", serverChannel.isConnected());

		assertTrue("client connect", clientChannel.finishConnect());
		clientKey.interestOps(OP_READ | OP_WRITE);

		log.info("server : {}", serverKey);
		log.info("client : {}", clientKey);

		{

			log.info("state 1 - process accept");

			final int readyCount = selector.select();
			log.info("readyCount={}", readyCount);
			final Set<SelectionKey> keySet = selector.selectedKeys();
			UnitHelp.logSet(keySet);

			assertEquals(2, readyCount);
			assertEquals(2, keySet.size());

			assertTrue(keySet.contains(serverKey));
			assertTrue(keySet.contains(clientKey));

			assertTrue("server has write", serverKey.isWritable());
			assertTrue("client has write", clientKey.isWritable());

			keySet.clear();

		}

		//

		final Random random = new Random(0);
		final byte[] writeArray = new byte[SIZE];
		random.nextBytes(writeArray);

		log.info("server : {}", serverKey);
		log.info("client : {}", clientKey);

		{

			log.info("state 2 - client write to server");

			final ByteBuffer buffer = ByteBuffer.allocate(SIZE);
			buffer.put(writeArray);
			buffer.flip();

			final int writeCount = clientChannel.write(buffer);

			assertEquals(SIZE, writeCount);

			Thread.sleep(1000); // let it send

			final int readyCount = selector.select();
			log.info("readyCount={}", readyCount);
			final Set<SelectionKey> keySet = selector.selectedKeys();
			UnitHelp.logSet(keySet);

			assertEquals(2, readyCount);
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

		log.info("server : {}", serverKey);
		log.info("client : {}", clientKey);

		{

			log.info("state 3 - server read from client");

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

		log.info("server : {}", serverKey);
		log.info("client : {}", clientKey);

		serverChannel.close();
		clientChannel.close();
		acceptChannel.close();

		assertFalse(serverChannel.isOpen());
		assertFalse(clientChannel.isOpen());
		assertFalse(acceptChannel.isOpen());

	}

}
