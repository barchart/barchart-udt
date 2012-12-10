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

import static com.barchart.udt.SocketUDT.*;
import static java.nio.channels.SelectionKey.*;

import java.io.IOException;
import java.net.InetSocketAddress;
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
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	protected static Selector open(TypeUDT type) throws IOException {
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
			synchronized (publicRegisteredKeySet) {
				synchronized (publicSelectedKeySet) {

					selectedKeySet.clear();

					for (final SelectionKey key : registeredKeySet) {
						try {
							key.channel().close();
						} catch (Throwable e) {
							log.error("unexpected", e);
						}
					}

					registeredKeySet.clear();

				}
			}
		}
	}

	// NOE: register() and select() are blocking each other
	@Override
	protected SelectionKey register(AbstractSelectableChannel channel,
			int interestOps, Object attachment) {

		if (!(channel instanceof ChannelUDT)) {
			// also takes care of null
			log.error("!(channel instanceof ChannelUDT)");
			throw new IllegalSelectorException();
		}

		synchronized (publicRegisteredKeySet) {

			if (publicRegisteredKeySet.size() == maximimSelectorSize) {
				log.error("reached maximimSelectorSize)");
				throw new IllegalSelectorException();
			}

			ChannelUDT channelUDT = (ChannelUDT) channel;

			SocketUDT socketUDT = channelUDT.getSocketUDT();

			SelectionKeyUDT keyUDT = new SelectionKeyUDT(//
					this, channelUDT, attachment, interestOps);

			// XXX the only place with "add/put"
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
		return (Set<SelectionKey>) publicRegisteredKeySet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SelectionKey> selectedKeys() {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		return (Set<SelectionKey>) publicSelectedKeySet;
	}

	@Override
	public int select() throws IOException {
		return select(0);
	}

	@Override
	// per java.nio.Selector contract 0 input means infinite
	public int select(long timeout) throws IOException {
		if (timeout < 0) {
			throw new IllegalArgumentException("Negative timeout");
		}
		long timeoutUDT = (timeout == 0) ? UDT_TIMEOUT_INFINITE : timeout;
		return doSelectInsideLock(timeoutUDT);
	}

	@Override
	public int selectNow() throws IOException {
		return doSelectInsideLock(UDT_TIMEOUT_NONE);
	}

	/**
	 * Wakeup the selector. NOTE: it is recommended to user timed
	 * {@link #select(long)} (say, timeout = 100 milliseconds) to avoid missed
	 * {@link #wakeup()} calls which are rare but possible in current
	 * implementation (accepted in favor of design simplicity / performance)
	 */
	@Override
	public Selector wakeup() {
		// publisher for volatile
		wakeupStepCount++;
		return this;
	}

	private volatile int wakeupStepCount;

	// guarded by doSelectInsideLock
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

	// totally immutable
	private final Set<? extends SelectionKey> publicRegisteredKeySet;
	// partially immutable: removal allowed, but not addition
	private final Set<? extends SelectionKey> publicSelectedKeySet;
	// mutable: requests for cancel
	private final Set<SelectionKey> cancelledKeySet;

	private final Map<Integer, SelectionKeyUDT> registeredKeyMap;

	/**
	 * used by SocketUDT.select(); performance optimization: use final arrays
	 */
	public final int maximimSelectorSize;
	//
	private final int[] readArray;
	private final int[] writeArray;
	private final int[] exceptArray;
	private final int[] sizeArray;
	//
	private final IntBuffer readBuffer;
	private final IntBuffer writeBuffer;
	private final IntBuffer exceptBuffer;
	private final IntBuffer sizeBuffer;

	/**
	 * connector thread pool limit
	 */
	public final int maximumConnectorSize;
	private final ConnectorThreadPoolUDT connectorPool;

	SelectorUDT(SelectorProvider provider, //
			int maximumSelectorSize, int maximumConnectorSize) {

		super(provider);

		registeredKeyMap = new HashMap<Integer, SelectionKeyUDT>();

		registeredKeySet = new HashSet<SelectionKeyUDT>();
		selectedKeySet = new HashSet<SelectionKeyUDT>();
		cancelledKeySet = cancelledKeys();

		publicRegisteredKeySet = Collections.unmodifiableSet(registeredKeySet);
		publicSelectedKeySet = HelperNIOUDT.ungrowableSet(selectedKeySet);

		this.maximimSelectorSize = maximumSelectorSize;
		if (isBufferBased) {
			readBuffer = SocketUDT.newDirectIntBufer(maximumSelectorSize);
			writeBuffer = SocketUDT.newDirectIntBufer(maximumSelectorSize);
			exceptBuffer = SocketUDT.newDirectIntBufer(maximumSelectorSize);
			sizeBuffer = SocketUDT.newDirectIntBufer(UDT_SIZE_COUNT);
			readArray = null;
			writeArray = null;
			exceptArray = null;
			sizeArray = null;
		} else {
			readBuffer = null;
			writeBuffer = null;
			exceptBuffer = null;
			sizeBuffer = null;
			readArray = new int[maximumSelectorSize];
			writeArray = new int[maximumSelectorSize];
			exceptArray = new int[maximumSelectorSize];
			sizeArray = new int[UDT_SIZE_COUNT];
		}

		this.maximumConnectorSize = maximumConnectorSize;
		connectorPool = new ConnectorThreadPoolUDT(maximumConnectorSize);

	}

	public static int UDT_TIMEOUT_INFINITE = -1;

	public static int UDT_TIMEOUT_NONE = 0;

	private final int doSelectInsideLock(long millisTimeout) throws IOException {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		synchronized (this) {
			synchronized (publicRegisteredKeySet) {
				synchronized (publicSelectedKeySet) {
					return doSelectReally(millisTimeout);
				}
			}
		}
	}

	//

	public static final String KEY_IS_ARRAY_BASED = //
	SelectorUDT.class.getName() + ".isArrayBased";

	private static final boolean isBufferBased;

	static {
		String isArrayBased = System.getProperty(KEY_IS_ARRAY_BASED);
		if (isArrayBased == null) {
			isBufferBased = true;
		} else {
			isBufferBased = false;
		}
		log.debug("isBufferBased={}", isBufferBased);
	}

	private final void setReadInterest(int index, int socketID) {
		if (isBufferBased) {
			readBuffer.put(index, socketID);
		} else {
			readArray[index] = socketID;
		}
	}

	private final int getReadInterest(int index) {
		if (isBufferBased) {
			return readBuffer.get(index);
		} else {
			return readArray[index];
		}
	}

	private final void setWriteInterest(int index, int socketID) {
		if (isBufferBased) {
			writeBuffer.put(index, socketID);
		} else {
			writeArray[index] = socketID;
		}
	}

	private final int getWriteInterest(int index) {
		if (isBufferBased) {
			return writeBuffer.get(index);
		} else {
			return writeArray[index];
		}
	}

	private final void setExceptInterest(int index, int socketID) {
		if (isBufferBased) {
			exceptBuffer.put(index, socketID);
		} else {
			exceptArray[index] = socketID;
		}
	}

	private final int getExceptInterest(int index) {
		if (isBufferBased) {
			return exceptBuffer.get(index);
		} else {
			return exceptArray[index];
		}
	}

	private final int doSelectSocketUDT(long timeout) throws ExceptionUDT {
		if (isBufferBased) {
			return SocketUDT.select(readBuffer, writeBuffer, exceptBuffer,
					sizeBuffer, timeout);
		} else {
			return SocketUDT.select(readArray, writeArray, exceptArray,
					sizeArray, timeout);
		}
	}

	private final void setInterestSize(int readSize, int writeSize,
			int exceptSize) {
		if (isBufferBased) {
			sizeBuffer.put(UDT_READ_INDEX, readSize);
			sizeBuffer.put(UDT_WRITE_INDEX, writeSize);
			sizeBuffer.put(UDT_EXCEPT_INDEX, exceptSize);
		} else {
			sizeArray[UDT_READ_INDEX] = readSize;
			sizeArray[UDT_WRITE_INDEX] = writeSize;
			sizeArray[UDT_EXCEPT_INDEX] = exceptSize;
		}
	}

	private final int getReadInterestSize() {
		if (isBufferBased) {
			return sizeBuffer.get(UDT_READ_INDEX);
		} else {
			return sizeArray[UDT_READ_INDEX];
		}
	}

	private final int getWriteInterestSize() {
		if (isBufferBased) {
			return sizeBuffer.get(UDT_WRITE_INDEX);
		} else {
			return sizeArray[UDT_WRITE_INDEX];
		}
	}

	private final int getExceptInterestSize() {
		if (isBufferBased) {
			return sizeBuffer.get(UDT_EXCEPT_INDEX);
		} else {
			return sizeArray[UDT_EXCEPT_INDEX];
		}
	}

	private final void prepareInterest() {

		int readSize = 0;
		int writeSize = 0;
		int exceptSize = 0;

		for (final SelectionKeyUDT keyUDT : registeredKeySet) {

			final ChannelUDT channelUDT = keyUDT.channelUDT;
			keyUDT.readyOps = 0; // publisher for volatile

			if (channelUDT.isOpenSocketUDT()) {

				final int interestOps = keyUDT.interestOps;
				final int socketID = keyUDT.socketID;
				final KindUDT channelType = channelUDT.getChannelKind();

				if ((interestOps & (OP_ACCEPT)) != 0) {
					assert channelType == KindUDT.ACCEPTOR;
					setReadInterest(readSize, socketID);
					readSize++;
				}
				if ((interestOps & (OP_READ)) != 0) {
					assert channelType == KindUDT.CONNECTOR;
					setReadInterest(readSize, socketID);
					readSize++;
				}
				if ((interestOps & (OP_WRITE)) != 0) {
					assert channelType == KindUDT.CONNECTOR;
					setWriteInterest(writeSize, socketID);
					writeSize++;
				}
				if ((interestOps & (OP_CONNECT)) != 0) {
					assert channelType == KindUDT.CONNECTOR;
					assert (interestOps & (OP_READ | OP_WRITE | OP_ACCEPT)) == 0;
					/*
					 * UDT does not support select() for connect() operation as
					 * yet; using thread pool instead
					 */
				}
			} else {
				synchronized (cancelledKeySet) {
					cancelledKeySet.add(keyUDT);
				}
			}
		}

		// set sizes
		setInterestSize(readSize, writeSize, exceptSize);

	}

	private final void processInterest() {

		// add ready to selected
		updateRead();
		updateWrite();
		updateExcept();

	}

	/**
	 * note: millisTimeout input values contract:
	 * 
	 * -1 : infinite
	 * 
	 * =0 : immediate (but really up to 10 ms UDT resolution slice)
	 * 
	 * >0 : finite
	 */
	private final int performSelect(long millisTimeout) throws ExceptionUDT {
		int readyCount = 0;
		saveWakeupBase();
		if (millisTimeout < 0) {
			/* infinite: do select in slices; check for wakeup; */
			do {
				readyCount = doSelectSocketUDT(DEFAULT_MIN_SELECTOR_TIMEOUT);
				if (readyCount > 0 || isWakeupPending()) {
					break;
				}
			} while (true);
		} else if (millisTimeout > 0) {
			/* finite: do select in slices; check for wakeup; count down */
			do {
				readyCount = doSelectSocketUDT(DEFAULT_MIN_SELECTOR_TIMEOUT);
				if (readyCount > 0 || isWakeupPending()) {
					break;
				}
				millisTimeout -= DEFAULT_MIN_SELECTOR_TIMEOUT;
			} while (millisTimeout > 0);
		} else { // millisTimeout == 0
			/* immediate: one shot select */
			readyCount = doSelectSocketUDT(0);
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
	private final int doSelectReally(long millisTimeout) throws IOException {

		selectedKeySet.clear();

		processCancelled();

		int readyCount = 0;

		trySelect: try {
			// java.nio.Selector contract for wakeup()
			begin();
			// pre select
			prepareInterest();
			// into select
			readyCount = performSelect(millisTimeout);
			if (readyCount == 0) {
				// timeout, nothing is ready; no need to process post select
				break trySelect;
			}
			// post select
			processInterest();
		} finally {
			// java.nio.Selector contract for wakeup()
			end();
		}

		// using thread pool based connect() processor
		readyCount += updateConnect();

		assert readyCount >= 0;

		return readyCount;

	}

	private final int updateConnect() {
		final Queue<SelectionKeyUDT> readyQueue = connectorPool.readyQueue;
		if (readyQueue.isEmpty()) {
			return 0;
		}
		int updateCount = 0;
		SelectionKeyUDT keyUDT;
		while ((keyUDT = readyQueue.poll()) != null) {
			// contract:
			assert keyUDT.channelUDT.getChannelKind() == KindUDT.CONNECTOR;
			assert registeredKeySet.contains(keyUDT);
			assert (keyUDT.interestOps & OP_CONNECT) != 0;
			assert (keyUDT.interestOps & (OP_READ | OP_WRITE | OP_ACCEPT)) == 0;
			//
			keyUDT.readyOps |= OP_CONNECT;
			selectedKeySet.add(keyUDT);
			updateCount++;
		}
		return updateCount;
	}

	private final void updateRead() {
		final int readSize = getReadInterestSize();
		for (int index = 0; index < readSize; index++) {
			final int socketId = getReadInterest(index);
			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);
			assert keyUDT != null;
			switch (keyUDT.channelUDT.getChannelKind()) {
			case ACCEPTOR:
				keyUDT.readyOps |= OP_ACCEPT;
				break;
			case CONNECTOR:
				keyUDT.readyOps |= OP_READ;
				break;
			default:
				assert false : "unexpected default";
				continue;
			}
			selectedKeySet.add(keyUDT);
		}
	}

	private final void updateWrite() {
		final int writeSize = getWriteInterestSize();
		for (int index = 0; index < writeSize; index++) {
			final int socketId = getWriteInterest(index);
			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);
			assert keyUDT != null;
			switch (keyUDT.channelUDT.getChannelKind()) {
			case ACCEPTOR:
				assert false : "unexpected ACCEPTOR";
				continue;
			case CONNECTOR:
				keyUDT.readyOps |= OP_WRITE;
				break;
			default:
				assert false : "unexpected default";
				continue;
			}
			selectedKeySet.add(keyUDT);
		}
	}

	private final void updateExcept() {
		final int exceptSize = getExceptInterestSize();
		for (int k = 0; k < exceptSize; k++) {
			final int socketId = getExceptInterest(k);
			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);
			assert keyUDT != null;
			switch (keyUDT.channelUDT.getChannelKind()) {
			case ACCEPTOR:
			case CONNECTOR:
				// set all ready OPS to throw on any operation
				keyUDT.readyOps |= keyUDT.channel().validOps();
				break;
			default:
				assert false : "unexpected default";
				continue;
			}
			selectedKeySet.add(keyUDT);
		}
	}

	private final void processCancelled() throws IOException {
		/*
		 * Precondition: Synchronized on this, publicRegisteredKeySet,
		 * publicSelectedKeySet
		 */
		synchronized (cancelledKeySet) {
			if (!cancelledKeySet.isEmpty()) {
				for (final SelectionKey key : cancelledKeySet) {

					final SelectionKeyUDT keyUDT = (SelectionKeyUDT) key;

					// XXX the only place with "remove"

					final SelectionKeyUDT removed = //
					registeredKeyMap.remove(keyUDT.socketID);
					assert removed != null;

					final boolean isRemoved = //
					registeredKeySet.remove(keyUDT);
					assert isRemoved;

				}
				cancelledKeySet.clear();
			}
		}
	}

	final void submitConnectRequest(SelectionKeyUDT keyUDT,
			InetSocketAddress remote) throws IOException {

		// TODO think again if lack of sync is OK?

		if (!registeredKeySet.contains(keyUDT)) {
			throw new IOException(//
					"connect request while not registered");
		}

		if ((keyUDT.interestOps & OP_CONNECT) == 0) {
			throw new IOException("connect request while not interested;"
					+ " key=" + keyUDT);
		}

		if ((keyUDT.interestOps & (OP_ACCEPT | OP_READ | OP_WRITE)) != 0) {
			throw new IOException("connect request while is not sole interest;"
					+ " key=" + keyUDT);
		}

		connectorPool.submitRequest(keyUDT, remote);

	}

}
