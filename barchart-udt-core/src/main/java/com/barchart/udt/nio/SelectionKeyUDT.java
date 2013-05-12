/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.EpollUDT;
import com.barchart.udt.EpollUDT.Opt;
import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.OptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.StatusUDT;

/**
 * UDT selection key implementation.
 */
public class SelectionKeyUDT extends SelectionKey implements
		Comparable<SelectionKeyUDT> {

	/**
	 * JDK interest to Epoll READ mapping.
	 */
	protected static final int HAS_READ = OP_ACCEPT | OP_READ;

	/**
	 * JDK interest to Epoll WRITE mapping.
	 */
	protected static final int HAS_WRITE = OP_CONNECT | OP_WRITE;

	protected static final Logger log = LoggerFactory
			.getLogger(SelectionKeyUDT.class);

	/**
	 * Convert select options : from jdk into epoll.
	 */
	protected static Opt from(final int interestOps) {

		final boolean hasRead = (interestOps & HAS_READ) != 0;
		final boolean hasWrite = (interestOps & HAS_WRITE) != 0;

		if (hasRead && hasWrite) {
			return Opt.ALL;
		}

		if (hasRead) {
			return Opt.ERROR_READ;
		}

		if (hasWrite) {
			return Opt.ERROR_WRITE;
		}

		return Opt.ERROR;

	}

	/**
	 * Render select options.
	 */
	public static final String toStringOps(final int selectOps) {
		final char A = (OP_ACCEPT & selectOps) != 0 ? 'A' : '-';
		final char C = (OP_CONNECT & selectOps) != 0 ? 'C' : '-';
		final char R = (OP_READ & selectOps) != 0 ? 'R' : '-';
		final char W = (OP_WRITE & selectOps) != 0 ? 'W' : '-';
		return String.format("%c%c%c%c", A, C, R, W);
	}

	/**
	 * Channel bound to the key.
	 */
	private final ChannelUDT channelUDT;

	/**
	 * Requested interest in epoll format.
	 */
	private volatile Opt epollOpt;

	/**
	 * Requested interest in JDK format.
	 */
	private volatile int interestOps;

	/**
	 * Key validity state. Key is valid when created, and invalid when canceled.
	 */
	private volatile boolean isValid;

	/**
	 * Reported ready interest.
	 */
	private volatile int readyOps;

	/**
	 * Correlation index for {@link #doRead(int)} vs {@link #doWrite(int)}
	 */
	private volatile int resultIndex;

	/**
	 * Selector bound to the key.
	 */
	private final SelectorUDT selectorUDT;

	protected SelectionKeyUDT( //
			final SelectorUDT selectorUDT, //
			final ChannelUDT channelUDT, //
			final Object attachment //
	) {

		super.attach(attachment);

		this.selectorUDT = selectorUDT;
		this.channelUDT = channelUDT;

		makeValid(true);

	}

	/**
	 * Ensure key is NOT canceled.
	 */
	protected void assertValidKey() throws CancelledKeyException {
		if (isValid()) {
			return;
		}
		throw new CancelledKeyException();
	}

	/**
	 * Ensure only permitted interest mask bits are present.
	 */
	protected void assertValidOps(final int interestOps) {
		if ((interestOps & ~(channel().validOps())) != 0) {
			throw new IllegalArgumentException("invalid interestOps="
					+ interestOps);
		}
	}

	@Override
	public void cancel() {
		if (isValid()) {
			selector().cancel(this);
		}
	}

	@Override
	public SelectableChannel channel() {
		return (SelectableChannel) channelUDT;
	}

	/**
	 * Underlying UDT channel.
	 */
	protected ChannelUDT channelUDT() {
		return channelUDT;
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

	/**
	 * Apply READ readiness according to {@link KindUDT} channel role.
	 * <p>
	 * Note: {@link #doRead(int)} is invoked before {@link #doWrite(int)}
	 * <p>
	 * Sockets with exceptions are returned to both read and write sets.
	 * 
	 * @return Should report ready-state change?
	 */
	protected boolean doRead(final int resultIndex) {

		int readyOps = 0;
		final int interestOps = this.interestOps;

		/** Store read/write verifier. */
		this.resultIndex = resultIndex;

		try {

			/** Check error report. */
			if (!epollOpt.hasRead()) {
				if (isSocketBroken()) {
					readyOps = channel().validOps();
					return true;
				} else {
					logError("Unexpected error report.");
					return false;
				}
			}

			switch (kindUDT()) {
			case ACCEPTOR:
				if ((interestOps & OP_ACCEPT) != 0) {
					readyOps = OP_ACCEPT;
					return true;
				} else {
					logError("Ready to ACCEPT while not interested.");
					return false;
				}
			case CONNECTOR:
			case RENDEZVOUS:
				if ((interestOps & OP_READ) != 0) {
					readyOps = OP_READ;
					return true;
				} else {
					logError("Ready to READ while not interested.");
					return false;
				}
			default:
				logError("Wrong kind.");
				return false;
			}

		} finally {

			this.readyOps = readyOps;

		}

	}

	/**
	 * Apply WRITE readiness according to {@link KindUDT} channel role.
	 * <p>
	 * Note: {@link #doRead(int)} is invoked before {@link #doWrite(int)}
	 * <p>
	 * Sockets with exceptions are returned to both read and write sets.
	 * 
	 * @return Should report ready-state change?
	 */
	protected boolean doWrite(final int resultIndex) {

		int readyOps = 0;
		final int interestOps = this.interestOps;

		/** Verify read/write relationship. */
		final boolean hadReadBeforeWrite = this.resultIndex == resultIndex;

		try {

			/** Check error report. */
			if (!epollOpt.hasWrite()) {
				if (isSocketBroken()) {
					readyOps = channel().validOps();
					return true;
				} else {
					logError("Unexpected error report.");
					return false;
				}
			}

			switch (kindUDT()) {
			case ACCEPTOR:
				logError("Ready to WRITE for acceptor.");
				return false;
			case CONNECTOR:
			case RENDEZVOUS:
				if (channelUDT().isConnectFinished()) {
					if ((interestOps & OP_WRITE) != 0) {
						readyOps = OP_WRITE;
						return true;
					} else {
						logError("Ready to WRITE when not insterested.");
						return false;
					}
				} else {
					if ((interestOps & OP_CONNECT) != 0) {
						readyOps = OP_CONNECT;
						return true;
					} else {
						logError("Ready to CONNECT when not interested.");
						return false;
					}
				}
			default:
				logError("Wrong kind.");
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

	/**
	 * Requested interest in epoll format.
	 */
	protected Opt epollOpt() {
		return epollOpt;
	}

	/**
	 * Epoll bound to this key.
	 */
	protected EpollUDT epollUDT() {
		return selector().epollUDT();
	}

	/**
	 * Key equality based on socket-id.
	 */
	@Override
	public boolean equals(final Object otherKey) {
		if (otherKey instanceof SelectionKeyUDT) {
			final SelectionKeyUDT other = (SelectionKeyUDT) otherKey;
			return other.socketId() == this.socketId();
		}
		return false;
	}

	/**
	 * Check socket error condition.
	 */
	boolean hasError() throws ExceptionUDT {
		final int code = socketUDT().getOption(OptionUDT.Epoll_Event_Mask);
		return EpollUDT.Opt.from(code).hasError();
	}

	/**
	 * Key hach code based on socket-id.
	 */
	@Override
	public int hashCode() {
		return socketId();
	}

	@Override
	public int interestOps() {
		return interestOps;
	}

	@Override
	public SelectionKey interestOps(final int interestOps) {

		assertValidKey();
		assertValidOps(interestOps);

		try {

			final Opt epollNew = from(interestOps);

			if (epollNew != epollOpt) {

				if (Opt.ERROR == epollNew) {
					epollUDT().remove(socketUDT());
				} else {
					epollUDT().remove(socketUDT());
					epollUDT().add(socketUDT(), epollNew);
				}

				epollOpt = epollNew;

			}

		} catch (final Exception e) {

			log.error("epoll udpate failure", e);

		} finally {

			this.interestOps = interestOps;

		}

		return this;

	}

	/**
	 * Check socket termination status.
	 * 
	 * @return true if status is {@link StatusUDT#BROKEN} or worse
	 */
	protected boolean isSocketBroken() {
		switch (socketUDT().status()) {
		case INIT:
		case OPENED:
		case LISTENING:
		case CONNECTING:
		case CONNECTED:
			return false;
		case BROKEN:
		case CLOSING:
		case CLOSED:
		case NONEXIST:
			return true;
		default:
			logError("Unknown socket status.");
			return true;
		}
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	/**
	 * Channel role.
	 */
	protected KindUDT kindUDT() {
		return channelUDT.kindUDT();
	}

	/**
	 * Key processing logic error logger.
	 */
	protected void logError(final String comment) {

		final String message = "logic error : \n\t" + this;

		log.warn(message, new Exception("" + comment));

	}

	/**
	 * Change socket registration with epoll, and change key validity status.
	 */
	protected void makeValid(final boolean isValid) {
		try {
			if (isValid) {
				epollOpt = Opt.ERROR;
				epollUDT().add(socketUDT(), epollOpt);
			} else {
				epollUDT().remove(socketUDT());
			}
		} catch (final Throwable e) {
			log.error("Epoll failure.", e);
		} finally {
			this.isValid = isValid;
		}
	}

	@Override
	public int readyOps() {
		return readyOps;
	}

	protected void readyOps(final int ops) {
		readyOps = ops;
	}

	@Override
	public SelectorUDT selector() {
		return selectorUDT;
	}

	/**
	 * Id of a socket bound to this key.
	 */
	protected int socketId() {
		return socketUDT().id();
	}

	/**
	 * Socket bound to this key.
	 */
	protected SocketUDT socketUDT() {
		return channelUDT.socketUDT();
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

}
