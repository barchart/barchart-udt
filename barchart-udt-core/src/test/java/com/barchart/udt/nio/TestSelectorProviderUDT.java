/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

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

public class TestSelectorProviderUDT {

	static final Logger log = LoggerFactory
			.getLogger(TestSelectorProviderUDT.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
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

			final InetSocketAddress addressServer = new InetSocketAddress(//
					"localhost", 9011);

			final InetSocketAddress addressClient = new InetSocketAddress(//
					"localhost", 9012);

			final ServerSocket socketServer = channelServer.socket();
			socketServer.bind(addressServer);
			// socketServer.accept();

			final Socket socketClient = channelClient.socket();
			socketClient.bind(addressClient);
			// socketClient.accept();

			final SelectionKeyUDT keyServer = (SelectionKeyUDT) channelServer
					.register(selector, SelectionKey.OP_ACCEPT);

			final SelectionKeyUDT keyClient = (SelectionKeyUDT) channelClient
					.register(selector, SelectionKey.OP_READ
							| SelectionKey.OP_WRITE);

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

		} catch (Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	static final int SIZE = 10 * 2048;

	@Test
	public void testSelect_Buffers() {

		log.info("testSelect_Buffers");

		try {

			final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

			final Selector selector = provider.openSelector();

			final SocketChannel channelClient = provider.openSocketChannel();

			final ServerSocketChannel channelServer = provider
					.openServerSocketChannel();

			channelClient.configureBlocking(false);
			channelServer.configureBlocking(false);

			final InetSocketAddress localServer = new InetSocketAddress(//
					"localhost", 9021);

			final InetSocketAddress localClient = new InetSocketAddress(//
					"localhost", 9022);

			final ServerSocket socketServer = channelServer.socket();
			socketServer.bind(localServer);
			// socketServer.accept();

			final Socket socketClient = channelClient.socket();
			socketClient.bind(localClient);
			// socketClient.accept();

			socketClient.connect(localServer);
			log.info("connect");

			final SelectionKeyUDT keyServer = (SelectionKeyUDT) channelServer
					.register(selector, SelectionKey.OP_ACCEPT);

			final SelectionKeyUDT keyClient = (SelectionKeyUDT) channelClient
					.register(selector, SelectionKey.OP_READ
							| SelectionKey.OP_WRITE);

			final int millisTimeout = 1 * 1000;

			//
			final long timeStart = System.currentTimeMillis();
			final int readyCount = selector.select(millisTimeout);
			final long timeFinish = System.currentTimeMillis();
			log.info("readyCount={}", readyCount);

			final long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			final Set<SelectionKey> selectedKeySet = selector.selectedKeys();
			logSet(selectedKeySet);

			for (final SelectionKey key : selectedKeySet) {
				if (key.isWritable()) {
					log.info("isWritable");
					final SocketChannel channel = (SocketChannel) key.channel();
					final ByteBuffer buffer = ByteBuffer.allocate(SIZE);
					final int writeCount = channel.write(buffer);
					log.info("writeCount={}", writeCount);
				}
				if (key.isReadable()) {
					log.info("isReadable");
					final SocketChannel channel = (SocketChannel) key.channel();
					final ByteBuffer buffer = ByteBuffer.allocate(SIZE);
					int readCount = channel.read(buffer);
					log.info("readCount={}", readCount);
				}
			}

			socketServer.close();
			socketClient.close();

		} catch (Exception e) {
			log.error("Exception;", e);
			fail("Exception; " + e.getMessage());
		}

	}

	static void logSet(Set<?> set) {
		for (Object item : set) {
			log.info("{}", item);
		}
	}

}
