/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import util.TestAny;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.barchart.udt.EpollUDT.Opt;

//FIXME increase ulimit on build farm
@Ignore
public class TestEpollLimit extends TestAny {

	private void epollWait0_Limit(final int limit) throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		final List<SocketUDT> list = new ArrayList<SocketUDT>();

		for (int k = 0; k < limit; k++) {
			final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
			// socket.bind(UnitHelp.localSocketAddress());
			list.add(socket);
		}

		for (int k = 0; k < limit; k++) {
			final int id = list.get(k).id();
			SocketUDT.epollAdd0(epollID, id, Opt.BOTH.code);
		}

	}

	@Test(expected = ExceptionUDT.class)
	public void epollWait0_Limit_ERR() throws Exception {
		epollWait0_Limit(SocketUDT.DEFAULT_MAX_SELECTOR_SIZE + 1);
	}

	@Test
	public void epollWait0_Limit_OK() throws Exception {
		epollWait0_Limit(SocketUDT.DEFAULT_MAX_SELECTOR_SIZE);
	}

	@BeforeClass
	public static void setUpClass() throws Exception {

		final Logger logBack = (Logger) SocketUDT.log;
		logBack.setLevel(Level.INFO);

	}

}
