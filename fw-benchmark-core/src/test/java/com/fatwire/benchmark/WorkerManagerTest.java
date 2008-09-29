package com.fatwire.benchmark;

import junit.framework.TestCase;

public class WorkerManagerTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRun() {
        int step=5;
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            //System.out.println(i % step == (step-1));
            System.out.println(i % (step+1) == 0);
        }

    }

}
