package com.fatwire.benchmark.util;

public final class HiResStopWatch {

    private boolean running = false;

    private long startTime;

    private long elapsed;

    private final TimePrecision precision;

    /**
     * Default constructor with MILLISECOND precision
     * 
     *
     */
    public HiResStopWatch() {

        precision = TimePrecision.MILLISECOND;
    }

    /**
     * @param precision precision for this StopWatch
     */
    public HiResStopWatch(TimePrecision precision) {

        this.precision = precision;
    }

    public void start() {
        if (running)
            throw new IllegalStateException(
                    "StopWatch is running, can't start it again. Please stop it first");
        running = true;
        startTime = System.nanoTime();
    }

    public long stop() {
        if (!running)
            throw new IllegalStateException(
                    "StopWatch is not running, can't stop it. Please start it first");

        elapsed = precision.calculateElapsed(System.nanoTime() - startTime);
        running = false;
        return elapsed;

    }

    public long getElapsed() {
        if (running) {
            throw new IllegalStateException(
                    "StopWatch is running, elapsed has not been calculated. Please stop it first");

        }
        return elapsed;
    }
    public long getIntermediate(){
        if (!running)
            throw new IllegalStateException(
                    "StopWatch is not running, can't produce an intermediate time. Please start it first");

        return precision.calculateElapsed(System.nanoTime()-startTime);
    }

    public String toString() {
        if (running)
            return "Running...";
        return precision.toString(getElapsed());
    }

}
