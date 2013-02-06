/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import static org.junit.Assert.*;

import java.nio.channels.SelectionKey;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import util.TestAny;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.barchart.udt.EpollUDT;

/** FIXME */
@Ignore
public class TestSelectionKeyUDT extends TestAny {

	@BeforeClass
	public static void startup() throws Exception {

		final Logger root = (Logger) LoggerFactory
				.getLogger(Logger.ROOT_LOGGER_NAME);

		root.setLevel(Level.ERROR);

	}

	@AfterClass
	public static void shutdown() throws Exception {

		final Logger root = (Logger) LoggerFactory
				.getLogger(Logger.ROOT_LOGGER_NAME);

		root.setLevel(Level.DEBUG);

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAccept() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final SelectorUDT selector = provider.openSelector();

		final ChannelUDT channel = provider.openServerSocketChannel();

		final SelectionKeyUDT keyUDT = //
		new SelectionKeyUDT(selector, channel, null);

		assertTrue(keyUDT.isValid());
		assertEquals(EpollUDT.Opt.NONE, keyUDT.epollOpt());

		keyUDT.interestOps(SelectionKey.OP_ACCEPT);

		assertEquals(EpollUDT.Opt.READ, keyUDT.epollOpt());

		assertTrue("accept valid", keyUDT.doRead(1));
		assertFalse("invalid accept", keyUDT.doWrite(1));

		selector.close();

	}

	@Test
	public void testConnect() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final SelectorUDT selector = provider.openSelector();

		final ChannelUDT channel = provider.openSocketChannel();

		final SelectionKeyUDT keyUDT = //
		new SelectionKeyUDT(selector, channel, null);

		assertTrue(keyUDT.isValid());
		assertEquals(EpollUDT.Opt.NONE, keyUDT.epollOpt());
		// assertFalse("not connect", keyUDT.isBeyondConnect());

		keyUDT.interestOps(SelectionKey.OP_CONNECT);

		assertEquals(EpollUDT.Opt.WRITE, keyUDT.epollOpt());

		assertFalse("surious read", keyUDT.doRead(1));
		assertTrue("connect 1 ok", keyUDT.doWrite(1));
		// assertTrue("connected", keyUDT.isBeyondConnect());
		assertFalse("connect 2 fail", keyUDT.doWrite(2));
		// assertTrue("connected", keyUDT.isBeyondConnect());

		selector.close();

	}

	@Test
	public void testRead() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final SelectorUDT selector = provider.openSelector();

		final ChannelUDT channel = provider.openSocketChannel();

		final SelectionKeyUDT keyUDT = //
		new SelectionKeyUDT(selector, channel, null);

		assertTrue(keyUDT.isValid());
		assertEquals(EpollUDT.Opt.NONE, keyUDT.epollOpt());
		// assertFalse("not connect", keyUDT.isBeyondConnect());

		keyUDT.interestOps(SelectionKey.OP_READ | SelectionKey.OP_CONNECT);

		assertEquals(EpollUDT.Opt.BOTH, keyUDT.epollOpt());

		assertTrue("connect ok", keyUDT.doWrite(1));
		// assertTrue("connected", keyUDT.isBeyondConnect());

		assertTrue("valid read 1", keyUDT.doRead(2));
		assertTrue("valid read 2", keyUDT.doRead(3));

		assertFalse("surious write", keyUDT.doWrite(4));

		selector.close();

	}

	@Test
	public void testWrite() throws Exception {

		final SelectorProviderUDT provider = SelectorProviderUDT.DATAGRAM;

		final SelectorUDT selector = provider.openSelector();

		final ChannelUDT channel = provider.openSocketChannel();

		final SelectionKeyUDT keyUDT = //
		new SelectionKeyUDT(selector, channel, null);

		assertTrue(keyUDT.isValid());
		assertEquals(EpollUDT.Opt.NONE, keyUDT.epollOpt());
		// assertFalse("not connect", keyUDT.isBeyondConnect());

		keyUDT.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);

		assertEquals(EpollUDT.Opt.WRITE, keyUDT.epollOpt());

		assertTrue("connect ok", keyUDT.doWrite(1));
		// assertTrue("connected", keyUDT.isBeyondConnect());

		assertTrue("valid write 1", keyUDT.doWrite(2));
		assertTrue("valid write 2", keyUDT.doWrite(3));

		assertFalse("surious read", keyUDT.doRead(4));

		selector.close();

	}

}
