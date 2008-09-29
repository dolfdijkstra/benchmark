package com.fatwire.benchmark.connectionmanager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class PoolManager {

    private final Timer timer;

    private final List<ConnectionPool> pools = new CopyOnWriteArrayList<ConnectionPool>();

    private int maxIdleCount = 30;

    final TimerTask task = new TimerTask() {

        @Override
        public void run() {
            for (ConnectionPool pool : pools) {
                pool.checkAndCloseIdleConnections(PoolManager.this.maxIdleCount);
            }
        }

    };

    public PoolManager(final int maxIdleCount) {
        this.maxIdleCount = maxIdleCount;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    void register(final ConnectionPool pool) {
        if (pools.contains(pool)){
            throw new java.lang.IllegalArgumentException("pool is already registered.");
        }
        pools.add(pool);
    }

    void deregister(final ConnectionPool pool) {
        pools.remove(pool);
    }

    public void shutdown() {
        task.cancel();
        timer.cancel();
        pools.clear();
        

    }

}
