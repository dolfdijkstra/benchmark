/**
 * 
 */
package com.fatwire.benchmark;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Condition that turns false after a period of time;
 * 
 * @author Dolf.Dijkstra
 * @since Sep 19, 2008
 */

class TimeConditional implements Condition {
    private static Log log = LogFactory.getLog(TimeConditional.class);

    private volatile boolean val = true;

    private final AtomicInteger counter = new AtomicInteger();

    private final Thread t;

    public TimeConditional(final int seconds) {
        t = new Thread(new Runnable() {

            public void run() {
                if (!val)
                    return; //we may have been killed before we went into action
                try {
                    Thread.sleep(seconds * 1000L);
                } catch (InterruptedException e) {
                    log.trace(e.getMessage());
                    val = false;
                } finally {
                    val = false;
                }

            }

        });
        t.start();
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

    public void kill() {
        t.interrupt();
    }

}