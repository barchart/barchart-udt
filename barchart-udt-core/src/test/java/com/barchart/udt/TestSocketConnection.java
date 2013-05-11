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

import org.junit.Test;

import util.TestAny;

public class TestSocketConnection extends TestAny {

	@Test(timeout = 3 * 1000)
	public void connectionLifeCycle() throws Exception {

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.setBlocking(false);
		accept.bind(localSocketAddress());

		socketAwait(accept, StatusUDT.OPENED);

		accept.listen(1);

		socketAwait(accept, StatusUDT.LISTENING);

		assertEquals(StatusUDT.LISTENING, accept.status());

		assertNull(accept.accept());

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.setBlocking(false);
		client.bind(localSocketAddress());

		socketAwait(client, StatusUDT.OPENED);

		client.connect(accept.getLocalSocketAddress());

		socketAwait(client, StatusUDT.CONNECTED);

		final SocketUDT server = accept.accept();
		assertNotNull(server);
		assertNull(accept.accept());

		socketAwait(server, StatusUDT.CONNECTED);

		log.info("### step 1: both server and client are connected");
		log.info("client = {}", client);
		log.info("server = {}", server);

		client.close();

		socketAwait(client, StatusUDT.CLOSED);
		socketAwait(server, StatusUDT.BROKEN);

		log.info("### step 2: server must be broken after and client is closed");
		log.info("client = {}", client);
		log.info("server = {}", server);

		server.close();

		socketAwait(server, StatusUDT.CLOSED);

		log.info("### step 3: server must be closed after server is closed");
		log.info("client = {}", client);
		log.info("server = {}", server);

		accept.close();

	}

}
