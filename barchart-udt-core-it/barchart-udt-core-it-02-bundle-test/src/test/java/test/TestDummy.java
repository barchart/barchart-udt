package test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class TestDummy {

	@Test
	public void testLibraryLoad() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		assertTrue(socket.isOpen());

	}

}
