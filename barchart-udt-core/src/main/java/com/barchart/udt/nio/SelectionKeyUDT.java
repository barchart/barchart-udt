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
 * NOTE: 1<->1 mapping between: SelectionKeyUDT -> ChannelUDT -> SocketUDT
 */
public class SelectionKeyUDT extends AbstractSelectionKey {

	final SelectorUDT selectorUDT;
	final ChannelUDT channelUDT;

	/**
	 * used by JNI
	 */
	final int socketID;
	final SocketUDT socketUDT;

	SelectionKeyUDT(SelectorUDT selector, ChannelUDT channel,
			Object attachment, int ops) {

		this.channelUDT = channel;
		this.selectorUDT = selector;
		super.attach(attachment);
		this.interestOps = ops;

		this.socketUDT = channel.getSocketUDT();
		this.socketID = socketUDT.getSocketId();

		if (channel.getChannelKind() == KindUDT.CONNECTOR) {
			// special relationship for connectors
			ChannelSocketUDT socketChannel = (ChannelSocketUDT) channel;
			socketChannel.setRegistredKey(this);
		}

	}

	volatile int interestOps;
	volatile int readyOps;

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
	public SelectionKey interestOps(int ops) {
		if (!isValid()) {
			throw new CancelledKeyException();
		}
		if ((ops & ~(channel().validOps())) != 0) {
			throw new IllegalArgumentException("invalid ops");
		}
		if (((ops & OP_CONNECT) != 0) && socketUDT.isConnected()) {
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

	@Override
	public Selector selector() {
		return selectorUDT;
	}

	//

	@Override
	public int hashCode() {
		return socketID;
	}

	@Override
	public boolean equals(Object otherKey) {
		if (otherKey instanceof SelectionKeyUDT) {
			SelectionKeyUDT other = (SelectionKeyUDT) otherKey;
			return other.socketID == this.socketID;
		}
		return false;
	}

	//

	@Override
	public String toString() {
		InetSocketAddress local;
		try {
			local = socketUDT.getLocalSocketAddress();
		} catch (ExceptionUDT e) {
			local = null;
		}
		InetSocketAddress remote;
		try {
			remote = socketUDT.getRemoteSocketAddress();
		} catch (ExceptionUDT e) {
			remote = null;
		}

		return " socketID=" + socketID
				+ //
				" type=" + channelUDT.getChannelKind()
				+ //
				" readyOps=" + toStringOps(readyOps) + "(" + readyOps + ")"
				+ //
				" ntrstOps=" + toStringOps(interestOps) + "(" + interestOps
				+ ")" + //
				" local=" + local + //
				" remote=" + remote + //
				"";
	}

	public static final String toStringOps(int keyOps) {
		char R = (OP_READ & keyOps) != 0 ? 'R' : '-';
		char W = (OP_WRITE & keyOps) != 0 ? 'W' : '-';
		char C = (OP_CONNECT & keyOps) != 0 ? 'C' : '-';
		char A = (OP_ACCEPT & keyOps) != 0 ? 'A' : '-';
		return String.format("%c%c%c%c", A, C, W, R);
	}

}
