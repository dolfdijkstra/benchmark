package com.fatwire.benchmark.util;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Dolf.Dijkstra
 * @since Dec 15, 2007
 */
public class HiResStopWatchTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * test for correct scale of returned value
     */
    public void testElapsed() {
        HiResStopWatch stopWatch = new HiResStopWatch();
        stopWatch.start();
        sleepFor(20, 0);
        long e = stopWatch.stop();
        Assert.assertTrue(e > 18 && e < 22);

    }

    public void testElapsedMicro() {
        HiResStopWatch stopWatch = new HiResStopWatch(TimePrecision.MICROSECOND);
        stopWatch.start();
        sleepFor(20, 0);
        long e = stopWatch.stop();

        Assert.assertTrue(e > 19000);
        Assert.assertTrue(e < 21000);

    }

    public void testElapsedNano() {
        HiResStopWatch stopWatch = new HiResStopWatch(TimePrecision.NANOSECOND);
        stopWatch.start();
        sleepFor(20, 0);
        long e = stopWatch.stop();

        Assert.assertTrue(e > 19000000);
        Assert.assertTrue(e < 21000000);

    }

    void sleepFor(long milli, int nano) {
        try {
            Thread.sleep(milli, nano);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
