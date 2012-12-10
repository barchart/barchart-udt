package com.barchart.udt.net;

import com.barchart.udt.SocketUDT;

interface ServiceFactory {

	StreamService newService(SocketUDT socket) throws Exception;

}
