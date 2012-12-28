/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.EpollUDT;
import com.barchart.udt.EpollUDT.Opt;
import com.barchart.udt.SocketUDT;

/**
 * selection key implementation <> FIXME think again about concurrency
 */
public class SelectionKeyUDT extends SelectionKey implements
		Comparable<SelectionKeyUDT> {

	protected static final Logger log = LoggerFactory
			.getLogger(SelectionKeyUDT.class);

	/** bound selector */
	protected final SelectorUDT selectorUDT;

	/** bound channel */
	protected final ChannelUDT channelUDT;

	protected SelectionKeyUDT( //
			final SelectorUDT selectorUDT, //
			final ChannelUDT channelUDT, //
			final Object attachment //
	) {

		this.selectorUDT = selectorUDT;
		this.channelUDT = channelUDT;
		super.attach(attachment);

		makeValid(true);

	}

	/** requested interest */
	private volatile int interestOps;

	/** interest reported as ready */
	private volatile int readyOps;

	//

	@Override
	public SelectableChannel channel() {
		return (SelectableChannel) channelUDT;
	}

	protected EpollUDT epollUDT() {
		return selectorUDT.epollUDT;
	}

	/**
	 * make sure key is not canceled
	 */
	protected void assertValidKey() throws CancelledKeyException {
		if (isValid()) {
			return;
		}
		throw new CancelledKeyException();
	}

	@Override
	public int interestOps() {

		return interestOps;

	}

	/**
	 * make sure only permitted mask bits
	 */
	protected void assertValidOps(final int interestOps) {
		if ((interestOps & ~(channel().validOps())) != 0) {
			throw new IllegalArgumentException("invalid interestOps="
					+ interestOps);
		}
	}

	private volatile Opt epollOpt;

	protected Opt epollOpt() {
		return epollOpt;
	}

	@Override
	public synchronized SelectionKey interestOps(final int interestOps) {

		assertValidKey();
		assertValidOps(interestOps);

		try {

			final Opt epollNew = from(interestOps);

			// log.debug("epollNew : {}", epollNew);

			if (epollNew != epollOpt) {

				// epollUDT().update(socketUDT(), epollNew);

				if (Opt.NONE == epollNew) {
					epollUDT().remove(socketUDT());
				} else {
					epollUDT().remove(socketUDT());
					epollUDT().add(socketUDT(), epollNew);
				}

				epollOpt = epollNew;

			}

			// final Opt epollUdt = epollUDT().verify(socketUDT());
			// log.debug("epollUdt : {}", epollUdt);

		} catch (final Exception e) {

			log.error("epoll failure", e);

		} finally {

			this.interestOps = interestOps;

		}

		return this;

	}

	@Override
	public int readyOps() {

		return readyOps;

	}

	protected void readyOps(final int ops) {

		readyOps = ops;

	}

	protected static boolean interestAccept(final int interestOps) {
		return (interestOps & OP_ACCEPT) != 0;
	}

	protected static boolean interestConnect(final int interestOps) {
		return (interestOps & OP_CONNECT) != 0;
	}

	protected static boolean interestRead(final int interestOps) {
		return (interestOps & OP_READ) != 0;
	}

	protected static boolean interestWrite(final int interestOps) {
		return (interestOps & OP_WRITE) != 0;
	}

	/** read and write correlation counter */
	private volatile int processCount;

	protected void logError(final String comment) {

		final String message = "logic error : \n\t" + this;

		log.warn(message, new Exception("" + comment));

	}

	/**
	 * note: read interest is reported before write interest
	 * 
	 * @return should report state change?
	 */
	protected boolean doRead(final int processCount) {

		// log.debug("read  {}", this);

		int readyOps = 0;
		final int interestOps = this.interestOps;
		this.processCount = processCount;

		try {

			if (!epollOpt.hasRead()) {
				if (interestOps == 0) {
					logError("error report when missing interest");
					return false;
				} else {
					readyOps = interestOps;
					return true;
				}
			}

			switch (kindUDT()) {
			case ACCEPTOR:
				if (interestAccept(interestOps)) {
					readyOps = OP_ACCEPT;
					return true;
				} else {
					logError("ready to ACCEPT while not interested");
					return false;
				}
			case CONNECTOR:
				if (interestRead(interestOps)) {
					readyOps = OP_READ;
					return true;
				} else {
					logError("ready to READ while not interested");
					return false;
				}
			default:
				logError("wrong kind");
				return false;
			}

		} finally {

			this.readyOps = readyOps;

		}

	}

	/**
	 * note: write interest is reported after read interest
	 * 
	 * @return should report state change?
	 */
	protected boolean doWrite(final int processCount) {

		// log.debug("write {}", this);

		int readyOps = 0;
		final int interestOps = this.interestOps;
		final boolean hadReadBeforeWrite = this.processCount == processCount;

		try {

			if (!epollOpt.hasWrite()) {
				if (interestOps == 0) {
					logError("error report when missing interest");
					return false;
				} else {
					readyOps = interestOps;
					return true;
				}
			}

			switch (kindUDT()) {
			case ACCEPTOR:
				logError("ready to WRITE for acceptor");
				return false;
			case CONNECTOR:
				if (channelUDT().isConnectFinished()) {
					if (interestWrite(interestOps)) {
						readyOps = OP_WRITE;
						return true;
					} else {
						logError("ready to WRITE when not insterested");
						return false;
					}
				} else {
					if (interestConnect(interestOps)) {
						readyOps = OP_CONNECT;
						return true;
					} else {
						logError("ready to CONNECT when not interested");
						return false;
					}
				}
			default:
				logError("wrong kind");
				return false;
			}

		} finally {
			if (hadReadBeforeWrite) {
				this.readyOps |= readyOps;
			} else {
				this.readyOps = readyOps;
			}
		}

	}

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

	@Override
	public String toString() {

		return String
				.format("[id: 0x%08x] poll=%s ready=%s inter=%s %s %s %s bind=%s:%s peer=%s:%s", //
						socketUDT().id(), //
						epollOpt, //
						toStringOps(readyOps), //
						toStringOps(interestOps), //
						channelUDT.typeUDT(), //
						channelUDT.kindUDT(), //
						socketUDT().status(), //
						socketUDT().getLocalInetAddress(), //
						socketUDT().getLocalInetPort(), //
						socketUDT().getRemoteInetAddress(), //
						socketUDT().getRemoteInetPort() //
				);

	}

	@Override
	public Selector selector() {
		return selectorUDT;
	}

	protected int socketId() {
		return socketUDT().id();
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

	public static final String toStringOps(final int selectOps) {
		final char A = (OP_ACCEPT & selectOps) != 0 ? 'A' : '-';
		final char C = (OP_CONNECT & selectOps) != 0 ? 'C' : '-';
		final char R = (OP_READ & selectOps) != 0 ? 'R' : '-';
		final char W = (OP_WRITE & selectOps) != 0 ? 'W' : '-';
		return String.format("%c%c%c%c", A, C, R, W);
	}

	protected static final int HAS_READ = OP_ACCEPT | OP_READ;
	protected static final int HAS_WRITE = OP_CONNECT | OP_WRITE;

	/**
	 * select options : jdk to epoll
	 */
	protected static Opt from(final int interestOps) {

		final boolean hasRead = (interestOps & HAS_READ) != 0;
		final boolean hasWrite = (interestOps & HAS_WRITE) != 0;

		if (hasRead && hasWrite) {
			return Opt.BOTH;
		}

		if (hasRead) {
			return Opt.READ;
		}

		if (hasWrite) {
			return Opt.WRITE;
		}

		return Opt.NONE;

	}

	/** is key not canceled? */
	private volatile boolean isValid;

	@Override
	public boolean isValid() {
		return isValid;
	}

	/** add/remove poll/socket registration */
	protected void makeValid(final boolean isValid) {
		try {
			if (isValid) {
				epollOpt = Opt.NONE;
				epollUDT().add(socketUDT(), epollOpt);
			} else {
				epollUDT().remove(socketUDT());
			}
		} catch (final Exception e) {
			log.error("epoll failure", e);
		} finally {
			this.isValid = isValid;
		}
	}

	@Override
	public void cancel() {
		if (isValid()) {
			selectorUDT.cancel(this);
		}
	}

	@Override
	public int compareTo(final SelectionKeyUDT that) {
		final int thisId = this.socketId();
		final int thatId = that.socketId();
		if (thisId > thatId) {
			return +1;
		}
		if (thisId < thatId) {
			return -1;
		}
		return 0;
	}

}
