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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.StatusUDT;

public class TestSelectorProviderUDT {

	static final Logger log = LoggerFactory
			.getLogger(TestSelectorProviderUDT.class);

	@Before
	public void init() throws Exception {
	}

	@After
	public void done() throws Exception {
	}

	static final int SIZE = 10 * 2048;

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

		logSet(readySet);

	}

	@Test
	public void testAcceptOne() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final ServerSocketChannel acceptorChannel = provider
				.openServerSocketChannel();
		acceptorChannel.configureBlocking(false);
		acceptorChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT acceptorKey = (SelectionKeyUDT) acceptorChannel
				.register(selector, OP_ACCEPT);

		assertEquals(StatusUDT.LISTENING, acceptorKey.socketUDT().getStatus());

		final SocketChannel clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(false);
		clientChannel.socket().bind(localSocketAddress());

		final SelectionKeyUDT clientKey = (SelectionKeyUDT) clientChannel
				.register(selector, OP_CONNECT);

		assertEquals(StatusUDT.OPENED, clientKey.socketUDT().getStatus());

		clientChannel.connect(acceptorChannel.socket().getLocalSocketAddress());

		{
			log.info("acceptorKey={}", acceptorKey);
			log.info("clientKey={}", clientKey);
		}

		{
			/** first state */

			Thread.sleep(100);

			final int readyCount = selector.select(100);

			log.info("acceptorKey={}", acceptorKey);
			log.info("clientKey={}", clientKey);

			assertEquals(2, readyCount);

			final Set<SelectionKey> readySet = selector.selectedKeys();

			assertEquals(2, readySet.size());

			logSet(readySet);
		}

		assertEquals(StatusUDT.LISTENING, acceptorKey.socketUDT().getStatus());
		assertEquals(StatusUDT.CONNECTED, clientKey.socketUDT().getStatus());

		{
			/** second state, same as first */

			Thread.sleep(100);

			final int readyCount = selector.select(100);

			log.info("acceptorKey={}", acceptorKey);
			log.info("clientKey={}", clientKey);

			assertEquals(2, readyCount);

			final Set<SelectionKey> readySet = selector.selectedKeys();

			assertEquals(2, readySet.size());

			logSet(readySet);
		}

		final SocketChannel server = acceptorChannel.accept();
		assertNotNull(server);

		Thread.sleep(100);
		assertNull(acceptorChannel.accept());

		assertEquals(StatusUDT.LISTENING, acceptorKey.socketUDT().getStatus());
		assertEquals(StatusUDT.CONNECTED, clientKey.socketUDT().getStatus());

		{
			final int readyCount = selector.select(100);

			log.info("acceptorKey={}", acceptorKey);
			log.info("clientKey={}", clientKey);

			assertEquals(2, readyCount);

			final Set<SelectionKey> readySet = selector.selectedKeys();

			assertEquals(2, readySet.size());

			logSet(readySet);

		}

	}

	@Test
	public void testSelect() throws Exception {

		final int millisTimeout = 1 * 1000;

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final Selector selector = provider.openSelector();

		final ServerSocketChannel acceptorChannel1 = provider
				.openServerSocketChannel();

		final SocketChannel clientChannel1 = provider.openSocketChannel();

		acceptorChannel1.configureBlocking(false);
		clientChannel1.configureBlocking(false);

		final InetSocketAddress localAcceptorAddr = localSocketAddress();
		final InetSocketAddress localClientAddr = localSocketAddress();

		final ServerSocket socketServer = acceptorChannel1.socket();
		socketServer.bind(localAcceptorAddr);

		final Socket clientSocket1 = clientChannel1.socket();
		clientSocket1.bind(localClientAddr);

		final SelectionKeyUDT acceptorKey = (SelectionKeyUDT) acceptorChannel1
				.register(selector, OP_ACCEPT);

		final SelectionKeyUDT clientKey = (SelectionKeyUDT) clientChannel1
				.register(selector, OP_READ | OP_WRITE);

		clientSocket1.connect(localAcceptorAddr);
		log.info("connect");

		Thread.sleep(100);

		final long timeStart1 = System.currentTimeMillis();
		final int readyCount1 = selector.select(millisTimeout);
		final long timeFinish1 = System.currentTimeMillis();
		log.info("readyCount1={}", readyCount1);

		assertEquals(3, readyCount1);

		final long timeDiff1 = timeFinish1 - timeStart1;
		log.info("timeDiff1={}", timeDiff1);

		assertTrue(timeDiff1 < 10);

		final Set<SelectionKey> selectedKeySet1 = selector.selectedKeys();
		logSet(selectedKeySet1);

		assertEquals(2, selectedKeySet1.size());

		assertTrue(selectedKeySet1.contains(clientKey));
		assertTrue(selectedKeySet1.contains(acceptorKey));

		assertTrue(acceptorKey.isValid());
		assertTrue(clientKey.isValid());

		assertTrue(acceptorKey.isAcceptable());
		assertFalse(acceptorKey.isReadable());
		assertFalse(acceptorKey.isWritable());

		assertFalse(clientKey.isAcceptable());
		assertFalse(clientKey.isReadable());
		assertTrue(clientKey.isWritable());

		final SocketChannel clientChannel2 = (SocketChannel) clientKey
				.channel();
		assertTrue(clientChannel2 == clientChannel1);
		assertTrue(clientChannel2.isConnected());

		final ServerSocketChannel serverChannel2 = (ServerSocketChannel) acceptorKey
				.channel();
		assertTrue(serverChannel2 == acceptorChannel1);

		final SocketChannel servert = serverChannel2.accept();

		assertTrue(servert.isConnected());

		servert.configureBlocking(false);

		final SelectionKey serviceKey = servert.register(selector,
				SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		selectedKeySet1.clear();

		Thread.sleep(100);

		//

		final long timeStart2 = System.currentTimeMillis();
		final int readyCount2 = selector.select(millisTimeout);
		final long timeFinish2 = System.currentTimeMillis();
		log.info("readyCount2={}", readyCount2);

		assertEquals(3, readyCount2);

		final long timeDiff2 = timeFinish2 - timeStart2;
		log.info("timeDiff2={}", timeDiff2);

		assertTrue(timeDiff2 < 10);

		final Set<SelectionKey> selectedKeySet2 = selector.selectedKeys();
		logSet(selectedKeySet2);

		assertTrue(selectedKeySet2.contains(serviceKey));
		assertTrue(selectedKeySet2.contains(clientKey));
		assertTrue(selectedKeySet2.contains(acceptorKey));

		assertEquals(OP_WRITE, serviceKey.readyOps());
		assertEquals(OP_WRITE, clientKey.readyOps());

		selectedKeySet2.clear();

		//

		final ByteBuffer buffer = ByteBuffer.allocate(SIZE);

		final int writeCount = clientChannel2.write(buffer);

		assertEquals(SIZE, writeCount);

		Thread.sleep(100);

		final long timeStart3 = System.currentTimeMillis();
		final int readyCount3 = selector.select(millisTimeout);
		final long timeFinish3 = System.currentTimeMillis();
		log.info("readyCount3={}", readyCount3);

		assertEquals(3, readyCount3);

		final long timeDiff3 = timeFinish3 - timeStart3;
		log.info("timeDiff3={}", timeDiff3);

		assertTrue(timeDiff3 < 10);

		final Set<SelectionKey> selectedKeySet3 = selector.selectedKeys();
		logSet(selectedKeySet3);

		// if (key == keyClient) {
		// log.info("keyClient");
		// final SocketChannel channel = (SocketChannel) key.channel();
		// final ByteBuffer buffer = ByteBuffer.allocate(SIZE);
		// final int readCount = channel.read(buffer);
		// log.info("readCount={}", readCount);
		// continue;
		// }

		// if (key == keyServer) {
		// log.info("keyServer");
		// final ServerSocketChannel channel = (ServerSocketChannel) key
		// .channel();
		// final ByteBuffer buffer = ByteBuffer.allocate(SIZE);
		// final int writeCount = channel.write(buffer);
		// log.info("writeCount={}", writeCount);
		// continue;
		// }

		// fail("unexpected");

		socketServer.close();
		clientSocket1.close();

	}

	static void logSet(final Set<?> set) {
		for (final Object item : set) {
			log.info("-- {}", item);
		}
	}

}
