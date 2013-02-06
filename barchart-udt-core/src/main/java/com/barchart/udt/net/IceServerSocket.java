/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;

/**
 * compatibility verification interface
 */
public interface IceServerSocket {

	/**
	 * 
	 * Binds the <code>ServerSocket</code> to a specific address (IP address and
	 * port number).
	 * <p>
	 * If the address is <code>null</code>, then the system will pick up an
	 * ephemeral port and a valid local address to bind the socket.
	 * <p>
	 * 
	 * @param endpoint
	 *            The IP address & port number to bind to.
	 * @throws IOException
	 *             if the bind operation fails, or if the socket is already
	 *             bound.
	 * @throws SecurityException
	 *             if a <code>SecurityManager</code> is present and its
	 *             <code>checkListen</code> method doesn't allow the operation.
	 * @throws IllegalArgumentException
	 *             if endpoint is a SocketAddress subclass not supported by this
	 *             socket
	 * @since 1.4
	 */
	void bind(SocketAddress endpoint) throws IOException;

	/**
	 * 
	 * Binds the <code>ServerSocket</code> to a specific address (IP address and
	 * port number).
	 * <p>
	 * If the address is <code>null</code>, then the system will pick up an
	 * ephemeral port and a valid local address to bind the socket.
	 * <P>
	 * The <code>backlog</code> argument must be a positive value greater than
	 * 0. If the value passed if equal or less than 0, then the default value
	 * will be assumed.
	 * 
	 * @param endpoint
	 *            The IP address & port number to bind to.
	 * @param backlog
	 *            The listen backlog length.
	 * @throws IOException
	 *             if the bind operation fails, or if the socket is already
	 *             bound.
	 * @throws SecurityException
	 *             if a <code>SecurityManager</code> is present and its
	 *             <code>checkListen</code> method doesn't allow the operation.
	 * @throws IllegalArgumentException
	 *             if endpoint is a SocketAddress subclass not supported by this
	 *             socket
	 * @since 1.4
	 */
	void bind(SocketAddress endpoint, int backlog) throws IOException;

	/**
	 * Returns the local address of this server socket.
	 * 
	 * @return the address to which this socket is bound, or <code>null</code>
	 *         if the socket is unbound.
	 */
	InetAddress getInetAddress();

	/**
	 * Returns the port on which this socket is listening.
	 * 
	 * @return the port number to which this socket is listening or -1 if the
	 *         socket is not bound yet.
	 */
	int getLocalPort();

	/**
	 * Returns the address of the endpoint this socket is bound to, or
	 * <code>null</code> if it is not bound yet.
	 * 
	 * @return a <code>SocketAddress</code> representing the local endpoint of
	 *         this socket, or <code>null</code> if it is not bound yet.
	 * @see #getInetAddress()
	 * @see #getLocalPort()
	 * @see #bind(SocketAddress)
	 * @since 1.4
	 */

	SocketAddress getLocalSocketAddress();

	/**
	 * Listens for a connection to be made to this socket and accepts it. The
	 * method blocks until a connection is made.
	 * 
	 * <p>
	 * A new Socket <code>s</code> is created and, if there is a security
	 * manager, the security manager's <code>checkAccept</code> method is called
	 * with <code>s.getInetAddress().getHostAddress()</code> and
	 * <code>s.getPort()</code> as its arguments to ensure the operation is
	 * allowed. This could result in a SecurityException.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs when waiting for a connection.
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkAccept</code> method doesn't allow the
	 *                operation.
	 * @exception SocketTimeoutException
	 *                if a timeout was previously set with setSoTimeout and the
	 *                timeout has been reached.
	 * @exception java.nio.channels.IllegalBlockingModeException
	 *                if this socket has an associated channel, the channel is
	 *                in non-blocking mode, and there is no connection ready to
	 *                be accepted
	 * 
	 * @return the new Socket
	 * @see SecurityManager#checkAccept revised 1.4 spec JSR-51
	 */
	Socket accept() throws IOException;

	/**
	 * Closes this socket.
	 * 
	 * Any thread currently blocked in {@link #accept()} will throw a
	 * {@link SocketException}.
	 * 
	 * <p>
	 * If this socket has an associated channel then the channel is closed as
	 * well.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs when closing the socket. revised
	 *                1.4 spec JSR-51
	 */
	void close() throws IOException;

	/**
	 * Returns the unique {@link java.nio.channels.ServerSocketChannel} object
	 * associated with this socket, if any.
	 * 
	 * <p>
	 * A server socket will have a channel if, and only if, the channel itself
	 * was created via the {@link java.nio.channels.ServerSocketChannel#open
	 * ServerSocketChannel.open} method.
	 * 
	 * @return the server-socket channel associated with this socket, or
	 *         <tt>null</tt> if this socket was not created for a channel
	 * 
	 * @since 1.4 spec JSR-51
	 */
	ServerSocketChannel getChannel();

	/**
	 * Returns the binding state of the ServerSocket.
	 * 
	 * @return true if the ServerSocket succesfuly bound to an address
	 * @since 1.4
	 */
	boolean isBound();

	/**
	 * Returns the closed state of the ServerSocket.
	 * 
	 * @return true if the socket has been closed
	 * @since 1.4
	 */
	boolean isClosed();

	/**
	 * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds.
	 * With this option set to a non-zero timeout, a call to accept() for this
	 * ServerSocket will block for only this amount of time. If the timeout
	 * expires, a <B>java.net.SocketTimeoutException</B> is raised, though the
	 * ServerSocket is still valid. The option <B>must</B> be enabled prior to
	 * entering the blocking operation to have effect. The timeout must be > 0.
	 * A timeout of zero is interpreted as an infinite timeout.
	 * 
	 * @param timeout
	 *            the specified timeout, in milliseconds
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since JDK1.1
	 * @see #getSoTimeout()
	 */
	void setSoTimeout(int timeout) throws SocketException;

	/**
	 * Retrieve setting for SO_TIMEOUT. 0 returns implies that the option is
	 * disabled (i.e., timeout of infinity).
	 * 
	 * @return the SO_TIMEOUT value
	 * @exception IOException
	 *                if an I/O error occurs
	 * @since JDK1.1
	 * @see #setSoTimeout(int)
	 */
	int getSoTimeout() throws IOException;

	/**
	 * Enable/disable the SO_REUSEADDR socket option.
	 * <p>
	 * When a TCP connection is closed the connection may remain in a timeout
	 * state for a period of time after the connection is closed (typically
	 * known as the <tt>TIME_WAIT</tt> state or <tt>2MSL</tt> wait state). For
	 * applications using a well known socket address or port it may not be
	 * possible to bind a socket to the required <tt>SocketAddress</tt> if there
	 * is a connection in the timeout state involving the socket address or
	 * port.
	 * <p>
	 * Enabling <tt>SO_REUSEADDR</tt> prior to binding the socket using
	 * {@link #bind(SocketAddress)} allows the socket to be bound even though a
	 * previous connection is in a timeout state.
	 * <p>
	 * When a <tt>ServerSocket</tt> is created the initial setting of
	 * <tt>SO_REUSEADDR</tt> is not defined. Applications can use
	 * {@link #getReuseAddress()} to determine the initial setting of
	 * <tt>SO_REUSEADDR</tt>.
	 * <p>
	 * The behaviour when <tt>SO_REUSEADDR</tt> is enabled or disabled after a
	 * socket is bound (See {@link #isBound()}) is not defined.
	 * 
	 * @param on
	 *            whether to enable or disable the socket option
	 * @exception SocketException
	 *                if an error occurs enabling or disabling the
	 *                <tt>SO_RESUEADDR</tt> socket option, or the socket is
	 *                closed.
	 * @since 1.4
	 * @see #getReuseAddress()
	 * @see #bind(SocketAddress)
	 * @see #isBound()
	 * @see #isClosed()
	 */
	void setReuseAddress(boolean on) throws SocketException;

	/**
	 * Tests if SO_REUSEADDR is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not SO_REUSEADDR is
	 *         enabled.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since 1.4
	 * @see #setReuseAddress(boolean)
	 */
	boolean getReuseAddress() throws SocketException;

	/**
	 * Returns the implementation address and implementation port of this socket
	 * as a <code>String</code>.
	 * 
	 * @return a string representation of this socket.
	 */
	@Override
	String toString();

	/**
	 * Sets a default proposed value for the SO_RCVBUF option for sockets
	 * accepted from this <tt>ServerSocket</tt>. The value actually set in the
	 * accepted socket must be determined by calling
	 * {@link Socket#getReceiveBufferSize()} after the socket is returned by
	 * {@link #accept()}.
	 * <p>
	 * The value of SO_RCVBUF is used both to set the size of the internal
	 * socket receive buffer, and to set the size of the TCP receive window that
	 * is advertized to the remote peer.
	 * <p>
	 * It is possible to change the value subsequently, by calling
	 * {@link Socket#setReceiveBufferSize(int)}. However, if the application
	 * wishes to allow a receive window larger than 64K bytes, as defined by
	 * RFC1323 then the proposed value must be set in the ServerSocket
	 * <B>before</B> it is bound to a local address. This implies, that the
	 * ServerSocket must be created with the no-argument constructor, then
	 * setReceiveBufferSize() must be called and lastly the ServerSocket is
	 * bound to an address by calling bind().
	 * <p>
	 * Failure to do this will not cause an error, and the buffer size may be
	 * set to the requested value but the TCP receive window in sockets accepted
	 * from this ServerSocket will be no larger than 64K bytes.
	 * 
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * 
	 * @param size
	 *            the size to which to set the receive buffer size. This value
	 *            must be greater than 0.
	 * 
	 * @exception IllegalArgumentException
	 *                if the value is 0 or is negative.
	 * 
	 * @since 1.4
	 * @see #getReceiveBufferSize
	 */
	void setReceiveBufferSize(int size) throws SocketException;

	/**
	 * Gets the value of the SO_RCVBUF option for this <tt>ServerSocket</tt>,
	 * that is the proposed buffer size that will be used for Sockets accepted
	 * from this <tt>ServerSocket</tt>.
	 * 
	 * <p>
	 * Note, the value actually set in the accepted socket is determined by
	 * calling {@link Socket#getReceiveBufferSize()}.
	 * 
	 * @return the value of the SO_RCVBUF option for this <tt>Socket</tt>.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @see #setReceiveBufferSize(int)
	 * @since 1.4
	 */
	int getReceiveBufferSize() throws SocketException;

	/**
	 * Sets performance preferences for this ServerSocket.
	 * 
	 * <p>
	 * Sockets use the TCP/IP protocol by default. Some implementations may
	 * offer alternative protocols which have different performance
	 * characteristics than TCP/IP. This method allows the application to
	 * express its own preferences as to how these tradeoffs should be made when
	 * the implementation chooses from the available protocols.
	 * 
	 * <p>
	 * Performance preferences are described by three integers whose values
	 * indicate the relative importance of short connection time, low latency,
	 * and high bandwidth. The absolute values of the integers are irrelevant;
	 * in order to choose a protocol the values are simply compared, with larger
	 * values indicating stronger preferences. If the application prefers short
	 * connection time over both low latency and high bandwidth, for example,
	 * then it could invoke this method with the values <tt>(1, 0, 0)</tt>. If
	 * the application prefers high bandwidth above low latency, and low latency
	 * above short connection time, then it could invoke this method with the
	 * values <tt>(0, 1, 2)</tt>.
	 * 
	 * <p>
	 * Invoking this method after this socket has been bound will have no
	 * effect. This implies that in order to use this capability requires the
	 * socket to be created with the no-argument constructor.
	 * 
	 * @param connectionTime
	 *            An <tt>int</tt> expressing the relative importance of a short
	 *            connection time
	 * 
	 * @param latency
	 *            An <tt>int</tt> expressing the relative importance of low
	 *            latency
	 * 
	 * @param bandwidth
	 *            An <tt>int</tt> expressing the relative importance of high
	 *            bandwidth
	 * 
	 * @since 1.5
	 */
	void setPerformancePreferences(int connectionTime, int latency,
			int bandwidth);

}