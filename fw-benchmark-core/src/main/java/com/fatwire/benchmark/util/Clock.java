package com.fatwire.benchmark.util;

public final class Clock {
    static {
        synchronized (Clock.class) {
            new Clock();
        }
    }

    private static volatile long currentSec=System.currentTimeMillis() / 1000;

    private Clock() {

        final Thread t = new Thread(new Runnable() {

            public void run() {
                while (true) {
                    currentSec = System.currentTimeMillis() / 1000;
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        }, "Clock");
        t.setDaemon(true);
        t.start();
    }

    public static long getCurrentSeconds() {
        return currentSec;
    }
    
    
}
