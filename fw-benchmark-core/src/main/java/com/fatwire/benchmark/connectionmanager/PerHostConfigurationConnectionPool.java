package com.fatwire.benchmark.connectionmanager;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PerHostConfigurationConnectionPool implements ConnectionPool {
    static final Log LOG = LogFactory
            .getLog(PerHostConfigurationConnectionPool.class);

    private final HostConfiguration hostConfiguration;

    PooledHttpConnection[] pool;

    final Object poolLock = new Object();

    private final HttpConnectionManager connectionManager;

    boolean shutdown = false;

    int useCount;

    /**
     * @param hostConfiguration
     */
    public PerHostConfigurationConnectionPool(
            final HttpConnectionManager connectionManager,
            final HostConfiguration hostConfiguration) {
        super();
        if (connectionManager.getParams().getMaxConnectionsPerHost(
                hostConfiguration) < 1) {
            throw new MaxConnectionsPerHostSizeTooSmallException(
                    connectionManager.getParams(), hostConfiguration);
        }
        int poolSize = Math.max(1, connectionManager.getParams()
                .getMaxConnectionsPerHost(hostConfiguration));
        if (poolSize > 100) {
            poolSize = 100;
        }
        this.connectionManager = connectionManager;
        this.hostConfiguration = hostConfiguration;
        pool = new PooledHttpConnection[poolSize];
        fillPool();

    }

    /*
     * To be called from a method that has a lock on poolLock
     *
     */
    private void fillPool() {
        for (int i = 0; i < pool.length; i++) {
            if (pool[i] == null) {
                pool[i] = createPooledHttpConnection();

            }
        }

    }

    public void shutdown() {
        int closedCounter = 0;
        shutdown = true;
        synchronized (poolLock) {
            for (int i = 0; i < pool.length; i++) {
                if (pool[i] != null) {
                    if (!pool[i].isInUse()) {
                        if (pool[i].isOpen()) {
                            pool[i].close();
                        }
                        closedCounter++;
                    } else {
                        LOG.warn("PooledHttpConnection[" + i
                                + "] is still in use. " + pool[i]);
                    }
                    pool[i] = null;
                } else {
                    closedCounter++;
                }
            }
        }
        if (pool.length > closedCounter) {
            throw new IllegalStateException(
                    "Not all connections closed in the pool by shutdown(), some are marked as in use.");
        }
    }

    PooledHttpConnection createPooledHttpConnection() {
        final PooledHttpConnection conn = new PooledHttpConnection(
                hostConfiguration, connectionManager, this);
        conn.setInUse(false);
        conn.getParams().setDefaults(connectionManager.getParams());
        return conn;
    }

    /*
     * To be called from a method that has a lock on poolLock
     *
     */

    boolean increasePool() {
        int maxPoolSize = connectionManager.getParams()
                .getMaxConnectionsPerHost(hostConfiguration);
        //System.out.println("increasePool " + maxPoolSize);
        if (pool.length < maxPoolSize) {

            int newSize = Math.min(Math.max(2, pool.length * 2), maxPoolSize);
            //System.out.println("increasePool2 " + newSize);
            final PooledHttpConnection[] newPool = new PooledHttpConnection[newSize];
            System.arraycopy(pool, 0, newPool, 0, pool.length);
            pool = newPool;
            fillPool();
            return true;
        } else {
            return false;
        }
    }

    public PooledHttpConnection getConnection(final long timeout) {
        if (shutdown) {
            throw new IllegalStateException("pool has been shut down.");
        }
        long t = System.nanoTime();
        synchronized (poolLock) {
            PooledHttpConnection conn = null;
            // let's see if we have an available connection
            for (int i = 0; (i < pool.length) && (conn == null); i++) {
                if (pool[i] != null) {
                    if (!pool[i].isInUse()) {

                        conn = pool[i];
                        conn.setInUse(true);
                        conn.resetIdleCounter();

                    }
                }
            }
            if (conn == null) {
                //so we did not get a free connection
                //increase pool
                if (!increasePool()) {
                    //if we could not increase the pool
                    //wait for an connection inUse to return
                    final long startWait = System.currentTimeMillis();
                    try {
                        poolLock.wait(timeout);
                    } catch (final InterruptedException e) {
                        LOG.warn("waiting for the pool was interrupted", e);
                    }
                    final long endWait = System.currentTimeMillis();
                    final long newTimeout = (timeout == 0 ? 0 : timeout
                            - (endWait - startWait));
                    //System.out.println("newTimeout " + newTimeout);
                    if (newTimeout <= 0 && timeout != 0) {
                        return null;
                    }
                    return getConnection(newTimeout);
                }
                return getConnection(timeout);
            } else {
                useCount++;
                //conn.increaseUseCount();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("getConnection(" + timeout + ") conn.usecount: "
                            + conn.getUseCount() + " pool usecount" + useCount + " in "
                            + Long.toString((System.nanoTime() - t) / 1000)
                            + "us");
                }

                return conn;
            }
        }
    }

    public void releaseConnection(final PooledHttpConnection conn) {
        ConnectionHelper.finishLastResponse(conn);
        synchronized (poolLock) {
            final PooledHttpConnection w = conn;//(PooledHttpConnection) conn;
            w.setInUse(false);
            w.resetIdleCounter();
            useCount--;
            poolLock.notifyAll();
        }

    }

    /**
     * The method increase the idleCount on any idle connection and close the connection is the idleCounter is above the maxIdelCount
     * The contract is that this method is called every so often by a background thread and it is the responsibility of this thread
     * to keep track on how othen this method is called
     * For instance, if this method is called every 1 second, and the maxIdleCount is 30, then after 30 invocations (30 seconds) connections are closed.
     * 
     * 
     * @param maxIdleCount
     */

    public void checkAndCloseIdleConnections(final int maxIdleCount) {
        LOG.trace("checkAndCloseIdleConnections (" + maxIdleCount + ") "
                + this.hashCode());
        synchronized (poolLock) {
            for (int i = 0; i < pool.length; i++) {
                final PooledHttpConnection conn = pool[i];
                if ((conn != null) && !conn.isInUse() && conn.isOpen()) {
                    final int val = conn.increaseIdleCount();
                    if (val > maxIdleCount) {
                        LOG.debug("Closing connection[" + i + "] for poolSize "
                                + pool.length + ". UseCount was "
                                + conn.getUseCount());
                        conn.close();
                    }
                }
            }
        }
    }

    public int inUseCount() {
        int inUse = 0;
        for (int i = 0; i < pool.length; i++) {
            final PooledHttpConnection conn = pool[i];
            if ((conn != null) && conn.isInUse()) {
                inUse++;
            }
        }
        return inUse;
    }

    public int idleConnectionsCount() {
        int idle = 0;
        for (int i = 0; i < pool.length; i++) {
            final PooledHttpConnection conn = pool[i];
            if ((conn != null) && !conn.isInUse()) {
                idle++;
            }
        }
        return idle;
    }

    public int getPoolSize() {
        return pool.length;
    }
}