package com.fatwire.benchmark.connectionmanager;

public interface ConnectionPool {

    public void shutdown();

    public PooledHttpConnection getConnection(final long timeout);

    public void releaseConnection(final PooledHttpConnection conn);

    /**
     * The method increase the idleCount on any idle connection and close the connection is the idleCounter is above the maxIdelCount
     * The contract is that this method is called every so often by a background thread and it is the responsibility of this thread
     * to keep track on how othen this method is called
     * For instance, if this method is called every 1 second, and the maxIdleCount is 30, then after 30 invocations (30 seconds) connections are closed.
     * 
     * 
     * @param maxIdleCount
     */
    public void checkAndCloseIdleConnections(final int maxIdleCount);

    public int inUseCount();

    public int idleConnectionsCount();

    public int getPoolSize();

}