package com.barchart.udt.nio;

import static com.barchart.udt.util.HelperUtils.*;
import static java.nio.channels.SelectionKey.*;
import static org.junit.Assert.*;

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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* basic single thread selector test */
public class TestSelectorUDT {

	protected static final int SIZE = 1460;

	protected static final int COUNT = 1000;

	private static Logger log = LoggerFactory.getLogger(TestSelectorUDT.class);

	final SelectorProvider provider = SelectorProviderUDT.DATAGRAM;

	volatile SelectionKey acceptorKey;
	volatile SelectionKey serverKey;
	volatile SelectionKey clientKey;

	volatile ServerSocketChannel acceptorChannel;
	volatile SocketChannel serverChannel;
	volatile SocketChannel clientChannel;

	volatile Selector selector;

	volatile SocketAddress acceptorAddress;
	volatile SocketAddress clientAddress;

	@Before
	public void setUp() throws Exception {

		selector = provider.openSelector();

		acceptorChannel = provider.openServerSocketChannel();
		acceptorChannel.configureBlocking(false);
		acceptorAddress = getLocalSocketAddress();
		acceptorChannel.socket().bind(acceptorAddress);

		acceptorKey = acceptorChannel.register(selector, OP_ACCEPT);

		clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(false);
		clientAddress = getLocalSocketAddress();
		clientChannel.socket().bind(clientAddress);

		clientKey = clientChannel.register(selector, OP_CONNECT);

		clientChannel.connect(acceptorAddress);

		log.info("setUp");

	}

	@After
	public void tearDown() throws Exception {

		log.info("tearDown");

	}

	volatile boolean isTestON = true;

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

		} catch (Exception e) {
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
					byte[] array = new byte[readSize];
					readerBuffer.flip();
					readerBuffer.get(array);
					serverQueue.offer(array);
					serverKey.interestOps(serverKey.interestOps() | OP_WRITE);
				}
			} catch (Exception e) {
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
			} catch (Exception e) {
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
					byte[] arrayRead = new byte[readSize];
					readerBuffer.flip();
					readerBuffer.get(arrayRead);
					byte[] arrayWritten = clientQueue.poll();
					assertNotNull(arrayWritten);
					assertTrue(Arrays.equals(arrayRead, arrayWritten));
					final int count = readCount.incrementAndGet();
					if (count == COUNT) {
						clientKey.interestOps(clientKey.interestOps()
								& ~OP_READ);
						isTestON = false;
						log.info("client read done");
						return;
					}
				}
			} catch (Exception e) {
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
					byte[] array = new byte[SIZE];
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
					// log.info("count={}", count);
					if (count == COUNT) {
						clientKey.interestOps(clientKey.interestOps()
								& ~OP_WRITE);
						log.info("client write done");
						return;
					}
				}
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}

	};

	private void doAccept(SelectionKey key) {
		try {

			log.info("doAccept; key={}", key);

			assertEquals(key, acceptorKey);
			assertEquals(acceptorChannel, (ServerSocketChannel) key.channel());

			assertNull(serverChannel);
			assertNull(serverKey);

			serverChannel = acceptorChannel.accept();
			serverChannel.configureBlocking(false);

			serverKey = serverChannel.register(selector, OP_READ);
			serverKey.attach(serverHandler);

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private void doConnect(SelectionKey key) {
		try {

			log.info("doConnect; key={}", key);

			assertEquals(key, clientKey);
			assertEquals(clientChannel, (SocketChannel) key.channel());

			assertTrue(clientChannel.finishConnect());
			assertTrue(clientChannel.isConnected());

			clientKey.interestOps(OP_READ | OP_WRITE);
			clientKey.attach(clientHandler);

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	private void doRead(SelectionKey key) {

		Object attachment = key.attachment();
		assertTrue(attachment instanceof Handler);

		Handler handler = (Handler) attachment;
		handler.handleRead();

		// log.info("doRead; handler={}", handler);

	}

	private void doWrite(SelectionKey key) {

		Object attachment = key.attachment();
		assertTrue(attachment instanceof Handler);

		Handler handler = (Handler) attachment;
		handler.handleWrite();

		// log.info("doWrite; handler={}", handler);

	}

}
