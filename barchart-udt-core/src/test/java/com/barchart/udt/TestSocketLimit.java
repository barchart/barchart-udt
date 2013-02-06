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
import util.UnitHelp;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

// FIXME increase ulimit on build farm
@Ignore
public class TestSocketLimit extends TestAny {

	private void socketBindLimit(final int limit) throws Exception {

		final List<SocketUDT> list = new ArrayList<SocketUDT>();

		allocate: for (int index = 0; index < limit; index++) {

			final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			try {

				socket.bind(UnitHelp.localSocketAddress());

			} catch (final ExceptionUDT e) {

				switch (e.getError()) {
				case ETHREAD:
					log.error("reached udt limit : {}", e.getMessage());
					break allocate;
				default:
					throw e;
				}

			} catch (final Throwable e) {
				log.error("reached system limit : {}", e.getMessage());
				break allocate;
			}

			list.add(socket);

		}

		log.info("socket limit : {}", list.size());

		for (final SocketUDT socket : list) {
			socket.close();
		}

	}

	@Test
	public void socketBindLimt() throws Exception {
		socketBindLimit(SocketUDT.DEFAULT_MAX_SELECTOR_SIZE);
	}

	@Test
	public void sigar() throws Exception {

		// Sigar.getPid();

	}

	@BeforeClass
	public static void setUpClass() throws Exception {

		final Logger logBack = (Logger) SocketUDT.log;
		logBack.setLevel(Level.INFO);

	}

}
