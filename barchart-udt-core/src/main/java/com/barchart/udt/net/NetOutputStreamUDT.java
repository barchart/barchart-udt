/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.IllegalBlockingModeException;

import com.barchart.udt.ErrorUDT;
import com.barchart.udt.SocketUDT;

/**
 * {@link OutputStream} for UDT sockets.
 */
public class NetOutputStreamUDT extends OutputStream {

	protected final SocketUDT socketUDT;

	/**
	 * 
	 * @param socketUDT
	 *            The UDT socket.
	 */
	public NetOutputStreamUDT(final SocketUDT socketUDT) {

		if (!socketUDT.isBlocking()) {
			throw new IllegalBlockingModeException();
		}

		this.socketUDT = socketUDT;

	}

	@Override
	public void write(final int b) throws IOException {

		// Just cast it -- this is the same thing SocketOutputStream does.
		final byte[] bytes = { (byte) b };

		write(bytes);

	}

	@Override
	public void write(final byte[] bytes) throws IOException {

		write(bytes, 0, bytes.length);

	}

	@Override
	public void write(final byte[] bytes, final int off, final int len)
			throws IOException {

		int bytesRemaining = len;

		while (bytesRemaining > 0) {

			final int count = socketUDT.send(bytes, off + len - bytesRemaining,
					off + len);

			if (count > 0) {
				bytesRemaining -= count;
				continue;
			}

			if (count == 0) {
				throw new ExceptionSendUDT(socketUDT.id(),
						ErrorUDT.USER_DEFINED_MESSAGE, "UDT send time out");
			}

			throw new IllegalStateException(
					"Socket has been chaged to non-blocking");
		}

	}

	@Override
	public void close() throws IOException {
		socketUDT.close();
	}

	@Override
	public void flush() throws IOException {
		socketUDT.flush();
	}
}
