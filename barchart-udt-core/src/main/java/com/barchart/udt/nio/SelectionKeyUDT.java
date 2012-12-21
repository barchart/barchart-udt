/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;

/**
 * NOTE: 1<->1 mapping between: SelectionKeyUDT - ChannelUDT - SocketUDT
 */
public class SelectionKeyUDT extends AbstractSelectionKey {

	/** bound selector */
	private final SelectorUDT selectorUDT;

	/** bound channel */
	private final ChannelUDT channelUDT;

	protected SelectionKeyUDT( //
			final SelectorUDT selectorUDT, //
			final ChannelUDT channelUDT, //
			final Object attachment, //
			final int interestOps //
	) {

		this.selectorUDT = selectorUDT;
		this.channelUDT = channelUDT;
		super.attach(attachment);
		this.interestOps = interestOps;

		channelUDT.bindKey(this);

	}

	protected volatile int interestOps;
	protected volatile int readyOps;

	//

	@Override
	public SelectableChannel channel() {
		return (SelectableChannel) channelUDT;
	}

	@Override
	public int interestOps() {
		if (!isValid()) {
			throw new CancelledKeyException();
		}
		return interestOps;
	}

	@Override
	public SelectionKey interestOps(final int ops) {
		if (!isValid()) {
			throw new CancelledKeyException();
		}
		if ((ops & ~(channel().validOps())) != 0) {
			throw new IllegalArgumentException("invalid ops");
		}
		if (((ops & OP_CONNECT) != 0) && socketUDT().isConnected()) {
			throw new IllegalArgumentException("already connected");
		}
		interestOps = ops;
		return this;
	}

	@Override
	public int readyOps() {
		if (!isValid()) {
			throw new CancelledKeyException();
		}
		return readyOps;
	}

	protected void readyOps(final int ops) {
		readyOps = ops;
	}

	//

	@Override
	public int hashCode() {
		return socketId();
	}

	@Override
	public boolean equals(final Object otherKey) {
		if (otherKey instanceof SelectionKeyUDT) {
			final SelectionKeyUDT other = (SelectionKeyUDT) otherKey;
			return other.socketId() == this.socketId();
		}
		return false;
	}

	//

	@Override
	public String toString() {

		InetSocketAddress local;
		try {
			local = socketUDT().getLocalSocketAddress();
		} catch (final ExceptionUDT e) {
			local = null;
		}

		InetSocketAddress remote;
		try {
			remote = socketUDT().getRemoteSocketAddress();
		} catch (final ExceptionUDT e) {
			remote = null;
		}

		final StringBuilder text = new StringBuilder(128);

		text.append("{");

		text.append("socketID=");
		text.append(socketId());
		text.append(",");

		text.append("kind=");
		text.append(channelUDT.kindUDT());
		text.append(",");

		text.append("readyOps=");
		text.append(toStringOps(readyOps));
		text.append(",");

		text.append("interestOps=");
		text.append(toStringOps(interestOps));
		text.append(",");

		text.append("local=");
		text.append(local);
		text.append(",");

		text.append("remote=");
		text.append(remote);

		text.append("}");

		return text.toString();

	}

	@Override
	public Selector selector() {
		return selectorUDT;
	}

	protected int socketId() {
		return socketUDT().getSocketId();
	}

	protected KindUDT kindUDT() {
		return channelUDT.kindUDT();
	}

	protected SocketUDT socketUDT() {
		return channelUDT.socketUDT();
	}

	public ChannelUDT channelUDT() {
		return channelUDT;
	}

	public static final String toStringOps(final int keyOps) {
		final char A = (OP_ACCEPT & keyOps) != 0 ? 'A' : '-';
		final char C = (OP_CONNECT & keyOps) != 0 ? 'C' : '-';
		final char R = (OP_READ & keyOps) != 0 ? 'R' : '-';
		final char W = (OP_WRITE & keyOps) != 0 ? 'W' : '-';
		return String.format("%c%c%c%c", A, C, R, W);
	}

}
