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

package io.netty.transport.udt.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.caliper.Measurement;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Timer;

public class CaliperMeasure {

    static final Map<String, Integer> RATE_UNIT = new HashMap<String, Integer>();
    static {
        RATE_UNIT.put("XX/s", 1);
        RATE_UNIT.put("KX/s", 1024);
        RATE_UNIT.put("MX/s", 1024 * 1024);
        RATE_UNIT.put("GX/s", 1024 * 1024 * 1024);
    }

    static final Map<String, Integer> TIME_UNIT = new HashMap<String, Integer>();
    static {
        TIME_UNIT.put("nano", 1);
        TIME_UNIT.put("micr", 1000);
        TIME_UNIT.put("mils", 1000 * 1000);
        TIME_UNIT.put("secs", 1000 * 1000 * 1000);
    }

    private final Map<Long, Measurement> rateMap = new HashMap<Long, Measurement>();
    private final Map<Long, Measurement> timeMap = new HashMap<Long, Measurement>();

    public void recordRate(final double value) {
        rateMap.put(System.nanoTime(), new Measurement(RATE_UNIT, value, value));
    }

    public void recordTime(final double value) {
        timeMap.put(System.nanoTime(), new Measurement(TIME_UNIT, value, value));
    }

    private volatile Metered meter;

    public void process(final Metered meter) {
        if (this.meter == null) {
            this.meter = meter;
        }
        if (this.meter != meter) {
            throw new IllegalStateException("only one meter is allowed");
        }
        if (meter.rateUnit() != TimeUnit.SECONDS) {
            throw new IllegalStateException("meter must use secs rate");
        }
        recordRate(meter.meanRate());
    }

    private volatile Timer timer;

    public void process(final Timer timer) {
        if (this.timer == null) {
            this.timer = timer;
        }
        if (this.timer != timer) {
            throw new IllegalStateException("only one timer is allowed");
        }
        if (timer.rateUnit() != TimeUnit.NANOSECONDS) {
            throw new IllegalStateException("timer mustuse nano rate");
        }
        recordTime(timer.mean());
    }

}
