/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;

/**
 * compatibility verification interface
 */
public interface IceDatagramSocket {

	/**
	 * Binds this DatagramSocket to a specific address & port.
	 * <p>
	 * If the address is <code>null</code>, then the system will pick up an
	 * ephemeral port and a valid local address to bind the socket.
	 * <p>
	 * 
	 * @param addr
	 *            The address & port to bind to.
	 * @throws SocketException
	 *             if any error happens during the bind, or if the socket is
	 *             already bound.
	 * @throws SecurityException
	 *             if a security manager exists and its <code>checkListen</code>
	 *             method doesn't allow the operation.
	 * @throws IllegalArgumentException
	 *             if addr is a SocketAddress subclass not supported by this
	 *             socket.
	 * @since 1.4
	 */
	void bind(SocketAddress addr) throws SocketException;

	/**
	 * Connects the socket to a remote address for this socket. When a socket is
	 * connected to a remote address, packets may only be sent to or received
	 * from that address. By default a datagram socket is not connected.
	 * 
	 * <p>
	 * If the remote destination to which the socket is connected does not
	 * exist, or is otherwise unreachable, and if an ICMP destination
	 * unreachable packet has been received for that address, then a subsequent
	 * call to send or receive may throw a PortUnreachableException. Note, there
	 * is no guarantee that the exception will be thrown.
	 * 
	 * <p>
	 * A caller's permission to send and receive datagrams to a given host and
	 * port are checked at connect time. When a socket is connected, receive and
	 * send <b>will not perform any security checks</b> on incoming and outgoing
	 * packets, other than matching the packet's and the socket's address and
	 * port. On a send operation, if the packet's address is set and the
	 * packet's address and the socket's address do not match, an
	 * IllegalArgumentException will be thrown. A socket connected to a
	 * multicast address may only be used to send packets.
	 * 
	 * @param address
	 *            the remote address for the socket
	 * 
	 * @param port
	 *            the remote port for the socket.
	 * 
	 * @exception IllegalArgumentException
	 *                if the address is null, or the port is out of range.
	 * 
	 * @exception SecurityException
	 *                if the caller is not allowed to send datagrams to and
	 *                receive datagrams from the address and port.
	 * 
	 * @see #disconnect
	 * @see #send
	 * @see #receive
	 */
	void connect(InetAddress address, int port);

	/**
	 * Connects this socket to a remote socket address (IP address + port
	 * number).
	 * <p>
	 * 
	 * @param addr
	 *            The remote address.
	 * @throws SocketException
	 *             if the connect fails
	 * @throws IllegalArgumentException
	 *             if addr is null or addr is a SocketAddress subclass not
	 *             supported by this socket
	 * @since 1.4
	 * @see #connect
	 */
	void connect(SocketAddress addr) throws SocketException;

	/**
	 * Disconnects the socket. This does nothing if the socket is not connected.
	 * 
	 * @see #connect
	 */
	void disconnect();

	/**
	 * Returns the binding state of the socket.
	 * 
	 * @return true if the socket succesfuly bound to an address
	 * @since 1.4
	 */
	boolean isBound();

	/**
	 * Returns the connection state of the socket.
	 * 
	 * @return true if the socket succesfuly connected to a server
	 * @since 1.4
	 */
	boolean isConnected();

	/**
	 * Returns the address to which this socket is connected. Returns null if
	 * the socket is not connected.
	 * 
	 * @return the address to which this socket is connected.
	 */
	InetAddress getInetAddress();

	/**
	 * Returns the port for this socket. Returns -1 if the socket is not
	 * connected.
	 * 
	 * @return the port to which this socket is connected.
	 */
	int getPort();

	/**
	 * Returns the address of the endpoint this socket is connected to, or
	 * <code>null</code> if it is unconnected.
	 * 
	 * @return a <code>SocketAddress</code> representing the remote endpoint of
	 *         this socket, or <code>null</code> if it is not connected yet.
	 * @see #getInetAddress()
	 * @see #getPort()
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
	 * Sends a datagram packet from this socket. The <code>DatagramPacket</code>
	 * includes information indicating the data to be sent, its length, the IP
	 * address of the remote host, and the port number on the remote host.
	 * 
	 * <p>
	 * If there is a security manager, and the socket is not currently connected
	 * to a remote address, this method first performs some security checks.
	 * First, if <code>p.getAddress().isMulticastAddress()</code> is true, this
	 * method calls the security manager's <code>checkMulticast</code> method
	 * with <code>p.getAddress()</code> as its argument. If the evaluation of
	 * that expression is false, this method instead calls the security
	 * manager's <code>checkConnect</code> method with arguments
	 * <code>p.getAddress().getHostAddress()</code> and <code>p.getPort()</code>
	 * . Each call to a security manager method could result in a
	 * SecurityException if the operation is not allowed.
	 * 
	 * @param p
	 *            the <code>DatagramPacket</code> to be sent.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkMulticast</code> or <code>checkConnect</code>
	 *                method doesn't allow the send.
	 * @exception PortUnreachableException
	 *                may be thrown if the socket is connected to a currently
	 *                unreachable destination. Note, there is no guarantee that
	 *                the exception will be thrown.
	 * @exception java.nio.channels.IllegalBlockingModeException
	 *                if this socket has an associated channel, and the channel
	 *                is in non-blocking mode.
	 * 
	 * @see java.net.DatagramPacket
	 * @see SecurityManager#checkMulticast(InetAddress)
	 * @see SecurityManager#checkConnect revised 1.4 spec JSR-51
	 */
	void send(DatagramPacket p) throws IOException;

	/**
	 * Receives a datagram packet from this socket. When this method returns,
	 * the <code>DatagramPacket</code>'s buffer is filled with the data
	 * received. The datagram packet also contains the sender's IP address, and
	 * the port number on the sender's machine.
	 * <p>
	 * This method blocks until a datagram is received. The <code>length</code>
	 * field of the datagram packet object contains the length of the received
	 * message. If the message is longer than the packet's length, the message
	 * is truncated.
	 * <p>
	 * If there is a security manager, a packet cannot be received if the
	 * security manager's <code>checkAccept</code> method does not allow it.
	 * 
	 * @param p
	 *            the <code>DatagramPacket</code> into which to place the
	 *            incoming data.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @exception SocketTimeoutException
	 *                if setSoTimeout was previously called and the timeout has
	 *                expired.
	 * @exception PortUnreachableException
	 *                may be thrown if the socket is connected to a currently
	 *                unreachable destination. Note, there is no guarantee that
	 *                the exception will be thrown.
	 * @exception java.nio.channels.IllegalBlockingModeException
	 *                if this socket has an associated channel, and the channel
	 *                is in non-blocking mode.
	 * @see java.net.DatagramPacket
	 * @see java.net.DatagramSocket revised 1.4 spec JSR-51
	 */
	void receive(DatagramPacket p) throws IOException;

	/**
	 * Gets the local address to which the socket is bound.
	 * 
	 * <p>
	 * If there is a security manager, its <code>checkConnect</code> method is
	 * first called with the host address and <code>-1</code> as its arguments
	 * to see if the operation is allowed.
	 * 
	 * @see SecurityManager#checkConnect
	 * @return the local address to which the socket is bound, or an
	 *         <code>InetAddress</code> representing any local address if either
	 *         the socket is not bound, or the security manager
	 *         <code>checkConnect</code> method does not allow the operation
	 * @since 1.1
	 */
	InetAddress getLocalAddress();

	/**
	 * Returns the port number on the local host to which this socket is bound.
	 * 
	 * @return the port number on the local host to which this socket is bound.
	 */
	int getLocalPort();

	/**
	 * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds.
	 * With this option set to a non-zero timeout, a call to receive() for this
	 * DatagramSocket will block for only this amount of time. If the timeout
	 * expires, a <B>java.net.SocketTimeoutException</B> is raised, though the
	 * DatagramSocket is still valid. The option <B>must</B> be enabled prior to
	 * entering the blocking operation to have effect. The timeout must be > 0.
	 * A timeout of zero is interpreted as an infinite timeout.
	 * 
	 * @param timeout
	 *            the specified timeout in milliseconds.
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as an
	 *             UDP error.
	 * @since JDK1.1
	 * @see #getSoTimeout()
	 */
	void setSoTimeout(int timeout) throws SocketException;

	/**
	 * Retrieve setting for SO_TIMEOUT. 0 returns implies that the option is
	 * disabled (i.e., timeout of infinity).
	 * 
	 * @return the setting for SO_TIMEOUT
	 * @throws SocketException
	 *             if there is an error in the underlying protocol, such as an
	 *             UDP error.
	 * @since JDK1.1
	 * @see #setSoTimeout(int)
	 */
	int getSoTimeout() throws SocketException;

	/**
	 * Sets the SO_SNDBUF option to the specified value for this
	 * <tt>DatagramSocket</tt>. The SO_SNDBUF option is used by the network
	 * implementation as a hint to size the underlying network I/O buffers. The
	 * SO_SNDBUF setting may also be used by the network implementation to
	 * determine the maximum size of the packet that can be sent on this socket.
	 * <p>
	 * As SO_SNDBUF is a hint, applications that want to verify what size the
	 * buffer is should call {@link #getSendBufferSize()}.
	 * <p>
	 * Increasing the buffer size may allow multiple outgoing packets to be
	 * queued by the network implementation when the send rate is high.
	 * <p>
	 * Note: If {@link #send(DatagramPacket)} is used to send a
	 * <code>DatagramPacket</code> that is larger than the setting of SO_SNDBUF
	 * then it is implementation specific if the packet is sent or discarded.
	 * 
	 * @param size
	 *            the size to which to set the send buffer size. This value must
	 *            be greater than 0.
	 * 
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as
	 *                an UDP error.
	 * @exception IllegalArgumentException
	 *                if the value is 0 or is negative.
	 * @see #getSendBufferSize()
	 */
	void setSendBufferSize(int size) throws SocketException;

	/**
	 * Get value of the SO_SNDBUF option for this <tt>DatagramSocket</tt>, that
	 * is the buffer size used by the platform for output on this
	 * <tt>DatagramSocket</tt>.
	 * 
	 * @return the value of the SO_SNDBUF option for this
	 *         <tt>DatagramSocket</tt>
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as
	 *                an UDP error.
	 * @see #setSendBufferSize
	 */
	int getSendBufferSize() throws SocketException;

	/**
	 * Sets the SO_RCVBUF option to the specified value for this
	 * <tt>DatagramSocket</tt>. The SO_RCVBUF option is used by the the network
	 * implementation as a hint to size the underlying network I/O buffers. The
	 * SO_RCVBUF setting may also be used by the network implementation to
	 * determine the maximum size of the packet that can be received on this
	 * socket.
	 * <p>
	 * Because SO_RCVBUF is a hint, applications that want to verify what size
	 * the buffers were set to should call {@link #getReceiveBufferSize()}.
	 * <p>
	 * Increasing SO_RCVBUF may allow the network implementation to buffer
	 * multiple packets when packets arrive faster than are being received using
	 * {@link #receive(DatagramPacket)}.
	 * <p>
	 * Note: It is implementation specific if a packet larger than SO_RCVBUF can
	 * be received.
	 * 
	 * @param size
	 *            the size to which to set the receive buffer size. This value
	 *            must be greater than 0.
	 * 
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as
	 *                an UDP error.
	 * @exception IllegalArgumentException
	 *                if the value is 0 or is negative.
	 * @see #getReceiveBufferSize()
	 */
	void setReceiveBufferSize(int size) throws SocketException;

	/**
	 * Get value of the SO_RCVBUF option for this <tt>DatagramSocket</tt>, that
	 * is the buffer size used by the platform for input on this
	 * <tt>DatagramSocket</tt>.
	 * 
	 * @return the value of the SO_RCVBUF option for this
	 *         <tt>DatagramSocket</tt>
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as
	 *                an UDP error.
	 * @see #setReceiveBufferSize(int)
	 */
	int getReceiveBufferSize() throws SocketException;

	/**
	 * Enable/disable the SO_REUSEADDR socket option.
	 * <p>
	 * For UDP sockets it may be necessary to bind more than one socket to the
	 * same socket address. This is typically for the purpose of receiving
	 * multicast packets (See {@link java.net.MulticastSocket}). The
	 * <tt>SO_REUSEADDR</tt> socket option allows multiple sockets to be bound
	 * to the same socket address if the <tt>SO_REUSEADDR</tt> socket option is
	 * enabled prior to binding the socket using {@link #bind(SocketAddress)}.
	 * <p>
	 * Note: This functionality is not supported by all existing platforms, so
	 * it is implementation specific whether this option will be ignored or not.
	 * However, if it is not supported then {@link #getReuseAddress()} will
	 * always return <code>false</code>.
	 * <p>
	 * When a <tt>DatagramSocket</tt> is created the initial setting of
	 * <tt>SO_REUSEADDR</tt> is disabled.
	 * <p>
	 * The behaviour when <tt>SO_REUSEADDR</tt> is enabled or disabled after a
	 * socket is bound (See {@link #isBound()}) is not defined.
	 * 
	 * @param on
	 *            whether to enable or disable the
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
	 *                if there is an error in the underlying protocol, such as
	 *                an UDP error.
	 * @since 1.4
	 * @see #setReuseAddress(boolean)
	 */
	boolean getReuseAddress() throws SocketException;

	/**
	 * Enable/disable SO_BROADCAST.
	 * 
	 * @param on
	 *            whether or not to have broadcast turned on.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as
	 *                an UDP error.
	 * @since 1.4
	 * @see #getBroadcast()
	 */
	void setBroadcast(boolean on) throws SocketException;

	/**
	 * Tests if SO_BROADCAST is enabled.
	 * 
	 * @return a <code>boolean</code> indicating whether or not SO_BROADCAST is
	 *         enabled.
	 * @exception SocketException
	 *                if there is an error in the underlying protocol, such as
	 *                an UDP error.
	 * @since 1.4
	 * @see #setBroadcast(boolean)
	 */
	boolean getBroadcast() throws SocketException;

	/**
	 * Sets traffic class or type-of-service octet in the IP datagram header for
	 * datagrams sent from this DatagramSocket. As the underlying network
	 * implementation may ignore this value applications should consider it a
	 * hint.
	 * 
	 * <P>
	 * The tc <B>must</B> be in the range <code> 0 <= tc <=
	 * 255</code> or an IllegalArgumentException will be thrown.
	 * <p>
	 * Notes:
	 * <p>
	 * for Internet Protocol v4 the value consists of an octet with precedence
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
	 * for Internet Protocol v6 <code>tc</code> is the value that would be
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
	 * Gets traffic class or type-of-service in the IP datagram header for
	 * packets sent from this DatagramSocket.
	 * <p>
	 * As the underlying network implementation may ignore the traffic class or
	 * type-of-service set using {@link #setTrafficClass(int)} this method may
	 * return a different value than was previously set using the
	 * {@link #setTrafficClass(int)} method on this DatagramSocket.
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
	 * Closes this datagram socket.
	 * <p>
	 * Any thread currently blocked in {@link #receive} upon this socket will
	 * throw a {@link SocketException}.
	 * 
	 * <p>
	 * If this socket has an associated channel then the channel is closed as
	 * well.
	 * 
	 * revised 1.4 spec JSR-51
	 */
	void close();

	/**
	 * Returns whether the socket is closed or not.
	 * 
	 * @return true if the socket has been closed
	 * @since 1.4
	 */
	boolean isClosed();

	/**
	 * Returns the unique {@link java.nio.channels.DatagramChannel} object
	 * associated with this datagram socket, if any.
	 * 
	 * <p>
	 * A datagram socket will have a channel if, and only if, the channel itself
	 * was created via the {@link java.nio.channels.DatagramChannel#open
	 * DatagramChannel.open} method.
	 * 
	 * @return the datagram channel associated with this datagram socket, or
	 *         <tt>null</tt> if this socket was not created for a channel
	 * 
	 * @since 1.4 spec JSR-51
	 */
	DatagramChannel getChannel();

}