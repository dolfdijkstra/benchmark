package com.fatwire.benchmark.util;

import junit.framework.TestCase;

public class ClockTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetCurrentSeconds() {
        long then = Clock.getCurrentSeconds();
        System.out.println(then);
        try {
            Thread.sleep(2010L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long now = Clock.getCurrentSeconds();
        System.out.println(now);
        System.out.println(now - then);
    }

}
