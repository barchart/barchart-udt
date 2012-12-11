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

// NOTE: 1<->1 mapping between: ChannelUDT -> SocketUDT  
interface ChannelUDT {

	SocketUDT getSocketUDT();

	KindUDT getChannelKind();

	void close() throws IOException;

	boolean isOpenSocketUDT();

}
