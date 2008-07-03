package com.fatwire.benchmark.connectionmanager;

import org.apache.commons.httpclient.HostConfiguration;

import junit.framework.TestCase;

public class PoolManagerTest extends TestCase {

    HostConfiguration hostConfiguration;

    BenchmarkHttpConnectionManager connectionManager;

    PoolManager poolManager;

    protected void setUp() throws Exception {
        super.setUp();
        poolManager = new PoolManager(5);
        hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("localhost", 8080);

        connectionManager = new BenchmarkHttpConnectionManager(poolManager);

    }

    protected void tearDown() throws Exception {
        try {
            connectionManager.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        poolManager.shutdown();
        super.tearDown();
    }

    public void testShutdown() {
        PooledHttpConnection conn = connectionManager
                .getConnection(hostConfiguration);
    }

}
