package com.fatwire.benchmark;

public interface HttpMethodListener {

    void executePerformed(RequestProcessedEvent event);
    void beforeExecute(RequestStartingEvent event);

    
}
