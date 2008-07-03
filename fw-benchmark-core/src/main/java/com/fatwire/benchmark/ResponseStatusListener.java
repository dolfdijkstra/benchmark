package com.fatwire.benchmark;

public class ResponseStatusListener implements HttpMethodListener {

    /**
     * @param stats
     */
    public ResponseStatusListener() {
        super();
    }

    public void executePerformed(final RequestProcessedEvent event) {
        if (event.getStatus() >= 400) {
            System.err.println(event.getUri() + " returned "
                    + event.getStatus());
        }

    }

    public void beforeExecute(RequestStartingEvent event) {
        
    }

}
