/**
 * 
 */
package com.fatwire.benchmark;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * Condition that turns false when a certain number of invocations has been reached.
 * 
 * 
 * @author Dolf.Dijkstra
 * @since Sep 27, 2008
 */

class CounterConditional implements Condition {
    /**
     * 
     */
    private final AtomicInteger counter = new AtomicInteger();

    private final int end;

    private int progressMarker;

    private boolean showProgress = false;

    /**
     * @param end
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
        if (showProgress) {
            if (num % progressMarker == 0)
                System.out.print(".");
            if (num % 500 == 0)
                System.out.println();
        }

        return num;
    }

    public int getNum() {
        return counter.get();
    }

}