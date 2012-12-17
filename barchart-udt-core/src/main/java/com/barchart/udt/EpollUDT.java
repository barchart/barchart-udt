package com.barchart.udt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Epoll resource descriptor
 * <p>
 * <a href"http://en.wikipedia.org/wiki/Epoll">Epoll</a>
 */
public class EpollUDT {

	private static final Logger log = LoggerFactory.getLogger(EpollUDT.class);

	private final int id;

	private volatile boolean isActive;

	/**
	 * allocate Epoll
	 */
	public EpollUDT() throws ExceptionUDT {

		id = SocketUDT.epollCreate0();
		isActive = true;

		log.debug("created id={}", id());

	}

	/**
	 * deallocate Epoll; idempotent
	 */
	public void destroy() throws ExceptionUDT {

		SocketUDT.epollRelease0(id);
		isActive = false;

		log.debug("destroy id={}", id());

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
	public void add(final SocketUDT socket) throws ExceptionUDT {
		SocketUDT.epollAdd0(id, socket.getSocketId());
	}

	/** unregister socket from event processing Epoll */
	public void remove(final SocketUDT socket) throws ExceptionUDT {
		SocketUDT.epollRemove0(id, socket.getSocketId());
	}

}
