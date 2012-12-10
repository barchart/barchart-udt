package com.barchart.udt.it;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class TestLoadSocketUDT {

	static final Logger log = LoggerFactory.getLogger(TestLoadSocketUDT.class);

	static void logClassPath() {

		String classPath = System.getProperty("java.class.path");

		String[] entries = classPath.split(File.pathSeparator);

		StringBuilder text = new StringBuilder(1024);

		for (String item : entries) {
			text.append("\n\t");
			text.append(item);
		}

		log.info("{}", text);
	}

	@Test
	public void testLoadLibs() {

		log.info("this example tests if barchart-udt maven dependency works");

		logClassPath();

		try {

			final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			log.info("made socketID={}", socket.getSocketId());

			log.info("socket status={}", socket.getStatus());

			log.info("socket isOpen={}", socket.isOpen());

			log.info("socket isBlocking={}", socket.isBlocking());

			log.info("socket options{}", socket.toStringOptions());

			assertTrue(true);

		} catch (Throwable e) {

			log.error("", e);

			fail("can not make socket");

		}

	}

}
