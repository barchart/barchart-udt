package com.barchart.udt.net;

import com.barchart.udt.SocketUDT;

/**
 * custom/common acceptor/connector socket features
 */
public interface IceCommon {

	/**
	 * expose underlying socket
	 */
	SocketUDT socketUDT();

}
