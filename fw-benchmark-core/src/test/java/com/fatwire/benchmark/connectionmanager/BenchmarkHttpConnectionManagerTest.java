package com.fatwire.benchmark.connectionmanager;

import junit.framework.TestCase;

import org.apache.commons.httpclient.ConnectionPoolTimeoutException;
import org.apache.commons.httpclient.HostConfiguration;

public class BenchmarkHttpConnectionManagerTest extends TestCase {

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

    public void testBenchmarkHttpConnectionManager() {
        assertNotNull(connectionManager);

    }

    public void testGetConnectionHostConfiguration() {
        PooledHttpConnection conn = connectionManager
                .getConnection(hostConfiguration);
        assertNotNull(conn);
        conn.releaseConnection();
    }


    public void testGetConnectionWithTimeout() {
        PooledHttpConnection[] conn = new PooledHttpConnection[3];
        for (int i = 0; i < 3; i++) {
            try {
                conn[i] = connectionManager.getConnectionWithTimeout(
                        hostConfiguration, 20);
                assertNotNull(conn[i]);
            } catch (ConnectionPoolTimeoutException e) {
                assertTrue(i == 2);
            }

        }
        for (int i = 0; i < conn.length; i++) {
            if (conn[i] != null) {
                conn[i].releaseConnection();
            }
        }

    }

    public void testGetConnectionWithTimeoutBiggerSize() {
        connectionManager.getParams().setMaxConnectionsPerHost(
                hostConfiguration, 3);
        PooledHttpConnection[] conn = new PooledHttpConnection[4];
        for (int i = 0; i < conn.length; i++) {
            try {
                conn[i] = connectionManager.getConnectionWithTimeout(
                        hostConfiguration, 50);
                assertNotNull(conn[i]);
                assertTrue(conn[i].isInUse());
            } catch (ConnectionPoolTimeoutException e) {
                //                System.err.println(i);
                //                e.printStackTrace();
                assertTrue(i == 3);

            }

        }
        for (int i = 0; i < conn.length; i++) {
            if (conn[i] != null) {
                conn[i].releaseConnection();
            }
        }

    }

    public void testReleaseConnection() {
        PooledHttpConnection conn = connectionManager
                .getConnection(hostConfiguration);
        conn.releaseConnection();

        assertFalse(conn.isInUse());

    }
}
