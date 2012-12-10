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

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import com.barchart.udt.SocketUDT;

/**
 * you must use {@link SelectorProviderUDT#openServerSocketChannel()} to obtain
 * instance of this class; do not use JDK
 * {@link java.nio.channels.ServerSocketChannel#open()}; <br>
 * 
 * example:
 * 
 * [code]
 * 
 * SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
 * 
 * ServerSocketChannel acceptorChannel = provider.openServerSocketChannel();
 * 
 * ServerSocket acceptorSocket = acceptorChannel.socket();
 * 
 * InetSocketAddress acceptorAddress= new InetSocketAddress("localhost", 12345);
 * 
 * acceptorSocket.bind(acceptorAddress);
 * 
 * assert acceptorSocket.isBound();
 * 
 * SocketChannel connectorChannel = acceptorChannel.accept();
 * 
 * assert connectorChannel.isConnected();
 * 
 * [/code]
 */
public class ChannelServerSocketUDT extends ServerSocketChannel implements
		ChannelUDT {

	final SocketUDT serverSocketUDT;

	ChannelServerSocketUDT(SelectorProvider provider, SocketUDT socketUDT) {
		super(provider);
		this.serverSocketUDT = socketUDT;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		serverSocketUDT.close();
	}

	@Override
	protected void implConfigureBlocking(boolean block) throws IOException {
		serverSocketUDT.configureBlocking(block);
	}

	@Override
	public SocketChannel accept() throws IOException {
		try {
			begin();
			SocketUDT socketUDT = serverSocketUDT.accept();
			SelectorProvider provider = provider();
			return new ChannelSocketUDT(provider, socketUDT);
		} finally {
			end(true);
		}
	}

	// guarded by 'this'
	private ServerSocket serverSocketAdapter;

	@Override
	public ServerSocket socket() {
		synchronized (this) {
			if (serverSocketAdapter == null) {
				try {
					serverSocketAdapter = new AdapterServerSocketUDT(this,
							serverSocketUDT);
				} catch (IOException e) {
					return null;
				}
			}
			return serverSocketAdapter;
		}
	}

	@Override
	public SocketUDT getSocketUDT() {
		return serverSocketUDT;
	}

	@Override
	public KindUDT getChannelKind() {
		return KindUDT.ACCEPTOR;
	}

	@Override
	public boolean isOpenSocketUDT() {
		return serverSocketUDT.isOpen();
	}

	//

	@Override
	public String toString() {
		return serverSocketUDT.toString();
	}

}
