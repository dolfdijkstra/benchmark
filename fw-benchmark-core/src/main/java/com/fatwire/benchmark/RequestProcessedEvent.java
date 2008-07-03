package com.fatwire.benchmark;

import java.net.URI;

import org.apache.commons.httpclient.Header;

public class RequestProcessedEvent {
    private final HttpWorker worker;

    private final HttpTransaction httpTransaction;

    public RequestProcessedEvent(HttpWorker worker, HttpTransaction transaction) {
        this.worker = worker;
        this.httpTransaction = transaction;
    }

    /**
     * @return the elapsed
     */
    public long getElapsed() {
        return httpTransaction.getDownloadTime();
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return httpTransaction.getResponse().getStatusCode();
    }

    /**
     * @return the uri
     */
    public URI getUri() {
        return httpTransaction.getRequest().getUri();
    }

    /**
     * @return the worker
     */
    public HttpWorker getWorker() {
        return worker;
    }

    public String getConnectionHeader() {
        for (Header header : httpTransaction.getResponse().getHeaders()) {
            if ("Connection".equalsIgnoreCase(header.getName())) {
                return header.getValue();
            }
        }
        return null;
    }

    /**
     * @return the bodySize
     */
    public int getBodySize() {
        return httpTransaction.getResponse().getBody().length;
    }

    /**
     * @return the httpTransaction
     */
    public HttpTransaction getHttpTransaction() {
        return httpTransaction;
    }

}
