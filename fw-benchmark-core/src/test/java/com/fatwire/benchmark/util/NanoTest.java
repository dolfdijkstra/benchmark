package com.fatwire.benchmark.util;

import junit.framework.TestCase;

public class NanoTest extends TestCase {

    
    int runs=1000000;
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNano() {

        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            System.nanoTime();
        }
        System.out.println("nano: " + ((System.nanoTime() - start) / 1000));

    }

    public void testMilli() {

        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            System.currentTimeMillis();
        }
        System.out.println("milli: " + ((System.nanoTime() - start) / 1000));

    }
    public void testNano2() {

        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            System.nanoTime();
        }
        System.out.println("nano: " + ((System.nanoTime() - start) / 1000));

    }

}
