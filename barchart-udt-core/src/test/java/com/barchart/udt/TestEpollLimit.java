package com.barchart.udt;

import java.util.ArrayList;
import java.util.List;

import com.barchart.udt.EpollUDT.Opt;

public class TestEpollLimit {

	private void epollWait0_Limit(final int limit) throws Exception {
	
		final int epollID = SocketUDT.epollCreate0();
	
		final List<SocketUDT> list = new ArrayList<SocketUDT>();
	
		for (int k = 0; k < limit; k++) {
			final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
			// socket.bind(UnitHelp.localSocketAddress());
			list.add(socket);
		}
	
		for (int k = 0; k < limit; k++) {
			final int id = list.get(k).getSocketId();
			SocketUDT.epollAdd0(epollID, id, Opt.BOTH.code);
		}
	
	}

	// @Test(expected = ExceptionUDT.class)
	public void epollWait0_Limit_ERR() throws Exception {
		epollWait0_Limit(SocketUDT.DEFAULT_MAX_SELECTOR_SIZE + 1);
	}

	// @Test
	public void epollWait0_Limit_OK() throws Exception {
		epollWait0_Limit(SocketUDT.DEFAULT_MAX_SELECTOR_SIZE);
	}

}
