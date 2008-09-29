package com.fatwire.benchmark.connectionmanager;

import java.io.IOException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A HttpConnection with inUse flag and idleCounter
 * 
 * @author Dolf.Dijkstra
 * @since Jun 2, 2008
 */
public class PooledHttpConnection extends HttpConnection {
    private static final Log LOG = LogFactory
            .getLog(PooledHttpConnection.class);

    private int idleCounter = 0;

    private int useCounter = 0;

    private boolean inUse;

    private final ConnectionPool pool;

    protected PooledHttpConnection(final HostConfiguration hostConfig,
            final HttpConnectionManager connectionManager,
            ConnectionPool pool) {
        super(hostConfig);
        this.pool = pool;
        super.setHttpConnectionManager(connectionManager);
        getParams().setDefaults(connectionManager.getParams());

    }

    ConnectionPool getPool() {
        return pool;
    }

    synchronized void resetIdleCounter() {
        idleCounter = 0;
    }

    synchronized int increaseIdleCount() {
        if (isOpen()) {
            return ++idleCounter;
        } else {
            return 0;
        }
    }

    synchronized int increaseUseCount() {
        return ++useCounter;
    }

    /**
     * @return the idleCounter
     */
    int getIdleCount() {
        return idleCounter;
    }

    /**
     * @return the inUse
     */
    boolean isInUse() {
        return inUse;
    }

    /**
     * @param inUse the inUse to set
     */
    synchronized void setInUse(final boolean inUse) {
        this.inUse = inUse;
        if (inUse) {
            useCounter++;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnection#setHttpConnectionManager(org.apache.commons.httpclient.HttpConnectionManager)
     */
    @Override
    public void setHttpConnectionManager(
            final HttpConnectionManager httpConnectionManager) {
        throw new java.lang.UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnection#close()
     */
    @Override
    public void close() {
        int oldCounter = useCounter;
        useCounter = 0;
        long t = LOG.isTraceEnabled() ? System.nanoTime() : 0;
        try {
            super.close();
        } finally {
            if (t != 0) {
                long end = System.nanoTime();
                LOG.trace("Closing connection to " + this.getHost()
                        + ", useCount=" + oldCounter + " took: "
                        + Long.toString((end - t) / 1000L) + "us.");
            }
        }
    }

    /**
     * @return the useCounter
     */
    public int getUseCount() {
        return useCounter;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnection#open()
     */
    @Override
    public void open() throws IOException {
        long t = LOG.isTraceEnabled() ? System.nanoTime() : 0;
        try {
            super.open();
        } finally {
            if (t != 0) {
                long end = System.nanoTime();
                LOG.trace("Connecting to " + this.getHost() + " took: "
                        + Long.toString((end - t) / 1000L) + "us.");
            }
        }
    }

}