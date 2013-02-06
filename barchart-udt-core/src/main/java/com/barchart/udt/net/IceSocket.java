/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;

/**
 * compatibility verification interface
 */
public interface IceSocket {

	/**
	 * Connects this socket to the server.
	 * 
	 * @param endpoint
	 *            the <code>SocketAddress</code>
	 * @throws IOException
	 *             if an error occurs during the connection
	 * @throws java.nio.channels.IllegalBlockingModeException
	 *             if this socket has an associated channel, and the channel is
	 *             in non-blocking mode
	 * @throws IllegalArgumentException
	 *             if endpoint is null or is a SocketAddress subclass not
	 *             supported by this socket
	 * @since 1.4 spec JSR-51
	 */
	void connect(SocketAddress endpoint) throws IOException;

	/**
	 * Connects this socket to the server with a specified timeout value. A
	 * timeout of zero is interpreted as an infinite timeout. The connection
	 * will then block until established or an error occurs.
	 * 
	 * @param endpoint
	 *            the <code>SocketAddress</code>
	 * @param timeout
	 *            the timeout value to be used in milliseconds.
	 * @throws IOException
	 *             if an error occurs during the connection
	 * @throws SocketTimeoutException
	 *             if timeout expires before connecting
	 * @throws java.nio.channels.IllegalBlockingModeException
	 *             if this socket has an associated channel, and the channel is
	 *             in non-blocking mode
	 * @throws IllegalArgumentException
	 *             if endpoint is null or is a SocketAddress subclass not
	 *             supported by this socket
	 * @since 1.4 spec JSR-51
	 */
	void connect(SocketAddress endpoint, int timeout) throws IOException;

	/**
	 * Binds the socket to a local address.
	 * <P>
	 * If the address is <code>null</code>, then the system will pick up an
	 * ephemeral port and a valid local address to bind the socket.
	 * 
	 * @param bindpoint
	 *            the <code>SocketAddress</code> to bind to
	 * @throws IOException
	 *             if the bind operation fails, or if the socket is already
	 *             bound.
	 * @throws IllegalArgumentException
	 *             if bindpoint is a SocketAddress subclass not supported by
	 *             this socket
	 * 
	 * @since 1.4
	 * @see #isBound
	 */
	void bind(SocketAddress bindpoint) throws IOException;

	/**
	 * Returns the address to which the socket is connected.
	 * 
	 * @return the remote IP address to which this socket is connected, or
	 *         <code>null</code> if the socket is not connected.
	 */
	InetAddress getInetAddress();

	/**
	 * Gets the local address to which the socket is bound.
	 * 
	 * @return the local address to which the socket is bound or
	 *         <code>InetAddress.anyLocalAddress()</code> if the socket is not
	 *         bound yet.
	 * @since JDK1.1
	 */
	InetAddress getLocalAddress();

	/**
	 * Returns the remote port to which this socket is connected.
	 * 
	 * @return the remote port number to which this socket is connected, or 0 if
	 *         the socket is not connected yet.
	 */
	int getPort();

	/**
	 * Returns the local port to which this socket is bound.
	 * 
	 * @return the local port number to which this socket is bound or -1 if the
	 *         socket is not bound yet.
	 */
	int getLocalPort();

	/**
	 * Returns the address of the endpoint this socket is connected to, or
	 * <code>null</code> if it is unconnected.
	 * 
	 * @return a <code>SocketAddress</code> reprensenting the remote endpoint of
	 *         this socket, or <code>null</code> if it is not connected yet.
	 * @see #getInetAddress()
	 * @see #getPort()
	 * @see #connect(SocketAddress, int)
	 * @see #connect(SocketAddress)
	 * @since 1.4
	 */
	SocketAddress getRemoteSocketAddress();

	/**
	 * Returns the address of the endpoint this socket is bound to, or
	 * <code>null</code> if it is not bound yet.
	 * 
	 * @return a <code>SocketAddress</code> representing the local endpoint of
	 *         this socket, or <code>null</code> if it is not bound yet.
	 * @see #getLocalAddress()
	 * @see #getLocalPort()
	 * @see #bind(SocketAddress)
	 * @since 1.4
	 */

	SocketAddress getLocalSocketAddress();

	/**
	 * Returns the unique {@link java.nio.channels.SocketChannel SocketChannel}
	 * object associated with this socket, if any.
	 * 
	 * <p>
	 * A socket will have a channel if, and only if, the channel itself was
	 * created via the {@link java.nio.channels.SocketChannel#open
	 * SocketChannel.open} or
	 * {@link java.nio.channels.ServerSocketChannel#accept
	 * ServerSocketChannel.accept} methods.
	 * 
	 * @return the socket channel associated with this socket, or <tt>null</tt>
	 *         if this socket was not created for a channel
	 * 
	 * @since 1.4 spec JSR-51
	 */
	SocketChannel getChannel();

	/**
	 * Returns an input stream for this socket.
	 * 
	 * <p>
	 * If this socket has an associated channel then the resulting input stream
	 * delegates all of its operations to the channel. If the channel is in
	 * non-blocking mode then the input stream's <tt>read</tt> operations will
	 * throw an {@link java.nio.channels.IllegalBlockingModeException}.
	 * 
	 * <p>
	 * Under abnormal conditions the underlying connection may be broken by the
	 * remote host or the network software (for example a connection reset in
	 * the case of TCP connections). When a broken connection is detected by the
	 * network software the following applies to the returned input stream :-
	 * 
	 * <ul>
	 * 
	 * <li>
	 * <p>
	 * The network software may discard bytes that are buffered by the socket.
	 * Bytes that aren't discarded by the network software can be read using
	 * {@link java.io.InputStream#read read}.
	 * 
	 * <li>
	 * <p>
	 * If there are no bytes buffered on the socket, or all buffered bytes have
	 * been consumed by {@link java.io.InputStream#read read}, then all
	 * subsequent calls to {@link java.io.InputStream#read read} will throw an
	 * {@link java.io.IOException IOException}.
	 * 
	 * <li>
	 * <p>
	 * If there are no bytes buffered on the socket, and the socket has not been
	 * closed using {@link #close close}, then
	 * {@link java.io.InputStream#available available} will return
	 * <code>0</code>.
	 * 
	 * </ul>
	 * 
	 * <p>
	 * Closing the returned {@link java.io.InputStream InputStream} will close
	 * the associated socket.
	 * 
	 * @return an input stream for reading bytes from this socket.
	 * @exception IOException
	 *                if an I/O error occurs when creating the input stream, the
	 *                socket is closed, the socket is not connected, or the
	 *                socket input has been shutdown using
	 *                {@link #shutdownInput()}
	 * 
	 *                revised 1.4 spec JSR-51
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns an output stream for this socket.
	 * 
	 * <p>
	 * If this socket has an associated channel then the resulting output stream
	 * delegates all of its operations to the channel. If the channel is in
	 * non-blocking mode then the output stream's <tt>write</tt> operations will
	 * throw an {@link java.nio.channels.IllegalBlockingModeException}.
	 * 
	 * <p>
	 * Closing the returned {@link java.io.OutputStream OutputStream} will close
	 * the associated socket.
	 * 
	 * @return an output stream for writing bytes to this socket.
	 * @exception IOException
	 *                if an I/O error occurs when creating the output stream or
	 *                if the socket is not connected. revised 1.4 spec JSR-51
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm).
	 * 
	 * @param on
	 *            <code>true</code> to enable TCP_NODELAY, <code>false</code> to
	 *            disable.
	 * 
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * 
	 * @since JDK1.1
	 * 
	 * @see #getTcpNoDelay()
	 */
	void setTcpNoDelay(boolean on) throws SocketException;

	/**
	 * Tests if TCP_NODELAY is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not TCP_NODELAY is
	 *         enabled.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since JDK1.1
	 * @see #setTcpNoDelay(boolean)
	 */
	boolean getTcpNoDelay() throws SocketException;

	/**
	 * Enable/disable SO_LINGER with the specified linger time in seconds. The
	 * maximum timeout value is platform specific.
	 * 
	 * The setting only affects socket close.
	 * 
	 * @param on
	 *            whether or not to linger on.
	 * @param linger
	 *            how long to linger for, if on is true.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @exception IllegalArgumentException
	 *                if the linger value is negative.
	 * @since JDK1.1
	 * @see #getSoLinger()
	 */
	void setSoLinger(boolean on, int linger) throws SocketException;

	/**
	 * Returns setting for SO_LINGER. -1 returns implies that the option is
	 * disabled.
	 * 
	 * The setting only affects socket close.
	 * 
	 * @return the setting for SO_LINGER.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since JDK1.1
	 * @see #setSoLinger(boolean, int)
	 */
	int getSoLinger() throws SocketException;

	/**
	 * Send one byte of urgent data on the socket. The byte to be sent is the
	 * lowest eight bits of the data parameter. The urgent byte is sent after
	 * any preceding writes to the socket OutputStream and before any future
	 * writes to the OutputStream.
	 * 
	 * @param data
	 *            The byte of data to send
	 * @exception IOException
	 *                if there is an error sending the data.
	 * @since 1.4
	 */
	void sendUrgentData(int data) throws IOException;

	/**
	 * Enable/disable OOBINLINE (receipt of TCP urgent data)
	 * 
	 * By default, this option is disabled and TCP urgent data received on a
	 * socket is silently discarded. If the user wishes to receive urgent data,
	 * then this option must be enabled. When enabled, urgent data is received
	 * inline with normal data.
	 * <p>
	 * Note, only limited support is provided for handling incoming urgent data.
	 * In particular, no notification of incoming urgent data is provided and
	 * there is no capability to distinguish between normal data and urgent data
	 * unless provided by a higher level protocol.
	 * 
	 * @param on
	 *            <code>true</code> to enable OOBINLINE, <code>false</code> to
	 *            disable.
	 * 
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * 
	 * @since 1.4
	 * 
	 * @see #getOOBInline()
	 */
	void setOOBInline(boolean on) throws SocketException;

	/**
	 * Tests if OOBINLINE is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not OOBINLINE is
	 *         enabled.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since 1.4
	 * @see #setOOBInline(boolean)
	 */
	boolean getOOBInline() throws SocketException;

	/**
	 * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds.
	 * With this option set to a non-zero timeout, a read() call on the
	 * InputStream associated with this Socket will block for only this amount
	 * of time. If the timeout expires, a <B>java.net.SocketTimeoutException</B>
	 * is raised, though the Socket is still valid. The option <B>must</B> be
	 * enabled prior to entering the blocking operation to have effect. The
	 * timeout must be > 0. A timeout of zero is interpreted as an infinite
	 * timeout.
	 * 
	 * @param timeout
	 *            the specified timeout, in milliseconds.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since JDK 1.1
	 * @see #getSoTimeout()
	 */
	void setSoTimeout(int timeout) throws SocketException;

	/**
	 * Returns setting for SO_TIMEOUT. 0 returns implies that the option is
	 * disabled (i.e., timeout of infinity).
	 * 
	 * @return the setting for SO_TIMEOUT
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since JDK1.1
	 * @see #setSoTimeout(int)
	 */
	int getSoTimeout() throws SocketException;

	/**
	 * Sets the SO_SNDBUF option to the specified value for this <tt>Socket</tt>
	 * . The SO_SNDBUF option is used by the platform's networking code as a
	 * hint for the size to set the underlying network I/O buffers.
	 * 
	 * <p>
	 * Because SO_SNDBUF is a hint, applications that want to verify what size
	 * the buffers were set to should call {@link #getSendBufferSize()}.
	 * 
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * 
	 * @param size
	 *            the size to which to set the send buffer size. This value must
	 *            be greater than 0.
	 * 
	 * @exception IllegalArgumentException
	 *                if the value is 0 or is negative.
	 * 
	 * @see #getSendBufferSize()
	 * @since 1.2
	 */
	void setSendBufferSize(int size) throws SocketException;

	/**
	 * Get value of the SO_SNDBUF option for this <tt>Socket</tt>, that is the
	 * buffer size used by the platform for output on this <tt>Socket</tt>.
	 * 
	 * @return the value of the SO_SNDBUF option for this <tt>Socket</tt>.
	 * 
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * 
	 * @see #setSendBufferSize(int)
	 * @since 1.2
	 */
	int getSendBufferSize() throws SocketException;

	/**
	 * Sets the SO_RCVBUF option to the specified value for this <tt>Socket</tt>
	 * . The SO_RCVBUF option is used by the platform's networking code as a
	 * hint for the size to set the underlying network I/O buffers.
	 * 
	 * <p>
	 * Increasing the receive buffer size can increase the performance of
	 * network I/O for high-volume connection, while decreasing it can help
	 * reduce the backlog of incoming data.
	 * 
	 * <p>
	 * Because SO_RCVBUF is a hint, applications that want to verify what size
	 * the buffers were set to should call {@link #getReceiveBufferSize()}.
	 * 
	 * <p>
	 * The value of SO_RCVBUF is also used to set the TCP receive window that is
	 * advertized to the remote peer. Generally, the window size can be modified
	 * at any time when a socket is connected. However, if a receive window
	 * larger than 64K is required then this must be requested <B>before</B> the
	 * socket is connected to the remote peer. There are two cases to be aware
	 * of:
	 * <p>
	 * <ol>
	 * <li>For sockets accepted from a ServerSocket, this must be done by
	 * calling {@link ServerSocket#setReceiveBufferSize(int)} before the
	 * ServerSocket is bound to a local address.
	 * <p></li>
	 * <li>For client sockets, setReceiveBufferSize() must be called before
	 * connecting the socket to its remote peer.
	 * <p></li>
	 * </ol>
	 * 
	 * @param size
	 *            the size to which to set the receive buffer size. This value
	 *            must be greater than 0.
	 * 
	 * @exception IllegalArgumentException
	 *                if the value is 0 or is negative.
	 * 
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * 
	 * @see #getReceiveBufferSize()
	 * @see ServerSocket#setReceiveBufferSize(int)
	 * @since 1.2
	 */
	void setReceiveBufferSize(int size) throws SocketException;

	/**
	 * Gets the value of the SO_RCVBUF option for this <tt>Socket</tt>, that is
	 * the buffer size used by the platform for input on this <tt>Socket</tt>.
	 * 
	 * @return the value of the SO_RCVBUF option for this <tt>Socket</tt>.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @see #setReceiveBufferSize(int)
	 * @since 1.2
	 */
	int getReceiveBufferSize() throws SocketException;

	/**
	 * Enable/disable SO_KEEPALIVE.
	 * 
	 * @param on
	 *            whether or not to have socket keep alive turned on.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since 1.3
	 * @see #getKeepAlive()
	 */
	void setKeepAlive(boolean on) throws SocketException;

	/**
	 * Tests if SO_KEEPALIVE is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not SO_KEEPALIVE is
	 *         enabled.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as a
	 *                TCP error.
	 * @since 1.3
	 * @see #setKeepAlive(boolean)
	 */
	boolean getKeepAlive() throws SocketException;

	/**
	 * Sets traffic class or type-of-service octet in the IP header for packets
	 * sent from this Socket. As the underlying network implementation may
	 * ignore this value applications should consider it a hint.
	 * 
	 * <P>
	 * The tc <B>must</B> be in the range <code> 0 <= tc <=
	 * 255</code> or an IllegalArgumentException will be thrown.
	 * <p>
	 * Notes:
	 * <p>
	 * For Internet Protocol v4 the value consists of an octet with precedence
	 * and TOS fields as detailed in RFC 1349. The TOS field is bitset created
	 * by bitwise-or'ing values such the following :-
	 * <p>
	 * <UL>
	 * <LI><CODE>IPTOS_LOWCOST (0x02)</CODE></LI>
	 * <LI><CODE>IPTOS_RELIABILITY (0x04)</CODE></LI>
	 * <LI><CODE>IPTOS_THROUGHPUT (0x08)</CODE></LI>
	 * <LI><CODE>IPTOS_LOWDELAY (0x10)</CODE></LI>
	 * </UL>
	 * The last low order bit is always ignored as this corresponds to the MBZ
	 * (must be zero) bit.
	 * <p>
	 * Setting bits in the precedence field may result in a SocketException
	 * indicating that the operation is not permitted.
	 * <p>
	 * As RFC 1122 section 4.2.4.2 indicates, a compliant TCP implementation
	 * should, but is not required to, let application change the TOS field
	 * during the lifetime of a connection. So whether the type-of-service field
	 * can be changed after the TCP connection has been established depends on
	 * the implementation in the underlying platform. Applications should not
	 * assume that they can change the TOS field after the connection.
	 * <p>
	 * For Internet Protocol v6 <code>tc</code> is the value that would be
	 * placed into the sin6_flowinfo field of the IP header.
	 * 
	 * @param tc
	 *            an <code>int</code> value for the bitset.
	 * @throws SocketException
	 *             if there is an error setting the traffic class or
	 *             type-of-service
	 * @since 1.4
	 * @see #getTrafficClass
	 */
	void setTrafficClass(int tc) throws SocketException;

	/**
	 * Gets traffic class or type-of-service in the IP header for packets sent
	 * from this Socket
	 * <p>
	 * As the underlying network implementation may ignore the traffic class or
	 * type-of-service set using {@link #setTrafficClass(int)} this method may
	 * return a different value than was previously set using the
	 * {@link #setTrafficClass(int)} method on this Socket.
	 * 
	 * @return the traffic class or type-of-service already set
	 * @throws SocketException
	 *             if there is an error obtaining the traffic class or
	 *             type-of-service value.
	 * @since 1.4
	 * @see #setTrafficClass(int)
	 */
	int getTrafficClass() throws SocketException;

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
	 * When a <tt>Socket</tt> is created the initial setting of
	 * <tt>SO_REUSEADDR</tt> is disabled.
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
	 * @see #isClosed()
	 * @see #isBound()
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
	 * Closes this socket.
	 * <p>
	 * Any thread currently blocked in an I/O operation upon this socket will
	 * throw a {@link SocketException}.
	 * <p>
	 * Once a socket has been closed, it is not available for further networking
	 * use (i.e. can't be reconnected or rebound). A new socket needs to be
	 * created.
	 * 
	 * <p>
	 * Closing this socket will also close the socket's
	 * {@link java.io.InputStream InputStream} and {@link java.io.OutputStream
	 * OutputStream}.
	 * 
	 * <p>
	 * If this socket has an associated channel then the channel is closed as
	 * well.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs when closing this socket. revised
	 *                1.4 spec JSR-51
	 * @see #isClosed
	 */
	void close() throws IOException;

	/**
	 * Places the input stream for this socket at "end of stream". Any data sent
	 * to the input stream side of the socket is acknowledged and then silently
	 * discarded.
	 * <p>
	 * If you read from a socket input stream after invoking shutdownInput() on
	 * the socket, the stream will return EOF.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs when shutting down this socket.
	 * 
	 * @since 1.3
	 * @see java.net.Socket#shutdownOutput()
	 * @see java.net.Socket#close()
	 * @see java.net.Socket#setSoLinger(boolean, int)
	 * @see #isInputShutdown
	 */
	void shutdownInput() throws IOException;

	/**
	 * Disables the output stream for this socket. For a TCP socket, any
	 * previously written data will be sent followed by TCP's normal connection
	 * termination sequence.
	 * 
	 * If you write to a socket output stream after invoking shutdownOutput() on
	 * the socket, the stream will throw an IOException.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs when shutting down this socket.
	 * 
	 * @since 1.3
	 * @see java.net.Socket#shutdownInput()
	 * @see java.net.Socket#close()
	 * @see java.net.Socket#setSoLinger(boolean, int)
	 * @see #isOutputShutdown
	 */
	void shutdownOutput() throws IOException;

	/**
	 * Converts this socket to a <code>String</code>.
	 * 
	 * @return a string representation of this socket.
	 */
	@Override
	String toString();

	/**
	 * Returns the connection state of the socket.
	 * 
	 * @return true if the socket successfuly connected to a server
	 * @since 1.4
	 */
	boolean isConnected();

	/**
	 * Returns the binding state of the socket.
	 * 
	 * @return true if the socket successfuly bound to an address
	 * @since 1.4
	 * @see #bind
	 */
	boolean isBound();

	/**
	 * Returns the closed state of the socket.
	 * 
	 * @return true if the socket has been closed
	 * @since 1.4
	 * @see #close
	 */
	boolean isClosed();

	/**
	 * Returns whether the read-half of the socket connection is closed.
	 * 
	 * @return true if the input of the socket has been shutdown
	 * @since 1.4
	 * @see #shutdownInput
	 */
	boolean isInputShutdown();

	/**
	 * Returns whether the write-half of the socket connection is closed.
	 * 
	 * @return true if the output of the socket has been shutdown
	 * @since 1.4
	 * @see #shutdownOutput
	 */
	boolean isOutputShutdown();

	/**
	 * Sets performance preferences for this socket.
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
	 * values indicating stronger preferences. Negative values represent a lower
	 * priority than positive values. If the application prefers short
	 * connection time over both low latency and high bandwidth, for example,
	 * then it could invoke this method with the values <tt>(1, 0, 0)</tt>. If
	 * the application prefers high bandwidth above low latency, and low latency
	 * above short connection time, then it could invoke this method with the
	 * values <tt>(0, 1, 2)</tt>.
	 * 
	 * <p>
	 * Invoking this method after this socket has been connected will have no
	 * effect.
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