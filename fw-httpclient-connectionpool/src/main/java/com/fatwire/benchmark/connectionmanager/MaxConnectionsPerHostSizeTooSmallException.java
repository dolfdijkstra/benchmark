package com.fatwire.benchmark.connectionmanager;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class MaxConnectionsPerHostSizeTooSmallException extends
        RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1263745355358155672L;

    private final HttpConnectionManagerParams params;

    private final HostConfiguration hostConfiguration;

    public MaxConnectionsPerHostSizeTooSmallException(
            HttpConnectionManagerParams params,
            HostConfiguration hostConfiguration) {
        this.hostConfiguration = hostConfiguration;
        this.params = params;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
        return "The MaxConnectionsPerHost parameter is too small ("+ params.getMaxConnectionsPerHost(hostConfiguration) + ") for host configuration " + this.hostConfiguration;
    }

}
