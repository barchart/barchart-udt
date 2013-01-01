package com.barchart.udt;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

import com.barchart.udt.EpollUDT.Opt;

public class TestEpollMask extends TestAny {

	@Test
	public void epollUpdate() throws Exception {

		epollUpdate(Opt.NONE);
		epollUpdate(Opt.READ);
		epollUpdate(Opt.WRITE);

		// epollMask(Opt.ERROR);

		epollUpdate(Opt.BOTH);

		// epollMask(Opt.ALL);

	}

	private void epollUpdate(final Opt opt) throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);

		SocketUDT.epollRemove0(epollID, client.id());

		SocketUDT.epollUpdate0(epollID, client.id(), opt.code);

		final int code = SocketUDT.epollVerify0(epollID, client.id());

		log.info("code {}", code);

		assertEquals(code, opt.code);

		SocketUDT.epollRemove0(epollID, client.id());

		SocketUDT.epollRelease0(epollID);

	}

	/**
	 * confirm can update and verify epoll mask
	 * <p>
	 * requires src/main/patches/udt-4.10/*
	 */
	@Test
	public void epollVerify() throws Exception {

		epollVerify(Opt.NONE);
		epollVerify(Opt.READ);
		epollVerify(Opt.WRITE);

		// epollMask(Opt.ERROR);

		epollVerify(Opt.BOTH);

		// epollMask(Opt.ALL);

	}

	private void epollVerify(final Opt opt) throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);

		SocketUDT.epollAdd0(epollID, client.id(), EpollUDT.Opt.NONE.code);

		SocketUDT.epollUpdate0(epollID, client.id(), opt.code);

		final int code = SocketUDT.epollVerify0(epollID, client.id());

		log.info("code {}", code);

		assertEquals(code, opt.code);

		SocketUDT.epollRemove0(epollID, client.id());

		SocketUDT.epollRelease0(epollID);

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
