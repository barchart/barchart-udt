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

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOption;

import java.net.StandardSocketOptions;

import com.barchart.udt.OptionUDT;

/**
 * TODO expose more of {@link OptionUDT}
 * 
 * A {@link ChannelConfig} for a {@link SocketChannel}.
 * 
 * <h3>Available options</h3>
 * 
 * In addition to the options provided by {@link ChannelConfig},
 * {@link UdtChannelConfig} allows the following options in the option map:
 * 
 * <table border="1" cellspacing="0" cellpadding="6">
 * <tr>
 * <th>Name</th>
 * <th>Associated setter method</th>
 * </tr>
 * <tr>
 * <td>{@code "reuseAddress"}</td>
 * <td>{@link #setReuseAddress(boolean)}</td>
 * </tr>
 * <tr>
 * <td>{@code "soLinger"}</td>
 * <td>{@link #setSoLinger(int)}</td>
 * </tr>
 * <tr>
 * <td>{@code "receiveBufferSize"}</td>
 * <td>{@link #setReceiveBufferSize(int)}</td>
 * </tr>
 * <tr>
 * <td>{@code "sendBufferSize"}</td>
 * <td>{@link #setSendBufferSize(int)}</td>
 * </tr>
 * <tr>
 * </table>
 */
public interface UdtChannelConfig extends ChannelConfig {

	ChannelOption<Integer> UDT_XXX = //
	new ChannelOption<Integer>("UDT_XXX");

	/**
	 * Gets the {@link StandardSocketOptions#SO_RCVBUF} option.
	 */
	int getReceiveBufferSize();

	/**
	 * Gets the {@link StandardSocketOptions#SO_SNDBUF} option.
	 */
	int getSendBufferSize();

	/**
	 * Gets the {@link StandardSocketOptions#SO_LINGER} option.
	 */
	int getSoLinger();

	/**
	 * Gets the {@link StandardSocketOptions#SO_REUSEADDR} option.
	 */
	boolean isReuseAddress();

	/**
	 * Sets the {@link StandardSocketOptions#SO_RCVBUF} option.
	 */
	UdtChannelConfig setReceiveBufferSize(int receiveBufferSize);

	/**
	 * Sets the {@link StandardSocketOptions#SO_REUSEADDR} option.
	 */
	UdtChannelConfig setReuseAddress(boolean reuseAddress);

	/**
	 * Sets the {@link StandardSocketOptions#SO_SNDBUF} option.
	 */
	UdtChannelConfig setSendBufferSize(int sendBufferSize);

	/**
	 * Sets the {@link StandardSocketOptions#SO_LINGER} option.
	 */
	UdtChannelConfig setSoLinger(int soLinger);

	UdtChannelConfig setBacklog(int backlog);

	int getBacklog();

}
