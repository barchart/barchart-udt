package com.barchart.udt;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import util.TestAny;
import util.UnitHelp;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

// FIXME
public class TestSocketLimit extends TestAny {

	private void socketBindLimit(final int limit) throws Exception {

		final List<SocketUDT> list = new ArrayList<SocketUDT>();

		for (int index = 0; index < limit; index++) {

			log.info("index {}", index);

			final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			socket.bind(UnitHelp.localSocketAddress());

			list.add(socket);

		}

	}

	@Test
	public void socketBindLimt() throws Exception {
		// socketBindLimit(SocketUDT.DEFAULT_MAX_SELECTOR_SIZE);
	}

	@BeforeClass
	public static void setUpClass() throws Exception {

		final Logger logBack = (Logger) SocketUDT.log;
		logBack.setLevel(Level.INFO);

	}

}
