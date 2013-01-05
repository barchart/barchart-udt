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

import java.io.PrintStream;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.reporting.AbstractPollingReporter;

/**
 * A simple reporters which prints out application metrics to a
 * {@link PrintStream} periodically.
 */
public class CaliperReporter extends AbstractPollingReporter implements
        MetricProcessor<CaliperMeasure> {

    /**
     * Enables the console reporter for the default metrics registry, and causes
     * it to print to STDOUT with the specified period.
     */
    public static void enable(final long period, final TimeUnit unit) {
        enable(Metrics.defaultRegistry(), period, unit);
    }

    /**
     * Enables the console reporter for the given metrics registry, and causes
     * it to print to STDOUT with the specified period and unrestricted output.
     */
    public static void enable(final MetricsRegistry metricsRegistry,
            final long period, final TimeUnit unit) {
        final CaliperReporter reporter = new CaliperReporter(metricsRegistry,
                MetricPredicate.ALL);
        reporter.start(period, unit);
    }

    private final MetricPredicate predicate;
    private final Clock clock;
    private final TimeZone timeZone;
    private final Locale locale;

    /**
     * Creates a new {@link CaliperReporter} for the default metrics registry,
     * with unrestricted output.
     */
    public CaliperReporter() {
        this(Metrics.defaultRegistry(), MetricPredicate.ALL);
    }

    /**
     * Creates a new {@link CaliperReporter} for a given metrics registry.
     */
    public CaliperReporter(final MetricsRegistry metricsRegistry,
            final MetricPredicate predicate) {
        this(metricsRegistry, predicate, Clock.defaultClock(), TimeZone
                .getDefault());
    }

    /**
     * Creates a new {@link CaliperReporter} for a given metrics registry.
     */
    public CaliperReporter(final MetricsRegistry metricsRegistry,
            final MetricPredicate predicate, final Clock clock,
            final TimeZone timeZone) {
        this(metricsRegistry, predicate, clock, timeZone, Locale.getDefault());
    }

    /**
     * Creates a new {@link CaliperReporter} for a given metrics registry.
     */
    public CaliperReporter(final MetricsRegistry metricsRegistry,
            final MetricPredicate predicate, final Clock clock,
            final TimeZone timeZone, final Locale locale) {
        super(metricsRegistry, "caliper-reporter");
        this.predicate = predicate;
        this.clock = clock;
        this.timeZone = timeZone;
        this.locale = locale;
    }

    @Override
    public void run() {
        try {

            for (final Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry()
                    .groupedMetrics(predicate).entrySet()) {
                // out.print(entry.getKey());
                for (final Entry<MetricName, Metric> subEntry : entry
                        .getValue().entrySet()) {
                    // out.print(subEntry.getKey().getName());
                    subEntry.getValue().processWith(this, subEntry.getKey(),
                            null);
                }
            }
        } catch (final Exception e) {
        }
    }

    @Override
    public void processGauge(final MetricName name, final Gauge<?> gauge,
            final CaliperMeasure measure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processCounter(final MetricName name, final Counter counter,
            final CaliperMeasure measure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processMeter(final MetricName name, final Metered meter,
            final CaliperMeasure measure) {
        measure.process(meter);
    }

    @Override
    public void processHistogram(final MetricName name,
            final Histogram histogram, final CaliperMeasure measure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processTimer(final MetricName name, final Timer timer,
            final CaliperMeasure measure) {
        measure.process(timer);
    }

}
