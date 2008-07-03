/**
 * 
 */
package com.fatwire.benchmark;

import java.util.concurrent.atomic.AtomicInteger;

class CounterConditional implements Condition {
    /**
     * 
     */
    private final AtomicInteger counter = new AtomicInteger();

    private final int end;

    private int progressMarker;

    /**
     * @param end
     * @param mark TODO
     */
    public CounterConditional(final int end) {
        super();
        this.end = end;
        if (end < 100) {
            progressMarker = 10;
        } else {
            progressMarker = 50;
        }
    }

    public boolean isTrue() {
        return counter.get() < end;
    }

    public int increment() {
        int num = counter.incrementAndGet();
        if (num % progressMarker == 0)
            System.out.print(".");
        if (num % 500 == 0)
            System.out.println();

        return num;
    }

    public int getNum() {
        return counter.get();
    }

}