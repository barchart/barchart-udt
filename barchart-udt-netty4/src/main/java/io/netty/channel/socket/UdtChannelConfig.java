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

import java.io.IOException;

import com.barchart.udt.OptionUDT;
import com.barchart.udt.nio.ChannelServerSocketUDT;
import com.barchart.udt.nio.ChannelSocketUDT;

/**
 * TODO expose more of {@link OptionUDT}
 * <p>
 * A {@link ChannelConfig} for a {@link SocketChannel}.
 * <p>
 * <h3>Available options</h3>
 * In addition to the options provided by {@link ChannelConfig},
 * {@link UdtChannelConfig} allows the following options in the option map:
 * <p>
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

    ChannelOption<Integer> PROTOCOL_RECEIVE_BUFFER_SIZE = //
    new ChannelOption<Integer>("");

    ChannelOption<Integer> PROTOCOL_SEND_BUFFER_SIZE = //
    new ChannelOption<Integer>("");

    ChannelOption<Integer> SYSTEM_RECEIVE_BUFFER_SIZE = //
    new ChannelOption<Integer>("");

    ChannelOption<Integer> SYSTEM_SEND_BUFFER_SIZE = //
    new ChannelOption<Integer>("");

    void apply(final ChannelServerSocketUDT channelUDT) throws IOException;

    void apply(final ChannelSocketUDT channelUDT) throws IOException;

    int getBacklog();

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

    UdtChannelConfig setBacklog(int backlog);

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

    //

    int getProtocolReceiveBufferSize();

    UdtChannelConfig setProtocolReceiveBufferSize(int size);

    int getProtocolSendBufferSize();

    UdtChannelConfig setProtocolSendBufferSize(int size);

    int getSystemReceiveBufferSize();

    UdtChannelConfig setSystemReceiveBufferSize(int size);

    int getSystemSendBufferSize();

    UdtChannelConfig setSystemSendBufferSize(int size);

}
