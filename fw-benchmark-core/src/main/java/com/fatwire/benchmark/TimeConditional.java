/**
 * 
 */
package com.fatwire.benchmark;

import java.util.concurrent.atomic.AtomicInteger;

class TimeConditional implements Condition {

    private volatile boolean val = true;
    private final AtomicInteger counter = new AtomicInteger();
    
    public TimeConditional(final int seconds) {
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(seconds * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    val = false;
                } finally {
                    val = false;
                }

            }

        }).start();
    }

    public boolean isTrue() {

        return val;
    }

    public int increment() {
        return counter.incrementAndGet();
    }

    public int getNum() {
        return counter.get();
    }

}