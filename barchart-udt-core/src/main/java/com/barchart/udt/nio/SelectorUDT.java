/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import static com.barchart.udt.SocketUDT.*;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.EpollUDT;
import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelpUDT;

/**
 * selector
 * <p>
 * design guidelines:
 * <p>
 * 1) follow general contracts of jdk 6 nio; see <a href=
 * "https://github.com/barchart/barchart-udt/tree/master/barchart-udt-reference-jdk6"
 * >barchart-udt-reference-jdk6</a>
 * <p>
 * 2) adapt to how netty is doing select; see <a href=
 * "https://github.com/netty/netty/blob/master/transport/src/main/java/io/netty/channel/socket/nio/NioEventLoop.java"
 * >NioEventLoop</a>
 * <p>
 * note: you must use {@link SelectorProviderUDT#openSelector()} to obtain
 * instance of this class; do not use JDK
 * {@link java.nio.channels.Selector#open()}
 */
public class SelectorUDT extends AbstractSelector {

	protected static final Logger log = LoggerFactory
			.getLogger(SelectorUDT.class);

	/**
	 * use this call to instantiate a selector for UDT
	 */
	protected static Selector open(final TypeUDT type) throws IOException {
		final SelectorProviderUDT provider;
		switch (type) {
		case DATAGRAM:
			provider = SelectorProviderUDT.DATAGRAM;
			break;
		case STREAM:
			provider = SelectorProviderUDT.STREAM;
			break;
		default:
			log.error("unsupported type={}", type);
			throw new IOException("unsupported type");
		}
		return provider.openSelector();
	}

	private final EpollUDT epollUDT = new EpollUDT();

	/**
	 */
	public final int maximimSelectorSize;

	/**
	 * list of epoll sockets with read interest
	 */
	private final IntBuffer readBuffer;

	/**
	 * [ socket-id : selection-key ]
	 */
	private final ConcurrentMap<Integer, SelectionKeyUDT> //
	registeredKeyMap = new ConcurrentHashMap<Integer, SelectionKeyUDT>();

	/**
	 * public view : immutable
	 */
	private final Set<? extends SelectionKey> //
	registeredKeySet = HelpUDT.unmodifiableSet(registeredKeyMap.values());

	/**
	 * tracks correlation read with write for the same key
	 */
	private volatile int resultIndex;

	/**
	 * set of keys with data ready for an operation
	 */
	private final ConcurrentMap<SelectionKeyUDT, SelectionKeyUDT> //
	selectedKeyMap = new ConcurrentHashMap<SelectionKeyUDT, SelectionKeyUDT>();

	/**
	 * public view : removal allowed, but not addition
	 */
	private final Set<? extends SelectionKey> //
	selectedKeySet = HelpUDT.ungrowableSet(selectedKeyMap.keySet());

	/** select is exclusive */
	private final Lock selectLock = new ReentrantLock();

	/** reported epoll socket list sizes */
	private final IntBuffer sizeBuffer;

	/**
	 * Canceled keys.
	 */
	private final ConcurrentMap<SelectionKeyUDT, SelectionKeyUDT> //
	terminatedKeyMap = new ConcurrentHashMap<SelectionKeyUDT, SelectionKeyUDT>();

	/** guarded by {@link #doSelectLocked} */
	private volatile int wakeupBaseCount;

	private volatile int wakeupStepCount;

	/** list of epoll sockets with write interest */
	private final IntBuffer writeBuffer;

	protected SelectorUDT( //
			final SelectorProvider provider, //
			final int maximumSelectorSize //
	) throws ExceptionUDT {

		super(provider);

		this.maximimSelectorSize = maximumSelectorSize;

		readBuffer = HelpUDT.newDirectIntBufer(maximumSelectorSize);
		writeBuffer = HelpUDT.newDirectIntBufer(maximumSelectorSize);
		sizeBuffer = HelpUDT.newDirectIntBufer(UDT_SIZE_COUNT);

	}

	/**
	 * Enqueue cancel request.
	 */
	protected void cancel(final SelectionKeyUDT keyUDT) {
		terminatedKeyMap.putIfAbsent(keyUDT, keyUDT);
	}

	/**
	 * Process pending cancel requests.
	 */
	protected void doCancel() {

		if (terminatedKeyMap.isEmpty()) {
			return;
		}

		final Iterator<SelectionKeyUDT> iterator = terminatedKeyMap.values()
				.iterator();

		while (iterator.hasNext()) {
			final SelectionKeyUDT keyUDT = iterator.next();
			iterator.remove();
			if (keyUDT.isValid()) {
				keyUDT.makeValid(false);
				registeredKeyMap.remove(keyUDT.socketId());
			}
		}

	}

	/**
	 * @param millisTimeout
	 *            <0 : invinite; =0 : immediate; >0 : finite;
	 */
	protected int doEpollEnter(final long millisTimeout) throws IOException {

		if (!isOpen()) {
			log.error("slector is closed");
			throw new ClosedSelectorException();
		}

		try {
			selectLock.lock();
			return doEpollExclusive(millisTimeout);
		} finally {
			selectLock.unlock();
		}

	}

	/**
	 * @param millisTimeout
	 * 
	 *            <0 : invinite;
	 * 
	 *            =0 : immediate;
	 * 
	 *            >0 : finite;
	 * @return
	 * 
	 *         <0 : should not happen
	 * 
	 *         =0 : means nothing was selected/timeout
	 * 
	 *         >0 : number of selected keys
	 */

	protected int doEpollExclusive(final long millisTimeout) throws IOException {

		try {

			/** java.nio.Selector contract for wakeup() */
			// begin();

			/** pre select */
			doCancel();

			/** select proper */
			doEpollSelect(millisTimeout);

			/** post select */
			doResults();

		} finally {
			/** java.nio.Selector contract for wakeup() */
			// end();
		}

		return selectedKeyMap.size();

	}

	/**
	 * @param millisTimeout
	 * 
	 *            <0 : infinite
	 * 
	 *            =0 : immediate
	 * 
	 *            >0 : finite
	 */
	protected int doEpollSelect(long millisTimeout) throws ExceptionUDT {

		wakeupMarkBase();

		int readyCount = 0;

		if (millisTimeout < 0) {

			/** infinite: do select in slices; check for wakeup; */

			do {
				readyCount = doEpollSelectUDT(DEFAULT_MIN_SELECTOR_TIMEOUT);
				if (readyCount > 0 || wakeupIsPending()) {
					break;
				}
			} while (true);

		} else if (millisTimeout > 0) {

			/** finite: do select in slices; check for wakeup; count down */

			do {
				readyCount = doEpollSelectUDT(DEFAULT_MIN_SELECTOR_TIMEOUT);
				if (readyCount > 0 || wakeupIsPending()) {
					break;
				}
				millisTimeout -= DEFAULT_MIN_SELECTOR_TIMEOUT;
			} while (millisTimeout > 0);

		} else {

			/** immediate */

			readyCount = doEpollSelectUDT(0);

		}

		return readyCount;

	}

	protected int doEpollSelectUDT(final long timeout) throws ExceptionUDT {
		return SocketUDT.selectEpoll(//
				epollUDT.id(), //
				readBuffer, //
				writeBuffer, //
				sizeBuffer, //
				timeout //
				);
	}

	protected void doResults() {

		final int resultIndex = this.resultIndex++;

		doResultsRead(resultIndex);

		doResultsWrite(resultIndex);

	}

	protected void doResultsRead(final int resultIndex) {

		final int readSize = sizeBuffer.get(UDT_READ_INDEX);

		for (int index = 0; index < readSize; index++) {

			final int socketId = readBuffer.get(index);

			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);

			/**
			 * Epoll will report closed socket once in both read and write sets.
			 * But selector consumer may cancel the key before close.
			 */
			if (keyUDT == null) {
				logSocketId("missing from read ", socketId);
				continue;
			}

			if (keyUDT.doRead(resultIndex)) {
				selectedKeyMap.putIfAbsent(keyUDT, keyUDT);
			}

		}

	}

	protected void doResultsWrite(final int resultIndex) {

		final int writeSize = sizeBuffer.get(UDT_WRITE_INDEX);

		for (int index = 0; index < writeSize; index++) {

			final int socketId = writeBuffer.get(index);

			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);

			/**
			 * Epoll will report closed socket once in both read and write sets.
			 * But selector consumer may cancel the key before close.
			 */
			if (keyUDT == null) {
				logSocketId("missing from write", socketId);
				continue;
			}

			if (keyUDT.doWrite(resultIndex)) {
				selectedKeyMap.putIfAbsent(keyUDT, keyUDT);
			}

		}

	}

	protected EpollUDT epollUDT() {
		return epollUDT;
	}

	@Override
	protected void implCloseSelector() throws IOException {

		wakeup();

		try {
			selectLock.lock();

			for (final SelectionKeyUDT keyUDT : registeredKeyMap.values()) {
				cancel(keyUDT);
			}

		} finally {
			selectLock.unlock();
		}

		doCancel();

	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SelectionKey> keys() {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		return (Set<SelectionKey>) registeredKeySet;
	}

	protected void logSocketId(final String title, final int socketId) {
		if (log.isDebugEnabled()) {
			log.debug("{} {}", title, String.format("[id: 0x%08x]", socketId));
		}
	}

	/** 
	 */
	@Override
	protected SelectionKey register( //
			final AbstractSelectableChannel channel, //
			final int interestOps, //
			final Object attachment //
	) {

		if (registeredKeyMap.size() >= maximimSelectorSize) {
			log.error("reached maximimSelectorSize");
			throw new IllegalSelectorException();
		}

		if (!(channel instanceof ChannelUDT)) {
			log.error("!(channel instanceof ChannelUDT)");
			throw new IllegalSelectorException();
		}

		final ChannelUDT channelUDT = (ChannelUDT) channel;

		final Integer socketId = channelUDT.socketUDT().id();

		SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);

		if (keyUDT == null) {
			keyUDT = new SelectionKeyUDT(this, channelUDT, attachment);
			registeredKeyMap.putIfAbsent(socketId, keyUDT);
			keyUDT = registeredKeyMap.get(socketId);
		}

		keyUDT.interestOps(interestOps);

		return keyUDT;

	}

	@Override
	public int select() throws IOException {
		return select(0);
	}

	@Override
	public int select(final long timeout) throws IOException {
		if (timeout < 0) {
			throw new IllegalArgumentException("negative timeout");
		} else if (timeout > 0) {
			return doEpollEnter(timeout);
		} else {
			return doEpollEnter(SocketUDT.TIMEOUT_INFINITE);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SelectionKey> selectedKeys() {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		return (Set<SelectionKey>) selectedKeySet;
	}

	@Override
	public int selectNow() throws IOException {
		return doEpollEnter(SocketUDT.TIMEOUT_NONE);
	}

	@Override
	public Selector wakeup() {
		wakeupStepCount++;
		return this;
	}

	protected boolean wakeupIsPending() {
		return wakeupBaseCount != wakeupStepCount;
	}

	protected void wakeupMarkBase() {
		wakeupBaseCount = wakeupStepCount;
	}

}
