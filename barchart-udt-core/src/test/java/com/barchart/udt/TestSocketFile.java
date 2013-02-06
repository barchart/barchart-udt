/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;
import static util.UnitHelp.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import util.TestAny;

/**
 * FIXME
 */
@Ignore
public class TestSocketFile extends TestAny {

	/**
	 * verify basic file send/receive
	 */
	@Test(timeout = 10 * 1000)
	public void fileTransfer() throws Exception {

		final InetSocketAddress addr1 = localSocketAddress();
		final InetSocketAddress addr2 = localSocketAddress();

		final SocketUDT peer1 = new SocketUDT(TypeUDT.STREAM);
		final SocketUDT peer2 = new SocketUDT(TypeUDT.STREAM);

		peer1.setBlocking(false);
		peer2.setBlocking(false);

		peer1.setRendezvous(true);
		peer2.setRendezvous(true);

		peer1.bind(addr1);
		peer2.bind(addr2);

		socketAwait(peer1, StatusUDT.OPENED);
		socketAwait(peer2, StatusUDT.OPENED);

		peer1.connect(addr2);
		peer2.connect(addr1);

		socketAwait(peer1, StatusUDT.CONNECTED);
		socketAwait(peer2, StatusUDT.CONNECTED);

		log.info("state 0 - connected");
		log.info("peer1 : {}", peer1);
		log.info("peer2 : {}", peer2);

		final int size = 64 * 1024;

		final Random random = new Random(0);
		final byte[] array1 = new byte[size];
		final byte[] array2 = new byte[size];
		random.nextBytes(array1);
		random.nextBytes(array2);

		final File folder = new File("./target/file");
		folder.mkdirs();

		final File source = File.createTempFile("source", "data", folder);
		final File target = File.createTempFile("target", "data", folder);

		FileUtils.writeByteArrayToFile(source, array1);
		FileUtils.writeByteArrayToFile(target, array2);

		assertEquals(size, source.length());
		assertEquals(size, target.length());

		assertFalse("files are different",
				FileUtils.contentEquals(source, target));

		// sender
		final Runnable task1 = new Runnable() {
			@Override
			public void run() {
				try {
					log.info("init send");
					final long length = peer1.sendFile(source, 0, size);
					assertEquals(length, size);
				} catch (final Exception e) {
					log.error("", e);
				}
			}
		};

		// receiver
		final Runnable task2 = new Runnable() {
			@Override
			public void run() {
				try {
					log.info("init recv");
					final long length = peer2.receiveFile(target, 0, size);
					assertEquals(length, size);
				} catch (final Exception e) {
					log.error("", e);
				}
			}
		};

		final ExecutorService executor = Executors.newFixedThreadPool(2);

		executor.submit(task1);
		executor.submit(task2);

		Thread.sleep(5 * 1000);

		executor.shutdownNow();

		assertTrue("files are the same",
				FileUtils.contentEquals(source, target));

		peer1.close();
		peer2.close();

	}
}
