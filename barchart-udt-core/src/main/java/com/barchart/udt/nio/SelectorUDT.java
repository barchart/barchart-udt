/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import static com.barchart.udt.SocketUDT.*;
import static java.nio.channels.SelectionKey.*;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.EpollUDT;
import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

/**
 * you must use {@link SelectorProviderUDT#openSelector()} to obtain instance of
 * this class; do not use JDK {@link java.nio.channels.Selector#open()}
 */
// NOTE: 'final' is used with hopes to force JVM function inlining
public class SelectorUDT extends AbstractSelector {

	private static final Logger log = LoggerFactory
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

	//

	@Override
	protected void implCloseSelector() throws IOException {
		wakeup();
		synchronized (this) {
			synchronized (registeredKeySetPublic) {
				synchronized (selectedKeySetPublic) {

					selectedKeySet.clear();
					cancelledKeySet.clear();
					registeredKeyMap.clear();

					for (final SelectionKey key : registeredKeySet) {
						try {
							key.channel().close();
						} catch (final Throwable e) {
							log.error("unexpected", e);
						}
					}

					registeredKeySet.clear();

				}
			}
		}
	}

	/** NOTE: register() and select() are blocking each other */
	@Override
	protected SelectionKey register( //
			final AbstractSelectableChannel channel, //
			final int interestOps, //
			final Object attachment //
	) {

		if (!(channel instanceof ChannelUDT)) {
			log.error("!(channel instanceof ChannelUDT)");
			throw new IllegalSelectorException();
		}

		synchronized (registeredKeySetPublic) {

			if (registeredKeySetPublic.size() == maximimSelectorSize) {
				log.error("reached maximimSelectorSize");
				throw new IllegalSelectorException();
			}

			final ChannelUDT channelUDT = (ChannelUDT) channel;

			final SocketUDT socketUDT = channelUDT.socketUDT();

			final SelectionKeyUDT keyUDT = new SelectionKeyUDT(//
					this, channelUDT, attachment, interestOps);

			/** XXX the only place with "add" */
			try {
				epoll.add(keyUDT.socketUDT());
			} catch (final ExceptionUDT e) {
				log.error("epoll add failure", e);
				throw new IllegalSelectorException();
			}

			registeredKeyMap.put(socketUDT.getSocketId(), keyUDT);
			registeredKeySet.add(keyUDT);

			return keyUDT;

		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SelectionKey> keys() {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		return (Set<SelectionKey>) registeredKeySetPublic;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SelectionKey> selectedKeys() {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		return (Set<SelectionKey>) selectedKeySetPublic;
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
			return stageEnter(timeout);
		} else {
			return stageEnter(UDT_TIMEOUT_INFINITE);
		}
	}

	@Override
	public int selectNow() throws IOException {
		return stageEnter(UDT_TIMEOUT_NONE);
	}

	/**
	 * Wakeup the selector. NOTE: it is recommended to user timed
	 * {@link #select(long)} (say, timeout = 100 milliseconds) to avoid missed
	 * {@link #wakeup()} calls which are rare but possible in current
	 * implementation (accepted in favor of design simplicity / performance)
	 */
	@Override
	public Selector wakeup() {
		/** publisher for volatile */
		wakeupStepCount++;
		return this;
	}

	private volatile int wakeupStepCount;

	/** guarded by {@link #doSelectLocked} */
	private int wakeupBaseCount;

	private final void saveWakeupBase() {
		wakeupBaseCount = wakeupStepCount;
	}

	private final boolean isWakeupPending() {
		return wakeupBaseCount != wakeupStepCount;
	}

	// #######################################################################

	/**
	 * Private views of the key sets
	 */

	/**
	 * The set of keys with data ready for an operation
	 */
	private final Set<SelectionKeyUDT> selectedKeySet;
	/**
	 * The set of keys registered with this Selector
	 */
	private final Set<SelectionKeyUDT> registeredKeySet;

	// Public views of the key sets

	/** totally immutable */
	private final Set<? extends SelectionKey> registeredKeySetPublic;

	/** partially immutable: removal allowed, but not addition */
	private final Set<? extends SelectionKey> selectedKeySetPublic;

	/** mutable: requests for cancel */
	private final Set<SelectionKey> cancelledKeySet;

	private final Map<Integer, SelectionKeyUDT> registeredKeyMap;

	/**
	 * used by SocketUDT.select(); performance optimization: use final arrays
	 */
	public final int maximimSelectorSize;
	//
	private final IntBuffer readBuffer;
	private final IntBuffer writeBuffer;
	private final IntBuffer sizeBuffer;

	private final EpollUDT epoll;

	protected SelectorUDT( //
			final SelectorProvider provider, //
			final int maximumSelectorSize //
	) throws ExceptionUDT {

		super(provider);

		registeredKeyMap = new HashMap<Integer, SelectionKeyUDT>();

		cancelledKeySet = cancelledKeys();

		registeredKeySet = new HashSet<SelectionKeyUDT>();
		selectedKeySet = new HashSet<SelectionKeyUDT>();

		registeredKeySetPublic = Collections.unmodifiableSet(registeredKeySet);
		selectedKeySetPublic = HelpUDT.ungrowableSet(selectedKeySet);

		this.maximimSelectorSize = maximumSelectorSize;

		readBuffer = SocketUDT.newDirectIntBufer(maximumSelectorSize);
		writeBuffer = SocketUDT.newDirectIntBufer(maximumSelectorSize);
		sizeBuffer = SocketUDT.newDirectIntBufer(UDT_SIZE_COUNT);

		epoll = new EpollUDT();

	}

	/** return immediately from epoll */
	public static long UDT_TIMEOUT_NONE = 0;

	/** block epoll till event arrives */
	public static long UDT_TIMEOUT_INFINITE = -1;

	private final int stageEnter(final long millisTimeout) throws IOException {

		if (!isOpen()) {
			throw new ClosedSelectorException();
		}

		synchronized (this) {
			synchronized (registeredKeySetPublic) {
				synchronized (selectedKeySetPublic) {
					return stageLocked(millisTimeout);
				}
			}
		}

	}

	//

	private final int stageSelectUDT(final long timeout) throws ExceptionUDT {
		return SocketUDT.selectEpoll(//
				epoll.id(), //
				readBuffer, //
				writeBuffer, //
				sizeBuffer, //
				timeout //
				);
	}

	/**
	 * note: millisTimeout contract:
	 * 
	 * -1 : infinite
	 * 
	 * =0 : immediate
	 * 
	 * >0 : finite
	 */
	private final int stageSelect(long millisTimeout) throws ExceptionUDT {

		int readyCount = 0;

		saveWakeupBase();

		if (millisTimeout < 0) {

			/** infinite: do select in slices; check for wakeup; */

			do {
				readyCount = stageSelectUDT(DEFAULT_MIN_SELECTOR_TIMEOUT);
				if (readyCount > 0 || isWakeupPending()) {
					break;
				}
			} while (true);

		} else if (millisTimeout > 0) {

			/** finite: do select in slices; check for wakeup; count down */

			do {
				readyCount = stageSelectUDT(DEFAULT_MIN_SELECTOR_TIMEOUT);
				if (readyCount > 0 || isWakeupPending()) {
					break;
				}
				millisTimeout -= DEFAULT_MIN_SELECTOR_TIMEOUT;
			} while (millisTimeout > 0);

		} else {

			/** immediate */

			readyCount = stageSelectUDT(0);

		}

		return readyCount;

	}

	/**
	 * note: return value contract:
	 * 
	 * <0 : should not happen
	 * 
	 * =0 : means nothing was selected/timeout
	 * 
	 * >0 : number of pending r/w ops, NOT number of selected keys
	 */
	private final int stageLocked(final long millisTimeout) throws IOException {

		try {

			/** java.nio.Selector contract for wakeup() */
			begin();

			/** pre select */
			stagePrepare();

			/** select proper */
			stageSelect(millisTimeout);

			/** post select */
			stageProcess();

		} finally {
			/** java.nio.Selector contract for wakeup() */
			end();
		}

		return selectedKeySet.size();

	}

	private final void stageProcess() {

		stageProcessRead();

		stageProcessWrite();

	}

	private final void stageProcessRead() {

		final int readSize = sizeBuffer.get(UDT_READ_INDEX);

		for (int index = 0; index < readSize; index++) {

			final int socketId = readBuffer.get(index);

			log.debug("read socketId={}", socketId);

			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);

			switch (keyUDT.kindUDT()) {

			case ACCEPTOR:
				keyUDT.readyOps |= OP_ACCEPT;
				break;

			case CONNECTOR:
				if (keyUDT.channelUDT().isConnectionPending()) {
					if (keyUDT.socketUDT().isConnected()) {
						keyUDT.readyOps |= OP_CONNECT;
					}
				} else {
					keyUDT.readyOps |= OP_READ;
				}
				break;

			default:
				assert false : "wrong kind=" + keyUDT.kindUDT();
				continue;

			}

			selectedKeySet.add(keyUDT);

		}

	}

	private final void stageProcessWrite() {

		final int writeSize = sizeBuffer.get(UDT_WRITE_INDEX);

		for (int index = 0; index < writeSize; index++) {

			final int socketId = writeBuffer.get(index);

			log.debug("write socketId={}", socketId);

			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);

			switch (keyUDT.kindUDT()) {

			case ACCEPTOR:
				keyUDT.readyOps |= OP_ACCEPT;
				continue;

			case CONNECTOR:
				if (keyUDT.channelUDT().isConnectionPending()) {
					if (keyUDT.socketUDT().isConnected()) {
						keyUDT.readyOps |= OP_CONNECT;
					}
				} else {
					keyUDT.readyOps |= OP_WRITE;
				}
				break;

			default:
				assert false : "wrong kind=" + keyUDT.kindUDT();
				continue;
			}

			selectedKeySet.add(keyUDT);

		}

	}

	private final void stagePrepare() throws IOException {

		selectedKeySet.clear();

		synchronized (cancelledKeySet) {

			if (cancelledKeySet.isEmpty()) {
				return;
			}

			for (final SelectionKey key : cancelledKeySet) {

				final SelectionKeyUDT keyUDT = (SelectionKeyUDT) key;

				/** XXX the only place with "remove" */
				epoll.remove(keyUDT.socketUDT());

				final SelectionKeyUDT removed = //
				registeredKeyMap.remove(keyUDT.socketId());
				assert removed != null;

				final boolean isRemoved = //
				registeredKeySet.remove(keyUDT);
				assert isRemoved;

			}

			cancelledKeySet.clear();

		}

	}

}
