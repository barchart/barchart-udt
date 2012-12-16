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

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.util.TestHelp;

public class TestSelectorProviderUDT {

	static final Logger log = LoggerFactory
			.getLogger(TestSelectorProviderUDT.class);

	@Before
	public void init() throws Exception {
	}

	@After
	public void done() throws Exception {
	}

	@Test
	public void testOpenSelector() {
		// log.info("Not yet implemented");
	}

	@Test
	public void testSelect_Arrays() {

		log.info("testSelect_Arrays");

		try {

			final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

			final Selector selector = provider.openSelector();

			final ChannelSocketUDT channelClient = (ChannelSocketUDT) provider
					.openSocketChannel();

			final ChannelServerSocketUDT channelServer = (ChannelServerSocketUDT) provider
					.openServerSocketChannel();

			channelClient.configureBlocking(false);
			channelServer.configureBlocking(false);

			final InetSocketAddress addressServer = TestHelp
					.localSocketAddress();

			final InetSocketAddress addressClient = TestHelp
					.localSocketAddress();

			final ServerSocket socketServer = channelServer.socket();
			socketServer.bind(addressServer);
			// socketServer.accept();

			final Socket socketClient = channelClient.socket();
			socketClient.bind(addressClient);
			// socketClient.accept();

			final SelectionKeyUDT keyServer = (SelectionKeyUDT) channelServer
					.register(selector, OP_ACCEPT);

			final SelectionKeyUDT keyClient = (SelectionKeyUDT) channelClient
					.register(selector, OP_READ | OP_WRITE);

			final Set<SelectionKeyUDT> registeredKeySet = new HashSet<SelectionKeyUDT>();

			registeredKeySet.add(keyServer);
			registeredKeySet.add(keyClient);

			final Set<SelectionKeyUDT> selectedKeySet = new HashSet<SelectionKeyUDT>();

			log.info("registeredKeySet={}", registeredKeySet);
			log.info("selectedKeySet={}", selectedKeySet);

			log.info("");

			// socketServer.clearError();

			final long timeStart = System.currentTimeMillis();

			final int millisTimeout = 1 * 1000;

			final int arraySize = 10;

			final int[] readArray = new int[arraySize];
			final int[] writeArray = new int[arraySize];
			final int[] exceptArray = new int[arraySize];
			final int[] sizeArray = new int[3];

			readArray[0] = channelServer.serverSocketUDT.getSocketId();
			readArray[1] = channelClient.socketUDT.getSocketId();

			sizeArray[0] = 2;

			// log.info("readArray={}", readArray);

			SocketUDT.select(readArray, writeArray, exceptArray, sizeArray,
					millisTimeout);

			// log.info("readArray={}", readArray);

			final long timeFinish = System.currentTimeMillis();

			final long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			socketServer.close();
			socketClient.close();

		} catch (final Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	static final int SIZE = 10 * 2048;

	@Test
	public void testSelect_Buffers() {

		log.info("testSelect_Buffers");

		final int millisTimeout = 1 * 1000;

		try {

			final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

			final Selector selector = provider.openSelector();

			final ServerSocketChannel serverChannel1 = provider
					.openServerSocketChannel();

			final SocketChannel clientChannel1 = provider.openSocketChannel();

			serverChannel1.configureBlocking(false);
			clientChannel1.configureBlocking(false);

			final InetSocketAddress localServerAddr = TestHelp
					.localSocketAddress();
			final InetSocketAddress localClientAddr = TestHelp
					.localSocketAddress();

			final ServerSocket socketServer = serverChannel1.socket();
			socketServer.bind(localServerAddr);

			final Socket clientSocket1 = clientChannel1.socket();
			clientSocket1.bind(localClientAddr);

			final SelectionKeyUDT serverKey = (SelectionKeyUDT) serverChannel1
					.register(selector, OP_ACCEPT);

			final SelectionKeyUDT clientKey = (SelectionKeyUDT) clientChannel1
					.register(selector, OP_READ | OP_WRITE);

			clientSocket1.connect(localServerAddr);
			log.info("connect");

			Thread.sleep(100);

			final long timeStart1 = System.currentTimeMillis();
			final int readyCount1 = selector.select(millisTimeout);
			final long timeFinish1 = System.currentTimeMillis();
			log.info("readyCount1={}", readyCount1);

			assertEquals(2, readyCount1);

			final long timeDiff1 = timeFinish1 - timeStart1;
			log.info("timeDiff1={}", timeDiff1);

			assertTrue(timeDiff1 < 10);

			final Set<SelectionKey> selectedKeySet1 = selector.selectedKeys();
			logSet(selectedKeySet1);

			assertEquals(2, selectedKeySet1.size());

			assertTrue(selectedKeySet1.contains(clientKey));
			assertTrue(selectedKeySet1.contains(serverKey));

			assertTrue(serverKey.isValid());
			assertTrue(clientKey.isValid());

			assertTrue(serverKey.isAcceptable());
			assertFalse(serverKey.isReadable());
			assertFalse(serverKey.isWritable());

			assertFalse(clientKey.isAcceptable());
			assertFalse(clientKey.isReadable());
			assertTrue(clientKey.isWritable());

			final SocketChannel clientChannel2 = (SocketChannel) clientKey
					.channel();
			assertTrue(clientChannel2 == clientChannel1);
			assertTrue(clientChannel2.isConnected());

			final ServerSocketChannel serverChannel2 = (ServerSocketChannel) serverKey
					.channel();
			assertTrue(serverChannel2 == serverChannel1);

			final SocketChannel service = serverChannel2.accept();

			assertTrue(service.isConnected());

			service.configureBlocking(false);

			final SelectionKey serviceKey = service.register(selector,
					SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			selectedKeySet1.clear();

			Thread.sleep(100);

			//

			final long timeStart2 = System.currentTimeMillis();
			final int readyCount2 = selector.select(millisTimeout);
			final long timeFinish2 = System.currentTimeMillis();
			log.info("readyCount2={}", readyCount2);

			assertEquals(2, readyCount2);

			final long timeDiff2 = timeFinish2 - timeStart2;
			log.info("timeDiff2={}", timeDiff2);

			assertTrue(timeDiff2 < 10);

			final Set<SelectionKey> selectedKeySet2 = selector.selectedKeys();
			logSet(selectedKeySet2);

			assertTrue(selectedKeySet2.contains(serviceKey));
			assertTrue(selectedKeySet2.contains(clientKey));
			assertFalse(selectedKeySet2.contains(serverKey));

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

		} catch (final Exception e) {
			log.error("Exception;", e);
			fail("Exception; " + e.getMessage());
		}

	}

	static void logSet(final Set<?> set) {
		for (final Object item : set) {
			log.info("-- {}", item);
		}
	}

}
