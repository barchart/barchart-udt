/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.anno.Native;
import com.barchart.udt.lib.LibraryLoader;
import com.barchart.udt.nio.KindUDT;
import com.barchart.udt.util.HelpUDT;

/**
 * UDT native socket wrapper
 */
public class SocketUDT {

	/**
	 * Maximum number of connections queued in listening mode by
	 * {@link #accept()}
	 */
	public static final int DEFAULT_ACCEPT_QUEUE_SIZE = 256;

	/**
	 * Block size used by {@link #sendFile(File, long, long)}
	 */
	public static final int DEFAULT_FILE_BLOCK_SIZE = 1 * 1024 * 1024;

	/**
	 * Maximum number sockets that can participate in a
	 * {@link com.barchart.udt.nio.SelectorUDT#select()} operation; see epoll.h
	 * to confirm current limit
	 */
	public static final int DEFAULT_MAX_SELECTOR_SIZE = 1024;

	/**
	 * Minimum timeout of a {@link com.barchart.udt.nio.SelectorUDT#select()}
	 * operations.
	 */
	public static final int DEFAULT_MIN_SELECTOR_TIMEOUT = 10;

	/**
	 * infinite message time to live;
	 */
	public static final int INFINITE_TTL = -1;

	/**
	 * Helper value that can be checked from CCC class and force JNI library
	 * load
	 */
	@Native
	public static boolean INIT_OK = false;

	protected static final Logger log = LoggerFactory
			.getLogger(SocketUDT.class);

	/**
	 * JNI Signature that must match between java code and c++ code on all
	 * platforms; failure to match will abort native library load, as an
	 * indication of inconsistent build.
	 */
	@Native
	public static final int SIGNATURE_JNI = 20130512; // VersionUDT.BUILDTIME;

	/**
	 * infinite timeout:
	 * <p>
	 * blocking send/receive
	 * <p>
	 * epoll wait
	 */
	public static final int TIMEOUT_INFINITE = -1;

	/**
	 * zero timeout:
	 * <p>
	 * epoll wait
	 */
	public static long TIMEOUT_NONE = 0;

	/**
	 * UDT::select() sizeArray/sizeBuffer index offset for EXCEPTION report
	 */
	@Native
	public static final int UDT_EXCEPT_INDEX = 2;

	/**
	 * UDT::select() sizeArray/sizeBuffer index offset for READ interest
	 */
	@Native
	public static final int UDT_READ_INDEX = 0;

	/**
	 * UDT::select() sizeArray/sizeBuffer size count or number of arrays/buffers
	 */
	@Native
	public static final int UDT_SIZE_COUNT = 3;

	/**
	 * UDT::select() sizeArray/sizeBuffer index offset for WRITE interest
	 */
	@Native
	public static final int UDT_WRITE_INDEX = 1;

	/**
	 * Native library loader.
	 * 
	 * @throws RuntimeException
	 */
	static {

		try {

			final String location = ResourceUDT.getLibraryExtractLocation();

			log.info("library location : {}", location);

			final String loaderName = ResourceUDT.getLibraryLoaderClassName();

			log.info("loader provider  : {}", loaderName);

			@SuppressWarnings("unchecked")
			final Class<LibraryLoader> loaderClass = //
			(Class<LibraryLoader>) Class.forName(loaderName);

			final LibraryLoader loaderInstance = loaderClass.newInstance();

			loaderInstance.load(location);

		} catch (final Throwable e) {
			log.error("Failed to LOAD native library", e);
			throw new RuntimeException("load", e);
		}

		try {
			initClass0();
		} catch (final Throwable e) {
			log.error("Failed to INIT native library", e);
			throw new RuntimeException("init", e);
		}

		if (SIGNATURE_JNI != getSignatureJNI0()) {
			log.error("Java/Native SIGNATURE inconsistent");
			throw new RuntimeException("signature");
		}

		INIT_OK = true;

		log.debug("native library load & init OK");

	}

	/**
	 * Cleans up global JNI references and the UDT library.
	 * <p>
	 * The behavior of SocketUDT class after a call to cleanup is undefined, so
	 * it should only ever be called once you are done and you are ready for the
	 * class loader to unload the JNI library
	 * 
	 * @throws ExceptionUDT
	 */
	public static void cleanup() throws ExceptionUDT {
		stopClass0();
	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/epoll.htm">UDT::epoll_add_usock()</a>
	 */
	protected static native void epollAdd0( //
			final int epollID, //
			final int socketID, //
			final int epollOpt //
	) throws ExceptionUDT;

	/**
	 * @return epoll id
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/epoll.htm">UDT::epoll_create()</a>
	 */
	protected static native int epollCreate0() throws ExceptionUDT;

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/epoll.htm">UDT::epoll_release()</a>
	 */
	protected static native void epollRelease0(final int epollID)
			throws ExceptionUDT;

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/epoll.htm">UDT::epoll_remove_usock()</a>
	 */
	protected static native void epollRemove0( //
			final int epollID, final int socketID) throws ExceptionUDT;

	/**
	 * update epoll mask
	 */
	protected static native void epollUpdate0(int epollID, int socketID,
			int epollMask) throws ExceptionUDT;

	/**
	 * query epoll mask
	 */
	protected static native int epollVerify0(int epollID, int socketID)
			throws ExceptionUDT;

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/epoll.htm">UDT::epoll_wait()</a>
	 */
	protected static native int epollWait0( //
			final int epollID, //
			final IntBuffer readBuffer, //
			final IntBuffer writeBuffer, //
			final IntBuffer sizeBuffer, //
			final long millisTimeout) throws ExceptionUDT;

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
	 *      href="http://udt.sourceforge.net/udt4/doc/startup.htm">UDT::startup()</a>
	 */
	protected static native void initClass0() throws ExceptionUDT;

	/**
	 * receive into a complete byte array
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected static native int receive0(//
			final int socketID, //
			final int socketType, //
			final byte[] array //
	) throws ExceptionUDT;

	/**
	 * receive into a portion of a byte array
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected static native int receive1( //
			final int socketID, //
			final int socketType, //
			final byte[] array, //
			final int position, //
			final int limit //
	) throws ExceptionUDT;

	/**
	 * receive into a {@link java.nio.channels.DirectByteBuffer}
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected static native int receive2( //
			final int socketID, //
			final int socketType, //
			final ByteBuffer buffer, //
			final int position, //
			final int limit //
	) throws ExceptionUDT;

	/**
	 * Receive file.
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendfile.htm">UDT::recvfile</a>
	 */
	protected static native long receiveFile0( //
			final int socketID, //
			final String path, //
			long offset, //
			long length, //
			int block //
	) throws ExceptionUDT;

	/**
	 * Basic access to UDT socket readiness selection feature. Based on
	 * {@link java.nio.DirectIntBuffer} info exchange.Timeout is in
	 * milliseconds.
	 * 
	 * @param millisTimeout
	 * 
	 *            http://udt.sourceforge.net/udt4/doc/epoll.htm
	 * 
	 *            "Finally, for epoll_wait, negative timeout value will make the
	 *            function to wait until an event happens. If the timeout value
	 *            is 0, then the function returns immediately with any sockets
	 *            associated an IO event. If timeout occurs before any event
	 *            happens, the function returns 0".
	 * 
	 * 
	 * @return <code><0</code> : should not happen<br>
	 *         <code>=0</code> : timeout, no ready sockets<br>
	 *         <code>>0</code> : total number or reads, writes, exceptions<br>
	 * 
	 * @see #epollWait0(int, IntBuffer, IntBuffer, IntBuffer, long)
	 */
	public static int selectEpoll( //
			final int epollId, //
			final IntBuffer readBuffer, //
			final IntBuffer writeBuffer, //
			final IntBuffer sizeBuffer, //
			final long millisTimeout) throws ExceptionUDT {

		/** asserts are contracts */

		assert readBuffer != null && readBuffer.isDirect();
		assert writeBuffer != null && writeBuffer.isDirect();
		assert sizeBuffer != null && sizeBuffer.isDirect();

		return epollWait0( //
				epollId, //
				readBuffer, //
				writeBuffer, //
				sizeBuffer, //
				millisTimeout //
		);

	}

	/**
	 * send from a complete byte[] array;
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected static native int send0( //
			final int socketID, //
			final int socketType, //
			final int timeToLive, //
			final boolean isOrdered, //
			final byte[] array //
	) throws ExceptionUDT;

	/**
	 * send from a portion of a byte[] array;
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected static native int send1( //
			final int socketID, //
			final int socketType, //
			final int timeToLive, //
			final boolean isOrdered, //
			final byte[] array, // /
			final int arayPosition, //
			final int arrayLimit //
	) throws ExceptionUDT;

	/**
	 * send from {@link java.nio.DirectByteBuffer};
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected static native int send2( //
			final int socketID, //
			final int socketType, //
			final int timeToLive, //
			final boolean isOrdered, //
			final ByteBuffer buffer, //
			final int bufferPosition, //
			final int bufferLimit //
	) throws ExceptionUDT;

	/**
	 * Send file.
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendfile.htm">UDT::sendfile</a>
	 */
	protected static native long sendFile0( //
			final int socketID, //
			final String path, //
			long offset, //
			long length, //
			int block //
	) throws ExceptionUDT;

	/**
	 * Call this before unloading native library.
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/cleanup.htm.htm">UDT::cleanup()</a>
	 */
	protected static native void stopClass0() throws ExceptionUDT;

	// ###########################################
	// ### used for development & testing only
	// ###

	protected static native void testCrashJVM0();

	protected static native void testDirectByteBufferAccess0(ByteBuffer buffer);

	protected static native void testDirectIntBufferAccess0(IntBuffer buffer);

	protected static native void testDirectIntBufferLoad0(IntBuffer buffer);

	protected static native void testEmptyCall0();

	protected static native void testFillArray0(byte[] array);

	protected static native void testFillBuffer0(ByteBuffer buffer);

	protected static native void testGetSetArray0(int[] array, boolean isReturn);

	protected static native void testInvalidClose0(int socketID)
			throws ExceptionUDT;

	protected static native void testIterateArray0(Object[] array);

	protected static native void testIterateSet0(Set<Object> set);

	protected static native int[] testMakeArray0(int size);

	// ###
	// ### used for development & testing only
	// ###########################################

	/**
	 * java copy of underlying native accept queue size parameter
	 * 
	 * @see #listen(int)
	 * @see #accept()
	 */
	private volatile int listenQueueSize;

	/**
	 * local end point; loaded by JNI by {@link #hasLoadedLocalSocketAddress()}
	 */
	@Native
	private volatile InetSocketAddress localSocketAddress;

	/**
	 */
	private volatile boolean messageIsOrdered;

	/**
	 */
	private volatile int messageTimeTolive;

	/**
	 */
	@Native
	private final MonitorUDT monitor;

	/**
	 * remote end point; loaded by JNI by
	 * {@link #hasLoadedRemoteSocketAddress()}
	 */
	@Native
	private volatile InetSocketAddress remoteSocketAddress;

	/**
	 * native address family; read by JNI
	 */
	@Native
	private final int socketAddressFamily;

	/**
	 * native descriptor; read by JNI; see udt.h "typedef int UDTSOCKET;"
	 */
	@Native
	private final int socketID;

	/**
	 */
	@Native
	private final TypeUDT type;

	/**
	 * "Primary" socket. Default constructor; will apply
	 * {@link #setDefaultMessageSendMode()}
	 * 
	 * @param type
	 *            UDT socket type
	 */
	public SocketUDT(final TypeUDT type) throws ExceptionUDT {
		synchronized (SocketUDT.class) {
			if (Boolean.valueOf(System.getProperty("java.net.preferIPv6Addresses")))
				this.socketAddressFamily = 10; // ipv6
			else
				this.socketAddressFamily = 2; // ipv4
			this.type = type;
			this.monitor = new MonitorUDT(this);
			this.socketID = initInstance0(type.code);
			setDefaultMessageSendMode();
		}
		log.debug("init : {}", this);
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
			if (Boolean.valueOf(System.getProperty("java.net.preferIPv6Addresses")))
				this.socketAddressFamily = 10; // ipv6
			else
				this.socketAddressFamily = 2; // ipv4
			this.type = type;
			this.monitor = new MonitorUDT(this);
			this.socketID = initInstance1(socketID);
			setDefaultMessageSendMode();
		}
		log.debug("init : {}", this);
	}

	/**
	 * @return null : no incoming connections (non-blocking mode only)<br>
	 *         non null : newly accepted SocketUDT (both blocking and
	 *         non-blocking)<br>
	 */
	public SocketUDT accept() throws ExceptionUDT {
		return accept0();
	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/accept.htm">UDT::accept()</a>
	 */
	protected native SocketUDT accept0() throws ExceptionUDT;

	public void bind(final InetSocketAddress localSocketAddress) //
			throws ExceptionUDT, IllegalArgumentException {
		HelpUDT.checkSocketAddress(localSocketAddress);
		bind0(localSocketAddress);
	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/bind.htm">UDT::bind()</a>
	 */
	protected native void bind0(final InetSocketAddress localSocketAddress)
			throws ExceptionUDT;

	/**
	 * Clear error status on a socket, if any.
	 * 
	 * @see <a href="http://udt.sourceforge.net/udt4/doc/t-error.htm">UDT Error
	 *      Handling</a>
	 */
	public void clearError() {
		clearError0();
	}

	/**
	 * @see <a href="http://udt.sourceforge.net/udt4/doc/t-error.htm">UDT Error
	 *      Handling</a>
	 */
	protected native void clearError0();

	/**
	 * Close socket if not already closed.
	 * 
	 * @see #close0()
	 */
	public void close() throws ExceptionUDT {
		synchronized (SocketUDT.class) {
			switch (status()) {
			case INIT:
			case OPENED:
			case LISTENING:
			case CONNECTING:
			case CONNECTED:
			case BROKEN:
				/** Requires close. */
				close0();
				log.debug("done : {}", this);
				break;
			case CLOSING:
			case CLOSED:
			case NONEXIST:
				/** Effectively closed. */
				log.debug("dead : {}", this);
				break;
			default:
				log.error("Invalid socket/status {}/{}", this, status());
			}
		}
	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/close.htm">UDT::close()</a>
	 */
	protected native void close0() throws ExceptionUDT;

	/**
	 * Connect to remote UDT socket.
	 * <p>
	 * Can be blocking or non blocking call; depending on
	 * {@link OptionUDT#Is_Receive_Synchronous}
	 * <p>
	 * Timing: UDT uses hard coded connect timeout:
	 * <p>
	 * normal socket: 3 seconds
	 * <p>
	 * rendezvous socket: 30 seconds; when
	 * {@link OptionUDT#Is_Randezvous_Connect_Enabled} is true
	 * 
	 * @see #connect0(InetSocketAddress)
	 */
	public void connect(final InetSocketAddress remoteSocketAddress) //
			throws ExceptionUDT {
		HelpUDT.checkSocketAddress(remoteSocketAddress);
		connect0(remoteSocketAddress);
	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/connect.htm">UDT::connect()</a>
	 */
	protected native void connect0(final InetSocketAddress remoteSocketAddress)
			throws ExceptionUDT;

	/**
	 * Note: equality is based on {@link #socketID}.
	 */
	@Override
	public boolean equals(final Object otherSocketUDT) {
		if (otherSocketUDT instanceof SocketUDT) {
			final SocketUDT other = (SocketUDT) otherSocketUDT;
			return other.socketID == this.socketID;
		}
		return false;
	}

	/**
	 * NOTE: catch all exceptions; else prevents GC
	 * <p>
	 * NOTE: do not leak "this" references; else prevents GC
	 */
	@Override
	protected void finalize() {
		try {
			close();
			super.finalize();
		} catch (final Throwable e) {
			log.error("failed to close id=" + socketID, e);
		}
	}

	/**
	 * Error object wrapper.
	 * 
	 * @return error status set by last socket operation
	 **/
	public ErrorUDT getError() {
		final int code = getErrorCode();
		return ErrorUDT.errorFrom(code);
	}

	/**
	 * Error code set by last operation on a socket.
	 * 
	 * @see <a href="http://udt.sourceforge.net/udt4/doc/t-error.htm">UDT Error
	 *      Handling</a>
	 */
	public int getErrorCode() {
		return getErrorCode0();
	}

	/**
	 * @see <a href="http://udt.sourceforge.net/udt4/doc/t-error.htm">UDT Error
	 *      Handling</a>
	 */
	protected native int getErrorCode0();

	/**
	 * Native error message set by last operation on a socket.
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/t-error.htm">t-error.htm</a>
	 */
	public String getErrorMessage() {
		return getErrorMessage0();
	}

	/**
	 * @see <a href="http://udt.sourceforge.net/udt4/doc/t-error.htm">UDT Error
	 *      Handling</a>
	 */
	protected native String getErrorMessage0();

	/**
	 * @see #listen(int)
	 */
	public int getListenQueueSize() {
		return listenQueueSize;
	}

	/**
	 * @return null : not bound<br>
	 *         not null : valid address; result of
	 *         {@link #bind(InetSocketAddress)}<br>
	 */
	public InetAddress getLocalInetAddress() {
		try {
			final InetSocketAddress local = getLocalSocketAddress();
			if (local == null) {
				return null;
			} else {
				return local.getAddress();
			}
		} catch (final Exception e) {
			log.debug("failed to get local address", e);
			return null;
		}
	}

	/**
	 * @return 0 : not bound<br>
	 *         >0 : valid port; result of {@link #bind(InetSocketAddress)}<br>
	 */
	public int getLocalInetPort() {
		try {
			final InetSocketAddress local = getLocalSocketAddress();
			if (local == null) {
				return 0;
			} else {
				return local.getPort();
			}
		} catch (final Exception e) {
			log.debug("failed to get local port", e);
			return 0;
		}
	}

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
	 * default isOrdered value used by sendmsg mode
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	public boolean getMessageIsOdered() {
		return messageIsOrdered;
	}

	/**
	 * default timeToLive value used by sendmsg mode
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	public int getMessageTimeTolLive() {
		return messageTimeTolive;
	}

	/**
	 * @see #getOption0(int, Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getOption(final OptionUDT<T> option) throws ExceptionUDT {

		if (option == null) {
			throw new IllegalArgumentException("option == null");
		}

		return (T) getOption0(option.code(), option.type());

	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/opt.htm">UDT::getsockopt()</a>
	 */
	protected native Object getOption0(final int code, final Class<?> klaz)
			throws ExceptionUDT;

	/**
	 * Get maximum receive buffer size. Reflects minimum of protocol-level (UDT)
	 * and kernel-level(UDP) settings.
	 * 
	 * @see java.net.Socket#getReceiveBufferSize()
	 */
	public int getReceiveBufferSize() throws ExceptionUDT {
		final int protocolSize = getOption(OptionUDT.Protocol_Receive_Buffer_Size);
		final int kernelSize = getOption(OptionUDT.System_Receive_Buffer_Size);
		return Math.min(protocolSize, kernelSize);
	}

	//

	/**
	 * @return null : not connected<br>
	 *         not null : valid address; result of
	 *         {@link #connect(InetSocketAddress)}<br>
	 */
	public InetAddress getRemoteInetAddress() {
		try {
			final InetSocketAddress remote = getRemoteSocketAddress();
			if (remote == null) {
				return null;
			} else {
				return remote.getAddress();
			}
		} catch (final Exception e) {
			log.debug("failed to get remote address", e);
			return null;
		}
	}

	/**
	 * @return 0 : not connected<br>
	 *         >0 : valid port ; result of {@link #connect(InetSocketAddress)}<br>
	 */
	public int getRemoteInetPort() {
		try {
			final InetSocketAddress remote = getRemoteSocketAddress();
			if (remote == null) {
				return 0;
			} else {
				return remote.getPort();
			}
		} catch (final Exception e) {
			log.debug("failed to get remote port", e);
			return 0;
		}
	}

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
	 * Check if local bind address is set to reuse mode.
	 * 
	 * @see java.net.Socket#getReuseAddress()
	 */
	public boolean getReuseAddress() throws ExceptionUDT {
		return getOption(OptionUDT.Is_Address_Reuse_Enabled);
	}

	/**
	 * Get maximum send buffer size. Reflects minimum of protocol-level (UDT)
	 * and kernel-level(UDP) settings.
	 * 
	 * @see java.net.Socket#getSendBufferSize()
	 */
	public int getSendBufferSize() throws ExceptionUDT {
		final int protocolSize = getOption(OptionUDT.Protocol_Send_Buffer_Size);
		final int kernelSize = getOption(OptionUDT.System_Send_Buffer_Size);
		return Math.min(protocolSize, kernelSize);
	}

	/**
	 * Get time to linger on close (seconds).
	 * 
	 * @see java.net.Socket#getSoLinger()
	 */
	public int getSoLinger() throws ExceptionUDT {
		return getOption(OptionUDT.Time_To_Linger_On_Close).intValue();
	}

	/**
	 * Get "any blocking operation" timeout setting.
	 * 
	 * Returns milliseconds; zero return means "infinite"; negative means
	 * invalid
	 * 
	 * @see java.net.Socket#getSoTimeout()
	 */
	public int getSoTimeout() throws ExceptionUDT {
		final int sendTimeout = getOption(OptionUDT.Send_Timeout);
		final int receiveTimeout = getOption(OptionUDT.Receive_Timeout);
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
	 * @see <a href="http://udt.sourceforge.net/udt4/doc/socket.htm"></a>
	 */
	protected native int getStatus0();

	//

	/**
	 * Note: uses {@link #socketID} as hash code.
	 */
	@Override
	public int hashCode() {
		return socketID;
	}

	/**
	 * Load {@link #localSocketAddress} value.
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sockname.htm">UDT::sockname()</a>
	 */
	protected native boolean hasLoadedLocalSocketAddress();

	/**
	 * Load {@link #remoteSocketAddress} value.
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/peername.htm">UDT::peername()</a>
	 */
	protected native boolean hasLoadedRemoteSocketAddress();

	/**
	 * native socket descriptor id; assigned by udt library
	 */
	public int id() {
		return socketID;
	}

	/**
	 * used by default constructor
	 */
	protected native int initInstance0(int typeCode) throws ExceptionUDT;

	/**
	 * used by accept() internally
	 */
	protected native int initInstance1(int socketUDT) throws ExceptionUDT;

	/**
	 * Check if socket is in strict blocking mode. (JDK semantics)
	 * 
	 * @return true : socket is valid and both send and receive are set to
	 *         blocking mode; false : at least one channel is set to
	 *         non-blocking mode or socket is invalid;
	 * 
	 * @see #isNonBlocking()
	 * @see #setBlocking(boolean)
	 */
	public boolean isBlocking() {
		try {
			if (isOpen()) {
				final boolean isReceiveBlocking = getOption(OptionUDT.Is_Receive_Synchronous);
				final boolean isSendBlocking = getOption(OptionUDT.Is_Send_Synchronous);
				return isReceiveBlocking && isSendBlocking;
			}
		} catch (final Exception e) {
			log.error("failed to get option", e);
		}
		return false;
	}

	/**
	 * Check if socket is bound. (JDK semantics)
	 * 
	 * @return true : {@link #bind(InetSocketAddress)} was successful<br>
	 *         false : otherwise<br>
	 */
	public boolean isBound() {
		switch (status()) {
		case OPENED:
		case CONNECTING:
		case CONNECTED:
		case LISTENING:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check if socket is closed. A convenience !isOpen();
	 * 
	 * @see #isOpen()
	 */
	public boolean isClosed() {
		return !isOpen();
	}

	/**
	 * Check if {@link KindUDT#CONNECTOR} socket is connected. (JDK semantics)
	 * 
	 * @return true : {@link #connect(InetSocketAddress)} was successful<br>
	 *         false : otherwise<br>
	 */
	public boolean isConnected() {
		switch (status()) {
		case CONNECTED:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check if socket is in strict non-blocking mode.
	 * 
	 * @return true : socket is valid and both send and receive are set to NON
	 *         blocking mode; false : at least one channel is set to blocking
	 *         mode or socket is invalid;
	 * @see #isBlocking()
	 * @see #setBlocking(boolean)
	 */
	public boolean isNonBlocking() {
		try {
			if (isOpen()) {
				final boolean isReceiveBlocking = getOption(OptionUDT.Is_Receive_Synchronous);
				final boolean isSendBlocking = getOption(OptionUDT.Is_Send_Synchronous);
				return !isReceiveBlocking && !isSendBlocking;
			}
		} catch (final Exception e) {
			log.error("failed to get option", e);
		}
		return false;
	}

	/**
	 * Check if socket is open. (JDK semantics). The status of underlying UDT
	 * socket is mapped into JDK expected categories
	 * 
	 * @see StatusUDT
	 */
	public boolean isOpen() {
		switch (status()) {
		case INIT:
		case OPENED:
		case LISTENING:
		case CONNECTING:
		case CONNECTED:
			return true;
		default:
			return false;
		}
	}

	public boolean isRendezvous() {
		try {
			if (isOpen()) {
				return getOption(OptionUDT.Is_Randezvous_Connect_Enabled);
			}
		} catch (final Exception e) {
			log.error("failed to get option", e);
		}
		return false;
	}

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
		listenQueueSize = queueSize;
		listen0(queueSize);
	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/listen.htm">UDT::listen()</a>
	 */
	protected native void listen0(final int queueSize) throws ExceptionUDT;

	/**
	 * performance monitor; updated by {@link #updateMonitor(boolean)} in JNI
	 * 
	 * @see #updateMonitor(boolean)
	 */
	public MonitorUDT monitor() {
		return monitor;
	}

	/**
	 * receive into byte[] array upto <code>array.length</code> bytes
	 * 
	 * @return <code>-1</code> : nothing received (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal receive, byte count<br>
	 * @see #receive0(int, int, byte[])
	 */
	public int receive(final byte[] array) throws ExceptionUDT {

		HelpUDT.checkArray(array);

		return receive0(socketID, type.code, array);

	}

	/**
	 * receive into byte[] array upto <code>size=limit-position</code> bytes
	 * 
	 * @return <code>-1</code> : nothing received (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal receive, byte count<br>
	 * @see #receive1(int, int, byte[], int, int)
	 */
	public int receive(final byte[] array, final int position, final int limit)
			throws ExceptionUDT {

		HelpUDT.checkArray(array);

		return receive1(socketID, type.code, array, position, limit);

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
	public int receive(final ByteBuffer buffer) throws ExceptionUDT {

		HelpUDT.checkBuffer(buffer);

		final int position = buffer.position();
		final int limit = buffer.limit();
		final int remaining = buffer.remaining();

		final int sizeReceived = //
		receive2(socketID, type.code, buffer, position, limit);

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

	//

	/**
	 * Receive file from remote peer.
	 * 
	 * @see #receiveFile0(int, String, long, long, int)
	 */
	public long receiveFile( //
			final File file, //
			final long offset, //
			final long length//
	) throws ExceptionUDT {

		if (type == TypeUDT.DATAGRAM) {
			throw new IllegalStateException("invalid socket type : " + type);
		}

		if (file == null || !file.exists() || !file.isFile()
				|| !file.canWrite()) {
			throw new IllegalArgumentException("invalid file : " + file);
		}

		if (offset < 0 || offset > file.length()) {
			throw new IllegalArgumentException("invalid offset : " + offset);
		}

		if (length < 0 || offset + length > file.length()) {
			throw new IllegalArgumentException("invalid length : " + length);
		}

		final String path = file.getAbsolutePath();

		final int block;
		if (length > DEFAULT_FILE_BLOCK_SIZE) {
			block = DEFAULT_FILE_BLOCK_SIZE;
		} else {
			block = (int) length;
		}

		return receiveFile0(id(), path, offset, length, block);

	}

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
	public int send(final byte[] array) throws ExceptionUDT {

		HelpUDT.checkArray(array);

		return send0( //
				socketID, //
				type.code, //
				messageTimeTolive, //
				messageIsOrdered, //
				array //
		);

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
	public int send( //
			final byte[] array, //
			final int position, //
			final int limit //
	) throws ExceptionUDT {

		HelpUDT.checkArray(array);

		return send1( //
				socketID, //
				type.code, //
				messageTimeTolive, //
				messageIsOrdered, //
				array, //
				position, //
				limit //
		);

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
	public int send(final ByteBuffer buffer) throws ExceptionUDT {

		HelpUDT.checkBuffer(buffer);

		final int position = buffer.position();
		final int limit = buffer.limit();
		final int remaining = buffer.remaining();

		final int sizeSent = send2( //
				socketID, //
				type.code, //
				messageTimeTolive, //
				messageIsOrdered, //
				buffer, //
				position, //
				limit //
		);

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
	 * Send file to remote peer.
	 * 
	 * @see #sendFile0(int, String, long, long, int)
	 */
	public long sendFile( //
			final File file, //
			final long offset, //
			final long length//
	) throws ExceptionUDT {

		if (type == TypeUDT.DATAGRAM) {
			throw new IllegalStateException("invalid socket type : " + type);
		}

		if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
			throw new IllegalArgumentException("invalid file : " + file);
		}

		if (offset < 0 || offset > file.length()) {
			throw new IllegalArgumentException("invalid offset : " + offset);
		}

		if (length < 0 || offset + length > file.length()) {
			throw new IllegalArgumentException("invalid length : " + length);
		}

		final String path = file.getAbsolutePath();

		final int block;
		if (length > DEFAULT_FILE_BLOCK_SIZE) {
			block = DEFAULT_FILE_BLOCK_SIZE;
		} else {
			block = (int) length;
		}

		return sendFile0(id(), path, offset, length, block);

	}

	//

	/**
	 * Configure socket in strict blocking / strict non-blocking mode.
	 * 
	 * @param block
	 *            true : set both send and receive to blocking mode; false : set
	 *            both send and receive to non-blocking mode
	 * @see java.nio.channels.SocketChannel#configureBlocking(boolean)
	 */
	public void setBlocking(final boolean block) throws ExceptionUDT {
		if (block) {
			setOption(OptionUDT.Is_Receive_Synchronous, Boolean.TRUE);
			setOption(OptionUDT.Is_Send_Synchronous, Boolean.TRUE);
		} else {
			setOption(OptionUDT.Is_Receive_Synchronous, Boolean.FALSE);
			setOption(OptionUDT.Is_Send_Synchronous, Boolean.FALSE);
		}
	}

	/**
	 * Apply default settings for message mode.
	 * <p>
	 * IsOdered = true;<br>
	 * TimeTolLive = INFINITE_TTL;<br>
	 */
	public void setDefaultMessageSendMode() {
		setMessageIsOdered(true);
		setMessageTimeTolLive(INFINITE_TTL);
	}

	/**
	 * default isOrdered value used by sendmsg mode
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	public void setMessageIsOdered(final boolean isOrdered) {
		messageIsOrdered = isOrdered;
	}

	/**
	 * default timeToLive value used by sendmsg mode
	 * 
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	public void setMessageTimeTolLive(final int timeToLive) {
		messageTimeTolive = timeToLive;
	}

	/**
	 * @see #setOption0(int, Class, Object)
	 */
	public <T> void setOption( //
			final OptionUDT<T> option, //
			final T value //
	) throws ExceptionUDT {

		if (option == null || value == null) {
			throw new IllegalArgumentException(
					"option == null || value == null");
		}

		setOption0(option.code(), option.type(), value);

	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/opt.htm">UDT::setsockopt()</a>
	 */
	protected native void setOption0( //
			final int code, //
			final Class<?> klaz, //
			final Object value //
	) throws ExceptionUDT;

	/**
	 * Set maximum receive buffer size. Affects both protocol-level (UDT) and
	 * kernel-level(UDP) settings.
	 */
	public void setReceiveBufferSize(final int size) throws ExceptionUDT {
		setOption(OptionUDT.Protocol_Receive_Buffer_Size, size);
		setOption(OptionUDT.System_Receive_Buffer_Size, size);
	}

	public void setRendezvous(final boolean isOn) throws ExceptionUDT {
		setOption(OptionUDT.Is_Randezvous_Connect_Enabled, isOn);
	}

	public void setReuseAddress(final boolean on) throws ExceptionUDT {
		setOption(OptionUDT.Is_Address_Reuse_Enabled, on);
	}

	/**
	 * Set maximum send buffer size. Affects both protocol-level (UDT) and
	 * kernel-level(UDP) settings
	 * 
	 * @see java.net.Socket#setSendBufferSize(int)
	 */
	public void setSendBufferSize(final int size) throws ExceptionUDT {
		setOption(OptionUDT.Protocol_Send_Buffer_Size, size);
		setOption(OptionUDT.System_Send_Buffer_Size, size);
	}

	public void setSoLinger(final boolean on, final int linger)
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
	public void setSoTimeout(/* non-final */int millisTimeout)
			throws ExceptionUDT {
		if (millisTimeout < 0) {
			throw new IllegalArgumentException("timeout < 0");
		}
		if (millisTimeout == 0) {
			// UDT uses different value for "infinite"
			millisTimeout = TIMEOUT_INFINITE;
		}
		setOption(OptionUDT.Send_Timeout, millisTimeout);
		setOption(OptionUDT.Receive_Timeout, millisTimeout);
	}

	/**
	 * returns native status of underlying native UDT socket
	 */
	public StatusUDT status() {
		return StatusUDT.from(getStatus0());
	}

	//
	@Override
	public String toString() {

		return String.format( //
				"[id: 0x%08x] %s %s bind=%s:%s peer=%s:%s", //
				socketID, //
				type, //
				status(), //
				getLocalInetAddress(), //
				getLocalInetPort(), //
				getRemoteInetAddress(), //
				getRemoteInetPort() //
				);

	}

	/**
	 * Show current monitor status.
	 */
	public String toStringMonitor() {

		try {
			updateMonitor(false);
		} catch (final Exception e) {
			return "failed to update monitor : " + e.getMessage();
		}

		final StringBuilder text = new StringBuilder(1024);

		monitor.appendSnapshot(text);

		return text.toString();

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
	 * message/stream socket type; read by JNI
	 */
	public TypeUDT type() {
		return type;
	}

	/**
	 * Load updated statistics values into {@link #monitor} object. Must call
	 * this methos only on connected socket.
	 * 
	 * @param makeClear
	 *            true : reset all statistics with this call; false : keep
	 *            collecting statistics, load updated values.
	 * @see #updateMonitor0(boolean)
	 */
	public void updateMonitor(final boolean makeClear) throws ExceptionUDT {
		updateMonitor0(makeClear);
	}

	/**
	 * @see <a
	 *      href="http://udt.sourceforge.net/udt4/doc/trace.htm">UDT::perfmon</a>
	 */
	protected native void updateMonitor0(final boolean makeClear)
			throws ExceptionUDT;

}
