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
package com.barchart.udt;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.lib.LibraryLoaderUDT;

/* note: do not change field names; used by JNI */
/* note: must synchronize create/destroy - workaround for bug in UDT */
// TODO centralize http references
/**
 * current implementation supports IPv4 only (no IPv6)
 */
public class SocketUDT {

	private static final Logger log = LoggerFactory.getLogger(SocketUDT.class);

	//

	/**
	 * JNI Signature that must match between java code and c++ code on all
	 * platforms; failure to match will abort native library load
	 */
	/*
	 * do not use automatic signature based on time stamp until all platforms
	 * are built at once by hudson
	 */
	public static final int SIGNATURE_JNI = 20110501; // VersionUDT.BUILDTIME;

	/**
	 * infinite message time to live;
	 */
	public static final int INFINITE_TTL = -1;

	/**
	 * blocking send/receive infinite call timeout;
	 */
	public static final int INFINITE_TIMEOUT = -1;

	/**
	 * unlimited bandwidth option value;
	 */
	public static final long UNLIMITED_BW = -1L;

	/**
	 * Maximum number of connections queued in listening mode by
	 * {@link #accept()}
	 */
	public static final int DEFAULT_ACCEPT_QUEUE_SIZE = 256;

	/**
	 * Maximum number sockets that can participate in a
	 * {@link com.barchart.udt.nio.SelectorUDT#select()} operation
	 */
	public static final int DEFAULT_MAX_SELECTOR_SIZE = 1024;

	/**
	 * Maximum number of threads. That can be doing
	 * {@link com.barchart.udt.nio.ChannelSocketUDT#connect(java.net.SocketAddress)}
	 * operation in non-blocking mode
	 */
	public static final int DEFAULT_CONNECTOR_POOL_SIZE = 16;

	/**
	 * Minimum timeout of a {@link com.barchart.udt.nio.SelectorUDT#select()}
	 * operation. Since UDT :: common.cpp :: void CTimer::waitForEvent() :: is
	 * using 10 milliseconds resolution; (milliseconds);
	 */
	public static final int DEFAULT_MIN_SELECTOR_TIMEOUT = 10;

	// native library extractor and loader

	/** native loader */
	static {

		try {

			final String location = ResourceUDT.getLibraryExtractLocation();

			final String loaderName = ResourceUDT.getLibraryLoaderClassName();

			@SuppressWarnings("unchecked")
			final Class<LibraryLoaderUDT> loaderClass = (Class<LibraryLoaderUDT>) Class
					.forName(loaderName);

			final LibraryLoaderUDT loaderInstance = loaderClass.newInstance();

			loaderInstance.load(location);

		} catch (Throwable e) {
			log.error("failed to LOAD native library; terminating", e);
			System.exit(1);
		}

		try {
			initClass0();
		} catch (Throwable e) {
			log.error("failed to INIT native library; terminating", e);
			System.exit(2);
		}

		if (SIGNATURE_JNI != getSignatureJNI0()) {
			log.error("java/native SIGNATURE inconsistent; terminating");
			System.exit(3);
		}

		log.debug("native library load & init OK");

	}

	// ###################################################

	/**
	 * native descriptor; read by JNI; see udt.h "typedef int UDTSOCKET;"
	 */
	protected final int socketID;

	public int getSocketId() {
		return socketID;
	}

	/**
	 * native socket type; SOCK_DGRAM / SOCK_STREAM
	 */
	protected final int socketType;

	/**
	 * native address family; read by JNI
	 */
	// TODO add support for AF_INET6
	protected final int socketAddressFamily;

	/**
	 * message/stream socket type; read by JNI
	 */
	protected final TypeUDT type;

	public TypeUDT getType() {
		return type;
	}

	/**
	 * performance monitor; updated by {@link #updateMonitor(boolean)} in JNI
	 * 
	 * @see #updateMonitor(boolean)
	 */
	protected final MonitorUDT monitor;

	public MonitorUDT getMonitor() {
		return monitor;
	}

	/**
	 * message send mode parameters; used by JNI on each message send
	 */
	protected volatile int messageTimeTolive;
	protected volatile boolean messageIsOrdered;

	/**
	 * local end point; loaded by JNI by {@link #hasLoadedLocalSocketAddress()}
	 */
	protected volatile InetSocketAddress localSocketAddress;

	/**
	 * remote end point; loaded by JNI by
	 * {@link #hasLoadedRemoteSocketAddress()}
	 */
	protected volatile InetSocketAddress remoteSocketAddress;

	/**
	 * UDT::select() sizeArray/sizeBuffer index offset for READ interest
	 */
	public static final int UDT_READ_INDEX = 0;
	/**
	 * UDT::select() sizeArray/sizeBuffer index offset for WRITE interest
	 */
	public static final int UDT_WRITE_INDEX = 1;
	/**
	 * UDT::select() sizeArray/sizeBuffer index offset for EXCEPTION report
	 */
	public static final int UDT_EXCEPT_INDEX = 2;
	/**
	 * UDT::select() sizeArray/sizeBuffer size count or number of arrays/buffers
	 */
	public static final int UDT_SIZE_COUNT = 3;

	/**
	 * UDT::selectEx() result status
	 */
	protected boolean isSelectedRead;
	protected boolean isSelectedWrite;
	protected boolean isSelectedException;

	// ###################################################
	// ### UDT API
	// ###

	/**
	 * Verify that java code and c++ code builds are consistent.
	 * 
	 * @see #SIGNATURE_JNI
	 */
	protected static native int getSignatureJNI0();

	/**
	 * Call this after loading native library.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/startup.htm">UDT::startup()</a>
	 */
	protected static native void initClass0() throws ExceptionUDT;

	/**
	 * Call this before unloading native library.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/cleanup.htm.htm">UDT::cleanup()</a>
	 */
	protected static native void stopClass0() throws ExceptionUDT;

	/**
	 * used by default constructor
	 */
	protected native int initInstance0(int typeCode) throws ExceptionUDT;

	/**
	 * used by accept() internally
	 */
	protected native int initInstance1(int socketUDT) throws ExceptionUDT;

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/accept.htm">UDT::accept()</a>
	 */
	protected native SocketUDT accept0() throws ExceptionUDT;

	/**
	 * @return null : no incoming connections (non-blocking mode only)<br>
	 *         non null : newly accepted SocketUDT (both blocking and
	 *         non-blocking)<br>
	 */
	public SocketUDT accept() throws ExceptionUDT {
		return accept0();
	}

	protected void checkSocketAddress(final InetSocketAddress socketAddress) {
		if (socketAddress == null) {
			throw new IllegalArgumentException("socketAddress can't be null");
		}
		if (socketAddress.isUnresolved()) {
			// can not use; internal InetAddress field is null
			throw new IllegalArgumentException("socketAddress is unresolved : "
					+ socketAddress + " : check your DNS settings");
		}
	}

	/**
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/bind.htm">UDT::bind()</a>
	 */
	protected native void bind0(final InetSocketAddress localSocketAddress)
			throws ExceptionUDT;

	public void bind(final InetSocketAddress localSocketAddress) //
			throws ExceptionUDT, IllegalArgumentException {
		checkSocketAddress(localSocketAddress);
		bind0(localSocketAddress);
	}

	/**
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/close.htm">UDT::close()</a>
	 */
	protected native void close0() throws ExceptionUDT;

	/**
	 * @see #close0()
	 */
	public void close() throws ExceptionUDT {
		synchronized (SocketUDT.class) {
			if (isOpen()) {
				close0();
				log.debug("closed socketID={}", socketID);
			}
		}
	}

	// NOTE: catch all exceptions; else prevents GC
	// NOTE: do not leak "this" references; else prevents GC
	@Override
	protected void finalize() {
		try {
			close();
			super.finalize();
		} catch (Throwable e) {
			log.error("failed to close", e);
		}
	}

	/**
	 * @see <a http://www.cs.uic.edu/~ygu1/doc/connect.htm">UDT::connect()</a>
	 */
	protected native void connect0(final InetSocketAddress remoteSocketAddress)
			throws ExceptionUDT;

	/**
	 * Note: this is always a blocking call.
	 * 
	 * @see #connect0(InetSocketAddress)
	 */
	public void connect(final InetSocketAddress remoteSocketAddress) //
			throws ExceptionUDT, IllegalArgumentException {
		checkSocketAddress(remoteSocketAddress);
		connect0(remoteSocketAddress);
	}

	/**
	 * Load {@link #remoteSocketAddress} value.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/peername.htm">UDT::peername()</a>
	 */
	protected native boolean hasLoadedRemoteSocketAddress();

	/**
	 * @return null : not connected; <br>
	 *         not null: remote UDT peer socket address to which this socket is
	 *         connected <br>
	 * @see #hasLoadedRemoteSocketAddress()
	 */
	public InetSocketAddress getRemoteSocketAddress() throws ExceptionUDT {
		if (hasLoadedRemoteSocketAddress()) {
			return remoteSocketAddress;
		} else {
			return null;
		}
	}

	/**
	 * Load {@link #localSocketAddress} value.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sockname.htm">UDT::sockname()</a>
	 */
	protected native boolean hasLoadedLocalSocketAddress();

	/**
	 * @return null: not bound; <br>
	 *         not null: local UDT socket address to which the the socket is
	 *         bound<br>
	 * @see #hasLoadedLocalSocketAddress()
	 */
	public InetSocketAddress getLocalSocketAddress() throws ExceptionUDT {
		if (hasLoadedLocalSocketAddress()) {
			return localSocketAddress;
		} else {
			return null;
		}
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/opt.htm">UDT::getsockopt()</a>
	 */
	protected native Object getOption0(final int code, final Class<?> klaz)
			throws ExceptionUDT;

	/**
	 * @see #getOption0(int, Class)
	 */
	public Object getOption(final OptionUDT option) throws ExceptionUDT {
		if (option == null) {
			throw new IllegalArgumentException("option == null");
		}
		return getOption0(option.getCode(), option.getKlaz());
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/opt.htm">UDT::setsockopt()</a>
	 */
	protected native void setOption0(final int code, final Class<?> klaz,
			final Object value) throws ExceptionUDT;

	/**
	 * @see #setOption0(int, Class, Object)
	 */
	public void setOption(final OptionUDT option, final Object value)
			throws ExceptionUDT {
		if (option == null || value == null) {
			throw new IllegalArgumentException(
					"option == null || value == null");
		}
		if (value.getClass() == option.getKlaz()) {
			setOption0(option.getCode(), option.getKlaz(), value);
		} else {
			throw new ExceptionUDT(socketID, ErrorUDT.WRAPPER_MESSAGE,
					"option and value types do not match: "
							+ option.getKlaz().getName() + " vs "
							+ value.getClass().getName());
		}
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/listen.htm">UDT::listen()</a>
	 */
	protected native void listen0(final int queueSize) throws ExceptionUDT;

	/**
	 * java copy of underlying native accept queue size parameter
	 * 
	 * @see #listen(int)
	 * @see #accept()
	 */
	protected volatile int listenQueueSize;

	private final static int JAVA_INT_SIZE_IN_BYTES = 4;

	/**
	 * @param queueSize
	 *            maximum number of queued clients
	 * 
	 * @see #listen0(int)
	 */
	public void listen(final int queueSize) throws ExceptionUDT {
		if (queueSize <= 0) {
			throw new IllegalArgumentException("queueSize <= 0");
		}
		// publisher for volatile
		listenQueueSize = queueSize;
		listen0(queueSize);
	}

	/**
	 * @see #listen(int)
	 */
	public int getListenQueueSize() {
		return listenQueueSize;
	}

	/**
	 * receive into a complete byte array
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected native int receive0(final int socketID, final int socketType, //
			final byte[] array) throws ExceptionUDT;

	/**
	 * receive into a portion of a byte array
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected native int receive1(final int socketID, final int socketType, //
			final byte[] array, final int position, final int limit)
			throws ExceptionUDT;

	/**
	 * receive into a {@link java.nio.channels.DirectByteBuffer}
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected native int receive2(final int socketID, final int socketType, //
			final ByteBuffer buffer, final int position, final int limit)
			throws ExceptionUDT;

	/**
	 * receive into byte[] array upto <code>array.length</code> bytes
	 * 
	 * @return <code>-1</code> : nothing received (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal receive, byte count<br>
	 * @see #receive0(int, int, byte[])
	 */
	public final int receive(final byte[] array) throws ExceptionUDT {
		checkArray(array);
		return receive0(socketID, socketType, array);
	}

	/**
	 * receive into byte[] array upto <code>size=limit-position</code> bytes
	 * 
	 * @return <code>-1</code> : nothing received (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal receive, byte count<br>
	 * @see #receive1(int, int, byte[], int, int)
	 */
	public final int receive(final byte[] array, final int position,
			final int limit) throws ExceptionUDT {
		checkArray(array);
		return receive1(socketID, socketType, array, position, limit);
	}

	/**
	 * receive into {@link java.nio.channels.DirectByteBuffer}; upto
	 * {@link java.nio.ByteBuffer#remaining()} bytes
	 * 
	 * @return <code>-1</code> : nothing received (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal receive, byte count<br>
	 * @see #receive2(int, int, ByteBuffer, int, int)
	 */
	public final int receive(final ByteBuffer buffer) throws ExceptionUDT {
		checkBuffer(buffer);
		final int position = buffer.position();
		final int limit = buffer.limit();
		final int remaining = buffer.remaining();
		final int sizeReceived = receive2(socketID, socketType, //
				buffer, position, limit);
		if (sizeReceived <= 0) {
			return sizeReceived;
		}
		if (sizeReceived <= remaining) {
			buffer.position(position + sizeReceived);
			return sizeReceived;
		} else { // should not happen
			log.error("sizeReceived > remaining");
			return 0;
		}
	}

	/**
	 * WRAPPER_UNIMPLEMENTED
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/recvfile.htm">UDT::recvfile()</a>
	 */
	public final int receiveFile(final ByteBuffer buffer) throws ExceptionUDT {
		throw new ExceptionUDT(//
				socketID, ErrorUDT.WRAPPER_UNIMPLEMENTED, "receiveFile");
	}

	/**
	 * @see com.barchart.udt.nio.SelectorUDT#select()
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/select.htm">UDT::select()</a>
	 */
	protected static native int select0( //
			final int[] readArray, //
			final int[] writeArray, //
			final int[] exceptArray, //
			final int[] sizeArray, //
			final long millisTimeout //
	) throws ExceptionUDT;

	/**
	 * Basic access to UDT socket readiness selection feature. Based on int[]
	 * array info exchange. Timeout is in milliseconds.
	 * 
	 * @return <code><0</code> : should not happen<br>
	 *         <code>=0</code> : timeout, no ready sockets<br>
	 *         <code>>0</code> : total number or reads, writes, exceptions<br>
	 * @see #select0(int[], int[], int[], int[], long)
	 */
	// asserts are contracts
	public final static int select( //
			final int[] readArray, //
			final int[] writeArray, //
			final int[] exceptArray, //
			final int[] sizeArray, //
			final long millisTimeout) throws ExceptionUDT {

		assert readArray != null;
		assert writeArray != null;
		assert exceptArray != null;
		assert sizeArray != null;

		assert readArray.length >= sizeArray[UDT_READ_INDEX];
		assert writeArray.length >= sizeArray[UDT_WRITE_INDEX];
		assert exceptArray.length >= readArray.length;
		assert exceptArray.length >= writeArray.length;

		assert millisTimeout >= DEFAULT_MIN_SELECTOR_TIMEOUT
				|| millisTimeout <= 0;

		return select0(readArray, writeArray, exceptArray, sizeArray,
				millisTimeout);

	}

	/**
	 * @see com.barchart.udt.nio.SelectorUDT#select()
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/select.htm">UDT::select()</a>
	 */
	protected static native int select1( //
			final IntBuffer readBuffer, //
			final IntBuffer writeBuffer, //
			final IntBuffer exceptBuffer, //
			final IntBuffer sizeBuffer, //
			long millisTimeout //
	) throws ExceptionUDT;

	/**
	 * Basic access to UDT socket readiness selection feature. Based on
	 * {@link java.nio.DirectIntBuffer} info exchange.Timeout is in
	 * milliseconds.
	 * 
	 * @return <code><0</code> : should not happen<br>
	 *         <code>=0</code> : timeout, no ready sockets<br>
	 *         <code>>0</code> : total number or reads, writes, exceptions<br>
	 * @see #select1(IntBuffer, IntBuffer, IntBuffer, IntBuffer, long)
	 */
	// asserts are contracts
	public final static int select( //
			final IntBuffer readBuffer, //
			final IntBuffer writeBuffer, //
			final IntBuffer exceptBuffer, //
			final IntBuffer sizeBuffer, //
			final long millisTimeout) throws ExceptionUDT {

		assert readBuffer != null && readBuffer.isDirect();
		assert writeBuffer != null && writeBuffer.isDirect();
		assert exceptBuffer != null && exceptBuffer.isDirect();
		assert sizeBuffer != null && sizeBuffer.isDirect();

		assert readBuffer.capacity() >= sizeBuffer.get(UDT_READ_INDEX);
		assert writeBuffer.capacity() >= sizeBuffer.get(UDT_WRITE_INDEX);
		assert exceptBuffer.capacity() >= readBuffer.capacity();
		assert exceptBuffer.capacity() >= writeBuffer.capacity();

		assert millisTimeout >= DEFAULT_MIN_SELECTOR_TIMEOUT
				|| millisTimeout <= 0;

		return select1(readBuffer, writeBuffer, exceptBuffer, sizeBuffer,
				millisTimeout);

	}

	/**
	 * Basic access to UDT socket readiness selection feature. Based on
	 * {@link java.nio.DirectIntBuffer} info exchange.Timeout is in
	 * milliseconds.
	 * 
	 * @return <code><0</code> : should not happen<br>
	 *         <code>=0</code> : timeout, no ready sockets<br>
	 *         <code>>0</code> : total number or reads, writes, exceptions<br>
	 * @see #epollWait0(int, IntBuffer, IntBuffer, IntBuffer, IntBuffer, long)
	 */
	// asserts are contracts
	public final static int selectEpoll( //
			final IntBuffer readBuffer, //
			final IntBuffer writeBuffer, //
			final IntBuffer exceptBuffer, //
			final IntBuffer sizeBuffer, //
			/* var */long millisTimeout) throws ExceptionUDT {

		if (millisTimeout < 0) {
			millisTimeout = Long.MAX_VALUE;
		}

		assert readBuffer != null && readBuffer.isDirect();
		assert writeBuffer != null && writeBuffer.isDirect();
		assert exceptBuffer != null && exceptBuffer.isDirect();
		assert sizeBuffer != null && sizeBuffer.isDirect();

		// TODO

		// assert readBuffer.capacity() >= sizeBuffer.get(UDT_READ_INDEX);
		// assert writeBuffer.capacity() >= sizeBuffer.get(UDT_WRITE_INDEX);
		// assert exceptBuffer.capacity() >= readBuffer.capacity();
		// assert exceptBuffer.capacity() >= writeBuffer.capacity();
		// assert millisTimeout >= DEFAULT_MIN_SELECTOR_TIMEOUT
		// || millisTimeout <= 0;

		int epollID = 0; // XXX

		// return epollWait(epollID, readBuffer, writeBuffer, exceptBuffer,
		// sizeBuffer, millisTimeout);

		throw new RuntimeException("TODO");

	}

	// ###

	/**
	 * unimplemented / unused
	 */
	protected static native void selectEx0(//
			final int[] registrationArray, //
			final int[] readArray, //
			final int[] writeArray, //
			final int[] exceptionArray, //
			final long timeout) throws ExceptionUDT;

	// #############################

	// note: will be inlined by jvm
	protected static final void checkBuffer(final ByteBuffer buffer) {
		if (buffer == null) {
			throw new IllegalArgumentException("buffer == null");
		}
		if (!buffer.isDirect()) {
			throw new IllegalArgumentException("must use DirectByteBuffer");
		}
	}

	// note: will be inlined by jvm
	protected static final void checkArray(final byte[] array) {
		if (array == null) {
			throw new IllegalArgumentException("array == null");
		}
	}

	// #############################

	/**
	 * send from a complete byte[] array;
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected native int send0(final int socketID, final int socketType, //
			final int timeToLive, final boolean isOrdered, //
			final byte[] array) throws ExceptionUDT;

	/**
	 * send from a portion of a byte[] array;
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected native int send1(final int socketID, final int socketType, //
			final int timeToLive, final boolean isOrdered, //
			final byte[] array, final int arayPosition, final int arrayLimit)
			throws ExceptionUDT;

	/**
	 * send from {@link java.nio.DirectByteBuffer};
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected native int send2(final int socketID,
			final int socketType, //
			final int timeToLive,
			final boolean isOrdered, //
			final ByteBuffer buffer, final int bufferPosition,
			final int bufferLimit) throws ExceptionUDT;

	/**
	 * send from byte[] array upto <code>size=array.length</code> bytes
	 * 
	 * @param array
	 *            array to send
	 * @return <code>-1</code> : no buffer space (non-blocking only) <br>
	 *         <code>=0</code> : timeout expired (blocking only) <br>
	 *         <code>>0</code> : normal send, actual sent byte count <br>
	 * @see #send0(int, int, int, boolean, byte[])
	 */
	public final int send(final byte[] array) throws ExceptionUDT {
		checkArray(array);
		return send0(socketID, socketType, //
				messageTimeTolive, messageIsOrdered, //
				array);
	}

	/**
	 * send from byte[] array upto <code>size=limit-position</code> bytes
	 * 
	 * @param array
	 *            array to send
	 * @param position
	 *            start of array portion to send
	 * @param limit
	 *            finish of array portion to send
	 * @return <code>-1</code> : no buffer space (non-blocking only) <br>
	 *         <code>=0</code> : timeout expired (blocking only) <br>
	 *         <code>>0</code> : normal send, actual sent byte count <br>
	 * @see #send1(int, int, int, boolean, byte[], int, int)
	 */
	public final int send(final byte[] array, final int position,
			final int limit) throws ExceptionUDT {
		checkArray(array);
		return send1(socketID, socketType, //
				messageTimeTolive, messageIsOrdered, //
				array, position, limit);
	}

	/**
	 * send from {@link java.nio.DirectByteBuffer}, upto
	 * {@link java.nio.ByteBuffer#remaining()} bytes
	 * 
	 * @param buffer
	 *            buffer to send
	 * @return <code>-1</code> : no buffer space (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal send, actual sent byte count<br>
	 * @see #send2(int, int, int, boolean, ByteBuffer, int, int)
	 */
	public final int send(final ByteBuffer buffer) throws ExceptionUDT {
		checkBuffer(buffer);
		final int position = buffer.position();
		final int limit = buffer.limit();
		final int remaining = buffer.remaining();
		final int sizeSent = send2(socketID, socketType, //
				messageTimeTolive, messageIsOrdered, //
				buffer, position, limit);
		if (sizeSent <= 0) {
			return sizeSent;
		}
		if (sizeSent <= remaining) {
			buffer.position(position + sizeSent);
			return sizeSent;
		} else { // should not happen
			log.error("sizeSent > remaining");
			return 0;
		}
	}

	/**
	 * default timeToLive value used by sendmsg mode
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	public final void setMessageTimeTolLive(final int timeToLive) {
		// publisher to volatile
		messageTimeTolive = timeToLive;
	}

	/**
	 * default isOrdered value used by sendmsg mode
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	public final void setMessageIsOdered(final boolean isOrdered) {
		// publisher to volatile
		messageIsOrdered = isOrdered;
	}

	/**
	 * WRAPPER_UNIMPLEMENTED
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendfile.htm">UDT::sendfile()</a>
	 */
	public final int sendFile(final ByteBuffer buffer) throws ExceptionUDT {
		throw new ExceptionUDT(//
				socketID, ErrorUDT.WRAPPER_UNIMPLEMENTED, "sendFile");
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/perfmon.htm">perfmon.htm</a>
	 */
	protected native void updateMonitor0(final boolean makeClear)
			throws ExceptionUDT;

	/**
	 * Load updated statistics values into {@link #monitor} object. Must call
	 * this methos only on connected socket.
	 * 
	 * @param makeClear
	 *            true : reset all statistics with this call; false : keep
	 *            collecting statistics, load updated values.
	 * @see #updateMonitor0(boolean)
	 */
	public final void updateMonitor(final boolean makeClear)
			throws ExceptionUDT {
		updateMonitor0(makeClear);
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	protected native int getErrorCode0();

	/**
	 * Error code set by last operation on a socket.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	public final int getErrorCode() {
		return getErrorCode0();
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	protected native String getErrorMessage0();

	/**
	 * Native error message set by last operation on a socket.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	public final String getErrorMessage() {
		return getErrorMessage0();
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	protected native void clearError0();

	/**
	 * Clear error status on a socket, if any.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	public final void clearError() {
		clearError0();
	}

	/**
	 * Check if socket is open. (JDK semantics). The status of underlying UDT
	 * socket is mapped into JDK expected categories
	 * 
	 * @see StatusUDT#isOpenEmulateJDK()
	 */
	public final boolean isOpen() {

		final StatusUDT status = getStatus();

		return status.isOpenEmulateJDK();

	}

	/**
	 * Check if socket is closed. A convenience !isOpen();
	 * 
	 * @see #isOpen()
	 */
	public boolean isClosed() {
		return !isOpen();
	}

	//

	/** TODO add link to UDT doc */
	protected native int getStatus0();

	/**
	 * returns native status of underlying native UDT socket
	 */
	public StatusUDT getStatus() {
		return StatusUDT.fromCode(getStatus0());
	}

	// ###
	// ### UDT API
	// ###################################################

	// convenience methods

	/**
	 * Apply default settings for message mode.
	 * <p>
	 * IsOdered = true;<br>
	 * TimeTolLive = INFINITE_TTL;<br>
	 */
	public final void setDefaultMessageSendMode() {
		setMessageIsOdered(true);
		setMessageTimeTolLive(INFINITE_TTL);
	}

	/**
	 * "Primary" socket. Default constructor; will apply
	 * {@link #setDefaultMessageSendMode()}
	 * 
	 * @param type
	 *            UDT socket type
	 */
	public SocketUDT(final TypeUDT type) throws ExceptionUDT {
		synchronized (SocketUDT.class) {
			this.type = type;
			this.monitor = new MonitorUDT(this);
			this.socketID = initInstance0(type.code);
			this.socketType = type.code;
			this.socketAddressFamily = 2; // ipv4
			setDefaultMessageSendMode();
		}
	}

	/**
	 * "Secondary" socket. Made by {@link #accept0()}, will apply
	 * {@link #setDefaultMessageSendMode()}
	 * 
	 * @param socketID
	 *            UDT socket descriptor;
	 */
	protected SocketUDT(final TypeUDT type, final int socketID)
			throws ExceptionUDT {
		synchronized (SocketUDT.class) {
			this.type = type;
			this.monitor = new MonitorUDT(this);
			this.socketID = initInstance1(socketID);
			this.socketType = type.code;
			this.socketAddressFamily = 2; // ipv4
			setDefaultMessageSendMode();
		}
	}

	/**
	 * Check if socket is bound. (JDK semantics)
	 * 
	 * @return true : {@link #bind(InetSocketAddress)} was successful<br>
	 *         false : otherwise<br>
	 */
	public final boolean isBound() {
		if (isClosed()) {
			return false;
		}
		try {
			return getLocalSocketAddress() != null;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if socket is connected. (JDK semantics)
	 * 
	 * @return true : {@link #connect(InetSocketAddress)} was successful<br>
	 *         false : otherwise<br>
	 */
	public final boolean isConnected() {
		if (isClosed()) {
			return false;
		}
		try {
			return getRemoteSocketAddress() != null;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Error object wrapper.
	 * 
	 * @return error status set by last socket operation
	 **/
	public final ErrorUDT getError() {
		int code = getErrorCode();
		return ErrorUDT.errorFrom(code);
	}

	//

	/**
	 * Configure socket in blocking/non-blocking mode. (JDK semantics)
	 * 
	 * @param block
	 *            true : set both send and receive to blocking mode; false : set
	 *            both send and receive to non-blocking mode
	 * @see java.nio.channels.SocketChannel#configureBlocking(boolean)
	 */
	public final void configureBlocking(final boolean block)
			throws ExceptionUDT {
		if (block) {
			setOption(OptionUDT.Is_Receive_Synchronous, Boolean.TRUE);
			setOption(OptionUDT.Is_Send_Synchronous, Boolean.TRUE);
		} else {
			setOption(OptionUDT.Is_Receive_Synchronous, Boolean.FALSE);
			setOption(OptionUDT.Is_Send_Synchronous, Boolean.FALSE);
		}
	}

	/**
	 * Check if socket is in strict blocking mode. (JDK semantics)
	 * 
	 * @return true : socket is valid and both send and receive are set to
	 *         blocking mode; false : at least one channel is set to
	 *         non-blocking mode or socket is invalid;
	 * 
	 * @see #isNonBlocking()
	 * @see #configureBlocking(boolean)
	 */
	public final boolean isBlocking() {
		try {
			if (isOpen()) {
				boolean isReceiveBlocking = (Boolean) getOption(OptionUDT.Is_Receive_Synchronous);
				boolean isSendBlocking = (Boolean) getOption(OptionUDT.Is_Send_Synchronous);
				return isReceiveBlocking && isSendBlocking;
			}
		} catch (Exception e) {
			log.error("unexpected;", e);
		}
		return false;
	}

	/**
	 * Check if socket is in strict non-blocking mode.
	 * 
	 * @return true : socket is valid and both send and receive are set to NON
	 *         blocking mode; false : at least one channel is set to blocking
	 *         mode or socket is invalid;
	 * @see #isBlocking()
	 * @see #configureBlocking(boolean)
	 */
	public final boolean isNonBlocking() {
		try {
			if (isOpen()) {
				boolean isReceiveBlocking = (Boolean) getOption(OptionUDT.Is_Receive_Synchronous);
				boolean isSendBlocking = (Boolean) getOption(OptionUDT.Is_Send_Synchronous);
				return !isReceiveBlocking && !isSendBlocking;
			}
		} catch (Exception e) {
			log.error("unexpected;", e);
		}
		return false;
	}

	/**
	 * Get maximum send buffer size. Reflects minimum of protocol-level (UDT)
	 * and kernel-level(UDP) settings.
	 * 
	 * @see java.net.Socket#getSendBufferSize()
	 */
	public final int getSendBufferSize() throws ExceptionUDT {
		final int protocolSize = (Integer) getOption(OptionUDT.Protocol_Send_Buffer_Size);
		final int kernelSize = (Integer) getOption(OptionUDT.Kernel_Send_Buffer_Size);
		return Math.min(protocolSize, kernelSize);
	}

	/**
	 * Get maximum receive buffer size. Reflects minimum of protocol-level (UDT)
	 * and kernel-level(UDP) settings.
	 * 
	 * @see java.net.Socket#getReceiveBufferSize()
	 */
	public final int getReceiveBufferSize() throws ExceptionUDT {
		final int protocolSize = (Integer) getOption(OptionUDT.Protocol_Receive_Buffer_Size);
		final int kernelSize = (Integer) getOption(OptionUDT.Kernel_Receive_Buffer_Size);
		return Math.min(protocolSize, kernelSize);
	}

	/**
	 * Check if local bind address is set to reuse mode.
	 * 
	 * @see java.net.Socket#getReuseAddress()
	 */
	public final boolean getReuseAddress() throws ExceptionUDT {
		return (Boolean) getOption(OptionUDT.Is_Address_Reuse_Enabled);
	}

	/**
	 * Get time to linger on close (seconds).
	 * 
	 * @see java.net.Socket#getSoLinger()
	 */
	public final int getSoLinger() throws ExceptionUDT {
		return ((LingerUDT) getOption(OptionUDT.Time_To_Linger_On_Close))
				.intValue();
	}

	/**
	 * Get "any blocking operation" timeout setting.
	 * 
	 * Returns milliseconds; zero return means "infinite"; negative means
	 * invalid
	 * 
	 * @see java.net.Socket#getSoTimeout()
	 */
	public final int getSoTimeout() throws ExceptionUDT {
		final int sendTimeout = (Integer) getOption(OptionUDT.Send_Timeout);
		final int receiveTimeout = (Integer) getOption(OptionUDT.Receive_Timeout);
		final int millisTimeout;
		if (sendTimeout != receiveTimeout) {
			log.error("sendTimeout != receiveTimeout");
			millisTimeout = Math.max(sendTimeout, receiveTimeout);
		} else {
			// map from UDT value convention to java.net.Socket value convention
			if (sendTimeout < 0) {
				// UDT infinite
				millisTimeout = 0;
			} else if (sendTimeout > 0) {
				// UDT finite
				millisTimeout = sendTimeout;
			} else { // ==0
				log.error("UDT reported unexpected zero timeout");
				millisTimeout = -1;
			}
		}
		return millisTimeout;
	}

	/**
	 * Set maximum send buffer size. Affects both protocol-level (UDT) and
	 * kernel-level(UDP) settings
	 * 
	 * @see java.net.Socket#setSendBufferSize(int)
	 */
	public final void setSendBufferSize(final int size) throws ExceptionUDT {
		setOption(OptionUDT.Protocol_Send_Buffer_Size, size);
		setOption(OptionUDT.Kernel_Send_Buffer_Size, size);
	}

	/**
	 * Set maximum receive buffer size. Affects both protocol-level (UDT) and
	 * kernel-level(UDP) settings.
	 */
	public final void setReceiveBufferSize(final int size) throws ExceptionUDT {
		setOption(OptionUDT.Protocol_Receive_Buffer_Size, size);
		setOption(OptionUDT.Kernel_Receive_Buffer_Size, size);
	}

	public final void setReuseAddress(final boolean on) throws ExceptionUDT {
		setOption(OptionUDT.Is_Address_Reuse_Enabled, on);
	}

	public final void setSoLinger(final boolean on, final int linger)
			throws ExceptionUDT {
		if (on) {
			if (linger <= 0) {
				// keep JDK contract for setSoLinger parameters
				throw new IllegalArgumentException("linger <= 0");
			}
			setOption(OptionUDT.Time_To_Linger_On_Close, new LingerUDT(linger));
		} else {
			setOption(OptionUDT.Time_To_Linger_On_Close, LingerUDT.LINGER_ZERO);
		}
	}

	/**
	 * call timeout (milliseconds); Set a timeout on blocking Socket operations:
	 * ServerSocket.accept(); SocketInputStream.read();
	 * DatagramSocket.receive(); Enable/disable SO_TIMEOUT with the specified
	 * timeout, in milliseconds. A timeout of zero is interpreted as an infinite
	 * timeout.
	 */
	public final void setSoTimeout(/* non-final */int millisTimeout)
			throws ExceptionUDT {
		if (millisTimeout < 0) {
			throw new IllegalArgumentException("timeout < 0");
		}
		if (millisTimeout == 0) {
			// UDT uses different value for "infinite"
			millisTimeout = INFINITE_TIMEOUT;
		}
		setOption(OptionUDT.Send_Timeout, millisTimeout);
		setOption(OptionUDT.Receive_Timeout, millisTimeout);
	}

	/**
	 * @return null : not connected<br>
	 *         not null : valid address; result of
	 *         {@link #connect(InetSocketAddress)}<br>
	 */
	public final InetAddress getRemoteInetAddress() {
		try {
			final InetSocketAddress remote = getRemoteSocketAddress();
			if (remote == null) {
				return null;
			} else {
				return remote.getAddress();
			}
		} catch (ExceptionUDT e) {
			log.debug("unexpected", e);
			return null;
		}
	}

	/**
	 * @return 0 : not connected<br>
	 *         >0 : valid port ; result of {@link #connect(InetSocketAddress)}<br>
	 */
	public final int getRemoteInetPort() {
		try {
			final InetSocketAddress remote = getRemoteSocketAddress();
			if (remote == null) {
				return 0;
			} else {
				return remote.getPort();
			}
		} catch (ExceptionUDT e) {
			log.debug("unexpected", e);
			return 0;
		}
	}

	/**
	 * @return null : not bound<br>
	 *         not null : valid address; result of
	 *         {@link #bind(InetSocketAddress)}<br>
	 */
	public final InetAddress getLocalInetAddress() {
		try {
			final InetSocketAddress local = getLocalSocketAddress();
			if (local == null) {
				return null;
			} else {
				return local.getAddress();
			}
		} catch (ExceptionUDT e) {
			log.debug("unexpected", e);
			return null;
		}
	}

	/**
	 * @return 0 : not bound<br>
	 *         >0 : valid port; result of {@link #bind(InetSocketAddress)}<br>
	 */
	public final int getLocalInetPort() {
		try {
			final InetSocketAddress local = getLocalSocketAddress();
			if (local == null) {
				return 0;
			} else {
				return local.getPort();
			}
		} catch (ExceptionUDT e) {
			log.debug("unexpected", e);
			return 0;
		}
	}

	//

	/**
	 * Note: uses {@link #socketID} as hash code.
	 */
	@Override
	public final int hashCode() {
		return socketID;
	}

	/**
	 * Note: equality is based on {@link #socketID}.
	 */
	@Override
	public final boolean equals(final Object otherSocketUDT) {
		if (otherSocketUDT instanceof SocketUDT) {
			final SocketUDT other = (SocketUDT) otherSocketUDT;
			return other.socketID == this.socketID;
		}
		return false;
	}

	//
	@Override
	public String toString() {
		return " socketID="
				+ socketID //
				+ " type="
				+ type //
				+ " isOpen="
				+ isOpen() //
				+ " isNonBlocking="
				+ isNonBlocking() //
				+ " local=" + getLocalInetAddress() + ":"
				+ getLocalInetPort()//
				+ " remote=" + getRemoteInetAddress() + ":"
				+ getRemoteInetPort()//
		;
	}

	/**
	 * Show current socket options.
	 */
	public String toStringOptions() {

		final StringBuilder text = new StringBuilder(1024);

		OptionUDT.appendSnapshot(this, text);

		return text.toString();

	}

	/**
	 * Show current monitor status.
	 */
	public String toStringMonitor() {

		try {
			updateMonitor(false);
		} catch (Exception e) {
			return "updateMonitor failed;" + e.getMessage();
		}

		final StringBuilder text = new StringBuilder(1024);

		monitor.appendSnapshot(text);

		return text.toString();

	}

	//

	/**
	 * @return epoll id
	 * 
	 * @see <a http://udt.sourceforge.net/udt4/doc/epoll.htm</a>
	 */
	protected static native int epollCreate() throws ExceptionUDT;

	/**
	 * @see <a http://udt.sourceforge.net/udt4/doc/epoll.htm</a>
	 */
	protected static native void epollRelease(final int epollID)
			throws ExceptionUDT;

	/**
	 * @see <a http://udt.sourceforge.net/udt4/doc/epoll.htm</a>
	 */
	protected static native void epollAdd(//
			final int epollID, final int socketID) throws ExceptionUDT;

	/**
	 * @see <a http://udt.sourceforge.net/udt4/doc/epoll.htm</a>
	 */
	protected static native void epollRemove(//
			final int epollID, final int socketID) throws ExceptionUDT;

	/**
	 * @see <a http://udt.sourceforge.net/udt4/doc/epoll.htm</a>
	 */
	protected static native int epollWait( //
			final int epollID, //
			final IntBuffer readBuffer, //
			final IntBuffer writeBuffer, //
			final IntBuffer exceptBuffer, //
			final IntBuffer sizeBuffer, //
			final long millisTimeout) throws ExceptionUDT;

	// ###########################################
	// ### used for development & testing only
	// ###

	native void testEmptyCall0();

	native void testIterateArray0(Object[] array);

	native void testIterateSet0(Set<Object> set);

	native int[] testMakeArray0(int size);

	native void testGetSetArray0(int[] array, boolean isReturn);

	native void testInvalidClose0(int socketID) throws ExceptionUDT;

	native void testCrashJVM0();

	native void testDirectByteBufferAccess0(ByteBuffer buffer);

	native void testDirectIntBufferAccess0(IntBuffer buffer);

	native void testFillArray0(byte[] array);

	native void testFillBuffer0(ByteBuffer buffer);

	native void testSocketStatus0();

	native void testEpoll0(); //

	public static final IntBuffer newDirectIntBufer(int capacity) {
		return ByteBuffer. //
				allocateDirect(capacity * SocketUDT.JAVA_INT_SIZE_IN_BYTES). //
				order(ByteOrder.nativeOrder()). //
				asIntBuffer();
	}

	// ###
	// ### used for development & testing only
	// ###########################################

}
