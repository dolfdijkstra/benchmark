package com.fatwire.benchmark.connectionmanager;

public interface PoolLifeCycle {

    void shutdown();
    
    boolean isShutdown();
    
    void startup();
    
}
