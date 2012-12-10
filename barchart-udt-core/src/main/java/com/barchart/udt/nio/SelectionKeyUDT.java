/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
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
