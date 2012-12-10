package com.barchart.udt.net;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.HelperUtils;

/**
 * Sets up a simple UDT client and server to test sending messages between them.
 */
public class SimpleUdtTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testUdtClientServer() throws Exception {

		// This will hold the message received on the server to make sure
		// we're getting the right one across multiple threads.
		final AtomicReference<String> ref = new AtomicReference<String>();

		final InetSocketAddress serverAddress = HelperUtils
				.getLocalSocketAddress();

		startThreadedServer(serverAddress, ref);

		final Socket clientSocket = new NetSocketUDT();

		final InetSocketAddress clientAddress = HelperUtils
				.getLocalSocketAddress();

		clientSocket.bind(clientAddress);
		assertTrue("Socket not bound!!", clientSocket.isBound());

		clientSocket.connect(serverAddress);
		assertTrue("Socket not connected!", clientSocket.isConnected());

		final String msgOut = "HELLO UDT";

		final OutputStream os = clientSocket.getOutputStream();

		os.write(msgOut.getBytes());

		synchronized (ref) {
			final String str = ref.get();
			if (str == null || !str.equals(msgOut)) {
				ref.wait(3 * 1000);
			}
		}

		assertEquals(msgOut.length(), ref.get().length());

		assertEquals("Did not get the expected message on the server!!",
				msgOut, ref.get());

		log.info("Server received: |{}|", ref.get());

		final byte[] received = new byte[1234];

		final InputStream is = clientSocket.getInputStream();

		final int readCount = is.read(received);

		assertEquals(msgOut, new String(received, 0, readCount));

	}

	private void startThreadedServer(final InetSocketAddress serverAddress,
			final AtomicReference<String> ref) throws InterruptedException {
		
		final AtomicBoolean readyToAccept = new AtomicBoolean(false);
		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				try {
					startUdtServer(serverAddress, ref, readyToAccept);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		};

		final Thread t = new Thread(runner, "test-thread");
		t.setDaemon(true);
		t.start();

		// We need to wait for a second to make sure the server thread starts.
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

	private void startUdtServer(final InetSocketAddress serverAddress,
			final AtomicReference<String> ref, 
			final AtomicBoolean readyToAccept) throws IOException {

		final ServerSocket acceptorSocket = new NetServerSocketUDT();

		acceptorSocket.bind(serverAddress);
		assert acceptorSocket.isBound();

		readyToAccept.set(true);
		synchronized(readyToAccept) {
			readyToAccept.notifyAll();
		}
		final Socket connectorSocket = acceptorSocket.accept();
		assert connectorSocket.isConnected();

		echo(connectorSocket, ref);

	}

	private void echo(final Socket socket, final AtomicReference<String> ref)
			throws IOException {

		final InputStream is = socket.getInputStream();
		final OutputStream os = socket.getOutputStream();

		final byte[] data = new byte[8192];

		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				try {

					final int count = is.read(data);

					final String str = new String(data, 0, count);

					log.info("|{}|", str);

					ref.set(str);

					os.write(str.getBytes());

					synchronized (ref) {
						ref.notifyAll();
					}

				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		};

		final Thread dt = new Thread(runner, "### starter");
		dt.setDaemon(true);
		dt.start();

	}

}
