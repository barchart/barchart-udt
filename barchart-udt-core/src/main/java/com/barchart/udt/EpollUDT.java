/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDT Epoll Manager
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Epoll">Epoll</a>
 * @see <a href="http://udt.sourceforge.net/udt4/doc/epoll.htm">UDT Epoll</a>
 */
public class EpollUDT {

	/**
	 * poll interest option mask
	 * <p>
	 * see udt.h enum - EPOLLOpt
	 * 
	 * <pre>
	 *    UDT_EPOLL_IN = 0x1,
	 *    UDT_EPOLL_OUT = 0x4,
	 *    UDT_EPOLL_ERR = 0x8
	 * </pre>
	 * 
	 * this is subset adapted to jdk select pattern
	 */
	public static enum Opt {

		/**
		 * not interested
		 */
		NONE(0x0), //

		/**
		 * UDT_EPOLL_IN : interested in read
		 */
		READ(0x1), //

		/**
		 * UDT_EPOLL_OUT: interested in write
		 */
		WRITE(0x4), //

		/**
		 * UDT_EPOLL_ERR: interested in exceptions
		 */
		ERROR(0x8), //

		BOTH(WRITE.code | READ.code), //

		ERROR_READ(ERROR.code | READ.code), //

		ERROR_WRITE(ERROR.code | WRITE.code), //

		ALL(ERROR.code | WRITE.code | READ.code), //

		UNKNOWN(-1);

		;

		private static final Opt[] ENUM_VALS = Opt.values();

		public static Opt from(final int code) {
			for (final Opt known : ENUM_VALS) {
				if (known.code == code) {
					return known;
				}
			}
			return UNKNOWN;
		}

		/**
		 * poll event mask;
		 * <p>
		 * used for both requesting interest and reporting readiness
		 */
		public final int code;

		Opt(final int code) {
			this.code = code;
		}

		public boolean hasError() {
			return (code & ERROR.code) != 0;
		}

		public boolean hasRead() {
			return (code & READ.code) != 0;
		}

		public boolean hasWrite() {
			return (code & WRITE.code) != 0;
		}

		/**
		 * Non-empty mask of 3 parts.
		 */
		public boolean isValidInterestRequest() {
			switch (this) {
			case NONE:
			case READ:
			case WRITE:
			case ERROR:
			case BOTH:
			case ERROR_WRITE:
			case ERROR_READ:
			case ALL:
				return true;
			default:
				return false;
			}
		}

	}

	protected static final Logger log = LoggerFactory.getLogger(EpollUDT.class);

	protected final int id;

	protected volatile boolean isActive;

	/**
	 * place holder socket to work around logic in epoll.h CEPoll::wait() which
	 * expects at least one socket being monitored with non empty interest
	 */
	private final SocketUDT socketUDT;

	/**
	 * allocate poll
	 */
	public EpollUDT() throws ExceptionUDT {

		id = SocketUDT.epollCreate0();
		isActive = true;

		socketUDT = new SocketUDT(TypeUDT.DATAGRAM);
		SocketUDT.epollAdd0(id, socketUDT.id(), Opt.BOTH.code);

		log.debug("ep {} create", id());

	}

	/**
	 * deallocate poll; called on {@link #finalize()}
	 */
	public void destroy() throws ExceptionUDT {

		SocketUDT.epollRemove0(id(), socketUDT.id());
		socketUDT.close();

		isActive = false;
		SocketUDT.epollRelease0(id());

		log.debug("ep {} delete", id());

	}

	/**
	 * poll descriptor id
	 */
	public int id() {
		return id;
	}

	/**
	 * poll becomes active after instance creation and inactive after
	 * {@link #destroy()}
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * deallocate poll
	 * <p>
	 * NOTE: catch all exceptions; else prevents GC
	 * <p>
	 * NOTE: do not leak "this" references; else prevents GC
	 */
	@Override
	protected void finalize() {
		try {
			destroy();
			super.finalize();
		} catch (final Throwable e) {
			log.error("failed to destroy id=" + id(), e);
		}
	}

	/**
	 * register socket into event processing poll
	 */
	public void add(final SocketUDT socket, final Opt option)
			throws ExceptionUDT {

		log.debug("ep {} add {} {}", id(), socket, option);

		// assert option.isValidInterestRequest();

		SocketUDT.epollAdd0(id(), socket.id(), option.code);

	}

	/**
	 * unregister socket from event processing poll
	 */
	public void remove(final SocketUDT socket) throws ExceptionUDT {

		log.debug("ep {} rem {}", id(), socket);

		SocketUDT.epollRemove0(id(), socket.id());

	}

	/**
	 * update existing poll/socket registration with changed interest
	 */
	public void update(final SocketUDT socket, final Opt option)
			throws ExceptionUDT {

		log.debug("ep {} mod {} {}", id(), socket, option);

		assert option.isValidInterestRequest();

		SocketUDT.epollUpdate0(id(), socket.id(), option.code);

	}

	/** report current poll/socket readiness */
	public Opt verify(final SocketUDT socket) throws ExceptionUDT {

		final int code = SocketUDT.epollVerify0(id(), socket.id());

		return Opt.from(code);

	}

}
