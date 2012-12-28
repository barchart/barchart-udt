/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.nio.channels.Channel;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

/**
 * convenience interface shared by client, server, rendezvous
 */
public interface ChannelUDT extends Channel {

	KindUDT kindUDT();

	SocketUDT socketUDT();

	/**
	 * was connection attempt acknowledged by
	 * {@link SocketChannelUDT#finishConnect()}
	 */
	boolean isConnectFinished();

	int validOps();

	SelectorProviderUDT providerUDT();

	TypeUDT typeUDT();

}
