/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

public class TestEpollBasic extends TestAny {

	/**
	 * NO exception
	 * <p>
	 * "If a socket is already in the epoll set, it will be ignored if being
	 * added again. Adding invalid or closed sockets will cause error. However,
	 * they will simply be ignored without any error returned when being
	 * removed."
	 * 
	 * */
	@Test
	public void epollAdd0_AgainSocketException() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		//

		final int epollID = SocketUDT.epollCreate0();

		assertTrue(epollID > 0);

		SocketUDT.epollAdd0(epollID, socket.id(), EpollUDT.Opt.BOTH.code);
		SocketUDT.epollAdd0(epollID, socket.id(), EpollUDT.Opt.BOTH.code);
		SocketUDT.epollAdd0(epollID, socket.id(), EpollUDT.Opt.BOTH.code);

		SocketUDT.epollRelease0(epollID);

	}

	/**
	 * YES exception; see http://udt.sourceforge.net/udt4/index.htm
	 * <p>
	 * "If a socket is already in the epoll set, it will be ignored if being
	 * added again. Adding invalid or closed sockets will cause error. However,
	 * they will simply be ignored without any error returned when being
	 * removed."
	 */
	@Test(expected = ExceptionUDT.class)
	public void epollAdd0_InvalidSocketException() throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		SocketUDT.epollAdd0(epollID, -1, EpollUDT.Opt.BOTH.code);

		SocketUDT.epollRelease0(epollID);

	}

	/** no exceptions is pass */
	@Test
	public void epollAdd0_Remove() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		//

		final int epollID = SocketUDT.epollCreate0();

		assertTrue(epollID > 0);

		SocketUDT.epollAdd0(epollID, socket.id(), EpollUDT.Opt.BOTH.code);

		SocketUDT.epollRemove0(epollID, socket.id());

		SocketUDT.epollRelease0(epollID);

	}

	/** no exceptions is pass */
	@Test
	public void epollCreate0() throws Exception {

		final int epollID = SocketUDT.epollCreate0();
		SocketUDT.epollRelease0(epollID);

	}

	@Test(expected = ExceptionUDT.class)
	public void epollRelease0() throws Exception {

		SocketUDT.epollRelease0(-1);

	}

	/**
	 * NO exception; see http://udt.sourceforge.net/udt4/index.htm
	 * <p>
	 * "If a socket is already in the epoll set, it will be ignored if being
	 * added again. Adding invalid or closed sockets will cause error. However,
	 * they will simply be ignored without any error returned when being
	 * removed."
	 */
	@Test
	public void epollRemove0_IvalidSocketException() throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		SocketUDT.epollRemove0(epollID, -1);
		SocketUDT.epollRemove0(epollID, -2);
		SocketUDT.epollRemove0(epollID, -3);

		SocketUDT.epollRelease0(epollID);

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
