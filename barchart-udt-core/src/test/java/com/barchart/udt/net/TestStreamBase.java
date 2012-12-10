package com.barchart.udt.net;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelperUtils;
import com.barchart.udt.util.StopWatch;

public class TestStreamBase {

	private static final Logger log = LoggerFactory
			.getLogger(TestStreamBase.class);

	final ServiceFactory factory1 = new ServiceFactory() {
		@Override
		public StreamService newService(SocketUDT connectorSocket)
				throws Exception {
			return new StreamService(connectorSocket) {
				@Override
				public void run() {
					while (true) {
						try {
							int value = streamIn.read();
							streamOut.write(value);
						} catch (IOException e) {
							log.error("server; {}", e.getMessage());
							break;
						}
					}
				}
			};
		}
	};

	@Test
	public void testStream11() throws Exception {

		InetSocketAddress serverAddress = HelperUtils.getLocalSocketAddress();

		StreamServer server = new StreamServer(TypeUDT.DATAGRAM, serverAddress,
				factory1);

		StreamClient client = new StreamClient(TypeUDT.DATAGRAM, serverAddress) {
			@Override
			public void run() {
				try {
					final int loop = 100;
					StopWatch timer = new StopWatch();
					timer.start();
					for (int k = 0; k < loop; k++) {
						for (int index = Byte.MIN_VALUE; index <= Byte.MAX_VALUE; index++) {
							streamOut.write(index);
						}
						for (int index = Byte.MIN_VALUE; index <= Byte.MAX_VALUE; index++) {
							int value = streamIn.read();
							assertEquals(value, index);
						}
					}
					timer.stop();
					log.info("timer : {}", timer.nanoString());
					synchronized (this) {
						this.notifyAll();
					}
				} catch (Exception e) {
					log.error("client; {}", e.getMessage());
				}
			}
		};

		server.showtime();
		client.showtime();

		synchronized (client) {
			client.wait();
		}

		client.shutdown();
		server.shutdown();

	}

	// #########################################################

	final ServiceFactory factory2 = new ServiceFactory() {
		@Override
		public StreamService newService(SocketUDT connectorSocket)
				throws Exception {
			return new StreamService(connectorSocket) {
				@Override
				public void run() {
					final int size = 1234;
					final byte[] array = new byte[size];
					while (true) {
						try {
							final int count = streamIn.read(array);
							streamOut.write(array, 0, count);
						} catch (IOException e) {
							log.error("server; {}", e.getMessage());
							break;
						}
					}
				}
			};
		}
	};

	@Test
	public void testStream22() throws Exception {

		InetSocketAddress serverAddress = HelperUtils.getLocalSocketAddress();

		StreamServer server = new StreamServer(TypeUDT.DATAGRAM, serverAddress,
				factory2);

		StreamClient client = new StreamClient(TypeUDT.DATAGRAM, serverAddress) {
			@Override
			public void run() {
				final Random random = new Random();
				final int loop = 10000;
				final int size = 1000;
				final byte[] arrayOut = new byte[size];
				final byte[] arrayIn = new byte[size];
				try {
					final StopWatch timer = new StopWatch();
					timer.start();
					for (int k = 0; k < loop; k++) {
						random.nextBytes(arrayOut);
						streamOut.write(arrayOut);
						final int count = streamIn.read(arrayIn);
						assertEquals(count, size);
						assertTrue(Arrays.equals(arrayIn, arrayOut));
					}
					timer.stop();
					log.info("timer : {}", timer.nanoString());
					synchronized (this) {
						this.notifyAll();
					}
				} catch (Exception e) {
					log.error("client; {}", e.getMessage());
				}
			}
		};

		server.showtime();
		client.showtime();

		synchronized (client) {
			client.wait();
		}

		client.shutdown();
		server.shutdown();

	}

	// #########################################################

	// @Test
	public void testStream12() throws Exception {

		InetSocketAddress serverAddress = HelperUtils.getLocalSocketAddress();

		StreamServer server = new StreamServer(TypeUDT.STREAM, serverAddress,
				factory1);

		StreamClient client = new StreamClient(TypeUDT.STREAM, serverAddress) {
			@Override
			public void run() {
				final Random random = new Random();
				final int loop = 3;
				final int size = 100;
				final byte[] arrayOut = new byte[size];
				final byte[] arrayIn = new byte[size];
				try {
					final StopWatch timer = new StopWatch();
					timer.start();
					for (int k = 0; k < loop; k++) {
						random.nextBytes(arrayOut);
						streamOut.write(arrayOut);
						final int count = streamIn.read(arrayIn);
						assertEquals(count, size);
						assertTrue(Arrays.equals(arrayIn, arrayOut));
					}
					timer.stop();
					log.info("timer : {}", timer.nanoString());
					synchronized (this) {
						this.notifyAll();
					}
				} catch (Exception e) {
					log.error("client; {}", e.getMessage());
				}
			}
		};

		server.showtime();
		client.showtime();

		synchronized (client) {
			client.wait();
		}

		client.shutdown();
		server.shutdown();

	}

}
