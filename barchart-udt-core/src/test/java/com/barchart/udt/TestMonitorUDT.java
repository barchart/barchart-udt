package com.barchart.udt;

import static com.barchart.udt.util.HelperUtils.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMonitorUDT {

	private static final Logger log = LoggerFactory
			.getLogger(TestMonitorUDT.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMonitor() {

		try {

			SocketUDT serverSocket = new SocketUDT(TypeUDT.DATAGRAM);
			InetSocketAddress serverAddress = getLocalSocketAddress();
			serverSocket.bind(serverAddress);
			serverSocket.listen(1);

			SocketUDT clientSocket = new SocketUDT(TypeUDT.DATAGRAM);
			InetSocketAddress clientAddress = getLocalSocketAddress();
			clientSocket.bind(clientAddress);

			clientSocket.connect(serverAddress);

			SocketUDT acceptSocket = serverSocket.accept();

			log.info("client montitor={}", clientSocket.toStringMonitor());
			log.info("accept montitor={}", acceptSocket.toStringMonitor());

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

}
