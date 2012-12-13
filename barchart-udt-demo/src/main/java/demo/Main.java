package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class Main {

	static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) throws Exception {

		log.info("init");

		TestHelp.logOsArch();
		TestHelp.logClassPath();
		TestHelp.logLibraryPath();

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		log.info("isOpen : {}", socket.isOpen());

		log.info("done");

	}

}
