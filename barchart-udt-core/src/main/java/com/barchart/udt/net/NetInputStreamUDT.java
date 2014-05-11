/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.IllegalBlockingModeException;

import com.barchart.udt.ErrorUDT;
import com.barchart.udt.SocketUDT;

/**
 * {@link InputStream} implementation for UDT sockets.
 */
public class NetInputStreamUDT extends InputStream {

	protected final SocketUDT socketUDT;

	/**
	 * 
	 * @param socketUDT
	 *            The UDT socket.
	 */
	public NetInputStreamUDT(final SocketUDT socketUDT) {

		if (!socketUDT.isBlocking()) {
			throw new IllegalBlockingModeException();
		}

		this.socketUDT = socketUDT;

	}

	@Override
	public int read() throws IOException {

		/*
		 * Here's the contract from the JavaDoc on this for SocketChannel:
		 * 
		 * A read operation might not fill the buffer, and in fact it might not
		 * read any bytes at all. Whether or not it does so depends upon the
		 * nature and state of the channel. A socket channel in non-blocking
		 * mode, for example, cannot read any more bytes than are immediately
		 * available from the socket's input buffer; similarly, a file channel
		 * cannot read any more bytes than remain in the file. It is guaranteed,
		 * however, that if a channel is in blocking mode and there is at least
		 * one byte remaining in the buffer then this method will block until at
		 * least one byte is read.
		 * 
		 * Long story short: This UDT InputStream should only ever be created
		 * when the SocketChannel's in blocking mode, and when it's in blocking
		 * mode the SocketChannel read call below will block just like we need
		 * it too.
		 */

		final byte[] data = new byte[1];

		final int count = read(data);

		assert count == 1;

		return data[0] & 0xFF;

	}

	@Override
	public int read(final byte[] bytes) throws IOException {

		return read(bytes, 0, bytes.length);

	}

	@SuppressWarnings("serial")
	@Override
	public int read(final byte[] bytes, final int off, final int len)
			throws IOException {

		final int count = socketUDT.receive(bytes, off, off + len);

		if (count > 0) {
			assert count <= len;
			return count;
		}

		if (count == 0) {
			throw new ExceptionReceiveUDT(socketUDT.id(),
					ErrorUDT.USER_DEFINED_MESSAGE, "UDT receive time out") {
			};
		}

		throw new IllegalStateException("should not happen");

	}

	@Override
	public void close() throws IOException {
		socketUDT.close();
	}

	@Override
	public int available() throws IOException {
		// This is the default InputStream return value.
		// The java/net/SocketInputStream.java implementation delegates to
		// the native implementation, which returns 0 on at least some OSes.
		return 0;
	}

	@Override
	public long skip(final long numbytes) throws IOException {
		if (numbytes <= 0) {
			return 0;
		}
		long n = numbytes;
		final int buflen = (int) Math.min(1024, n);
		final byte data[] = new byte[buflen];
		while (n > 0) {
			final int r = read(data, 0, (int) Math.min(buflen, n));
			if (r < 0) {
				break;
			}
			n -= r;
		}
		return numbytes - n;
	}

	@Override
	public void mark(final int readlimit) {
		throw new UnsupportedOperationException("mark not supported");
	}

	@Override
	public void reset() throws IOException {
		throw new UnsupportedOperationException("reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}
