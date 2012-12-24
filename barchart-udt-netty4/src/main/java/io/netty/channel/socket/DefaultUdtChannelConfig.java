/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.socket;

import static io.netty.channel.ChannelOption.*;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;

import java.net.SocketException;
import java.util.Map;

import com.barchart.udt.SocketUDT;

/**
 * The default {@link UdtChannelConfig} implementation.
 */
public class DefaultUdtChannelConfig extends DefaultChannelConfig implements
		UdtChannelConfig {

	private final SocketUDT socket;

	/**
	 * Creates a new instance.
	 */
	public DefaultUdtChannelConfig(final SocketUDT socket) {
		if (socket == null) {
			throw new NullPointerException("socket");
		}
		this.socket = socket;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOption(final ChannelOption<T> option) {
		if (option == SO_RCVBUF) {
			return (T) Integer.valueOf(getReceiveBufferSize());
		}
		if (option == SO_SNDBUF) {
			return (T) Integer.valueOf(getSendBufferSize());
		}
		if (option == SO_REUSEADDR) {
			return (T) Boolean.valueOf(isReuseAddress());
		}
		if (option == SO_LINGER) {
			return (T) Integer.valueOf(getSoLinger());
		} else if (option == SO_BACKLOG) {
			return (T) Integer.valueOf(getBacklog());
		}
		return super.getOption(option);
	}

	@Override
	public Map<ChannelOption<?>, Object> getOptions() {
		return getOptions( //
				super.getOptions(), //
				SO_RCVBUF, //
				SO_SNDBUF, //
				SO_REUSEADDR, //
				SO_LINGER, //
				SO_BACKLOG);
	}

	@Override
	public int getReceiveBufferSize() {
		try {
			return socket.getReceiveBufferSize();
		} catch (final SocketException e) {
			throw new ChannelException(e);
		}
	}

	@Override
	public int getSendBufferSize() {
		try {
			return socket.getSendBufferSize();
		} catch (final SocketException e) {
			throw new ChannelException(e);
		}
	}

	@Override
	public int getSoLinger() {
		try {
			return socket.getSoLinger();
		} catch (final SocketException e) {
			throw new ChannelException(e);
		}
	}

	@Override
	public boolean isReuseAddress() {
		try {
			return socket.getReuseAddress();
		} catch (final SocketException e) {
			throw new ChannelException(e);
		}
	}

	@Override
	public UdtChannelConfig setAllocator(final ByteBufAllocator allocator) {
		return (UdtChannelConfig) super.setAllocator(allocator);
	}

	@Override
	public UdtChannelConfig setConnectTimeoutMillis(
			final int connectTimeoutMillis) {
		return (UdtChannelConfig) super
				.setConnectTimeoutMillis(connectTimeoutMillis);
	}

	@Override
	public <T> boolean setOption(final ChannelOption<T> option, final T value) {

		validate(option, value);

		if (option == SO_RCVBUF) {
			setReceiveBufferSize((Integer) value);
		} else if (option == SO_SNDBUF) {
			setSendBufferSize((Integer) value);
		} else if (option == SO_REUSEADDR) {
			setReuseAddress((Boolean) value);
		} else if (option == SO_LINGER) {
			setSoLinger((Integer) value);
		} else if (option == SO_BACKLOG) {
			setBacklog((Integer) value);
		} else {
			return super.setOption(option, value);
		}

		return true;

	}

	@Override
	public UdtChannelConfig setReceiveBufferSize(final int receiveBufferSize) {
		try {
			socket.setReceiveBufferSize(receiveBufferSize);
		} catch (final SocketException e) {
			throw new ChannelException(e);
		}
		return this;
	}

	@Override
	public UdtChannelConfig setReuseAddress(final boolean reuseAddress) {
		try {
			socket.setReuseAddress(reuseAddress);
		} catch (final SocketException e) {
			throw new ChannelException(e);
		}
		return this;
	}

	@Override
	public UdtChannelConfig setSendBufferSize(final int sendBufferSize) {
		try {
			socket.setSendBufferSize(sendBufferSize);
		} catch (final SocketException e) {
			throw new ChannelException(e);
		}
		return this;
	}

	@Override
	public UdtChannelConfig setSoLinger(final int soLinger) {
		try {
			if (soLinger < 0) {
				socket.setSoLinger(false, 0);
			} else {
				socket.setSoLinger(true, soLinger);
			}
		} catch (final SocketException e) {
			throw new ChannelException(e);
		}
		return this;
	}

	@Override
	public UdtChannelConfig setWriteSpinCount(final int writeSpinCount) {
		return (UdtChannelConfig) super.setWriteSpinCount(writeSpinCount);
	}

	private int backlog = 100;

	@Override
	public UdtChannelConfig setBacklog(final int backlog) {
		this.backlog = backlog;
		return this;
	}

	@Override
	public int getBacklog() {
		return backlog;
	}

}
