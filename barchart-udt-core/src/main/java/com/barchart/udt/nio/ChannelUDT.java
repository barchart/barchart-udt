/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.io.IOException;

import com.barchart.udt.SocketUDT;

/**
 */
public interface ChannelUDT {

	KindUDT kindUDT();

	SocketUDT socketUDT();

	void close() throws IOException;

	boolean isOpenSocketUDT();

	/**
	 * was connection attempt acknowledged by
	 * {@link ChannelSocketUDT#finishConnect()}
	 */
	boolean isConnectFinished();

	public int validOps();

}
