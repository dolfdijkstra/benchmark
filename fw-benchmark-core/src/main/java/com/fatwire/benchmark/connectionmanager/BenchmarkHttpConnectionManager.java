package com.fatwire.benchmark.connectionmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.ConnectionPoolTimeoutException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class BenchmarkHttpConnectionManager implements HttpConnectionManager {

    private HttpConnectionManagerParams params = new HttpConnectionManagerParams();

    private final Map<HostConfiguration, ConnectionPool> pools = new ConcurrentHashMap<HostConfiguration, ConnectionPool>();

    private final PoolManager poolManager;

    /**
     * @param hostConfiguration
     */
    public BenchmarkHttpConnectionManager(PoolManager poolManager) {
        super();
        this.poolManager = poolManager;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnectionManager#closeIdleConnections(long)
     */
    public void closeIdleConnections(final long idleTimeout) {
        // do nothing

    }

    public void shutdown() {
        for (ConnectionPool pool : pools.values()) {
            pool.shutdown();
        }
        pools.clear();
    }

    private synchronized ConnectionPool getPool(
            final HostConfiguration hostConfiguration) {
        ConnectionPool pool = pools.get(hostConfiguration);
        if (pool == null) {
            pool = new PerHostConfigurationConnectionPool(this,
                    hostConfiguration);
            pools.put(hostConfiguration, pool);
            poolManager.register(pool);
            return pools.get(hostConfiguration);
        }
        return pool;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnectionManager#getConnection(org.apache.commons.httpclient.HostConfiguration)
     */
    public PooledHttpConnection getConnection(
            final HostConfiguration hostConfiguration) {
        //System.out.println(Thread.currentThread().getName() + " getConnection()");
        return getPool(hostConfiguration).getConnection(0);

    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnectionManager#getConnection(org.apache.commons.httpclient.HostConfiguration, long)
     */
    /**
     * @deprecated Use #getConnectionWithTimeout(HostConfiguration, long) 
     *
     */
    public PooledHttpConnection getConnection(
            final HostConfiguration hostConfiguration, final long timeout)
            throws HttpException {
        try {
            return getConnectionWithTimeout(hostConfiguration, timeout);
        } catch (ConnectionPoolTimeoutException e) {
            throw new HttpException(e.getMessage());

        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnectionManager#getConnectionWithTimeout(org.apache.commons.httpclient.HostConfiguration, long)
     */
    public PooledHttpConnection getConnectionWithTimeout(
            final HostConfiguration hostConfiguration, final long timeout)
            throws ConnectionPoolTimeoutException {

        if (hostConfiguration == null)
            throw new IllegalArgumentException(
                    "hostConfiguration can not be null.");
        if (timeout < 0)
            throw new IllegalArgumentException(
                    "timeout can not be smaller than zero.");
        //System.out.println(Thread.currentThread().getName() + " getConnection("+timeout+")");        
        final PooledHttpConnection conn = getPool(hostConfiguration)
                .getConnection(timeout);
        if (conn == null) {
            throw new ConnectionPoolTimeoutException(
                    "Timeout waiting for a connection to " + hostConfiguration);
        }
        return conn;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnectionManager#getParams()
     */
    public HttpConnectionManagerParams getParams() {
        return params;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnectionManager#releaseConnection(org.apache.commons.httpclient.HttpConnection)
     */
    public void releaseConnection(final HttpConnection conn) {
        if (conn instanceof PooledHttpConnection) {
            PooledHttpConnection pConn = (PooledHttpConnection) conn;
            pConn.getPool().releaseConnection(pConn);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpConnectionManager#setParams(org.apache.commons.httpclient.params.HttpConnectionManagerParams)
     */
    public void setParams(final HttpConnectionManagerParams params) {
        this.params = params;

    }

}