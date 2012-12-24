/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDT Epoll resource descriptor
 * 
 * @see <a href"http://en.wikipedia.org/wiki/Epoll">Epoll</a>
 */
public class EpollUDT {

	/**
	 * epoll interest option mask
	 * <p>
	 * see udt.h enum - EPOLLOpt
	 * 
	 * <pre>
	 *    UDT_EPOLL_IN = 0x1,
	 *    UDT_EPOLL_OUT = 0x4,
	 *    UDT_EPOLL_ERR = 0x8
	 * </pre>
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

		BOTH(READ.code | WRITE.code), //

		ALL(READ.code | WRITE.code | ERROR.code), //

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
		 * epoll event mask;
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

	}

	protected static final Logger log = LoggerFactory.getLogger(EpollUDT.class);

	protected final int id;

	protected volatile boolean isActive;

	/**
	 * allocate Epoll
	 */
	public EpollUDT() throws ExceptionUDT {

		id = SocketUDT.epollCreate0();
		isActive = true;

		log.debug("ep create {}", id());

	}

	/**
	 * deallocate Epoll; idempotent
	 */
	public void destroy() throws ExceptionUDT {

		SocketUDT.epollRelease0(id);

		isActive = false;

		log.debug("ep delete {}", id());

	}

	/** descriptor id */
	public int id() {
		return id;
	}

	public boolean isActive() {
		return isActive;
	}

	/**
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

	/** register socket into event processing Epoll */
	public void add(final SocketUDT socket, final Opt option)
			throws ExceptionUDT {

		log.debug("ep add {} {}", socket, option);

		SocketUDT.epollAdd0(id, socket.getSocketId(), option.code);

	}

	/** unregister socket from event processing Epoll */
	public void remove(final SocketUDT socket) throws ExceptionUDT {

		log.debug("ep rem {}", socket);

		SocketUDT.epollRemove0(id, socket.getSocketId());

	}

	/** re-register socket with a new option */
	public void update(final SocketUDT socket, final Opt option)
			throws ExceptionUDT {

		log.debug("ep mod {} {}", socket, option);

		// SocketUDT.epollUpdate(this, socket, option);
		SocketUDT.epollRemove0(id, socket.getSocketId());
		SocketUDT.epollAdd0(id, socket.getSocketId(), option.code);

	}

}
