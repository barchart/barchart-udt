package io.netty.transport.udt.util;

import io.netty.logging.InternalLoggerFactory;
import io.netty.logging.Slf4JLoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.caliper.SimpleBenchmark;

public class CaliperBench extends SimpleBenchmark {

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    TrafficControl.delay(0);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static {
        final InternalLoggerFactory defaultFactory = new Slf4JLoggerFactory();
        InternalLoggerFactory.setDefaultFactory(defaultFactory);
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final CaliperMeasure measure = new CaliperMeasure();

    protected CaliperMeasure measure() {
        // CaliperMeasure measure = this.measure;
        // if (measure == null) {
        // measure = this.measure = new CaliperMeasure();
        // }
        return measure;
    }

    protected void markWait(final long time) throws Exception {

        final long timeStart = System.currentTimeMillis();

        while (true) {
            final long timeFinish = System.currentTimeMillis();
            final long timeDiff = timeFinish - timeStart;
            if (timeDiff >= time) {
                break;
            } else {
                log.info("mark");
                measure().mark();
                Thread.sleep(3 * 1000);
            }
        }

    }

}
