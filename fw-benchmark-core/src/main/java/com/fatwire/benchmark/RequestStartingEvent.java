package com.fatwire.benchmark;

public class RequestStartingEvent {
    private final HttpWorker worker;
    private long startTime;
    /**
     * @param worker
     * @param startTime
     */
    public RequestStartingEvent(final HttpWorker worker, long startTime) {
        super();
        this.worker = worker;
        this.startTime = startTime;
    }
    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }
    /**
     * @return the worker
     */
    public HttpWorker getWorker() {
        return worker;
    }

}
