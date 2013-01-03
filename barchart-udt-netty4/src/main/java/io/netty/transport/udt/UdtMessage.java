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
package io.netty.transport.udt;

import io.netty.buffer.ByteBuf;

import com.barchart.udt.TypeUDT;

/**
 * The message container that is used for {@link TypeUDT#DATAGRAM} messages.
 */
public final class UdtMessage {

    private final ByteBuf data;

    /**
     * Create a new instance
     */
    public UdtMessage(final ByteBuf data) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        this.data = data;
    }

    /**
     * Return the container data.
     */
    public ByteBuf data() {
        return data;
    }

    /**
     * Free underlying data.
     */
    public void free() {
        if (data.isFreed()) {
            return;
        } else {
            data.free();
        }
    }

    @Override
    public String toString() {
        return "datagram(" + data.readableBytes() + "B, " + ')';
    }

}
