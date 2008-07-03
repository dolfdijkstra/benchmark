package com.fatwire.benchmark.plexus;

import java.io.IOException;

public interface BenchmarkRunner
{
    /** The Plexus role identifier. */
    String ROLE = BenchmarkRunner.class.getName();

    void init() throws Exception;
    void go() throws InterruptedException;
    void shutdown();
    /**
     * @return
     * @see com.fatwire.benchmark.FatWireBenchmark#getDelay()
     */
    public long getDelay();
    /**
     * @return
     * @see com.fatwire.benchmark.FatWireBenchmark#getNumberOfWorkers()
     */
    public int getNumberOfWorkers();
    /**
     * @return
     * @see com.fatwire.benchmark.FatWireBenchmark#getType()
     */
    public String getType();
    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setDelay(long)
     */
    public void setDelay(long arg0);
    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setMax(int)
     */
    public void setMax(int arg0);
    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setNumberOfWorkers(int)
     */
    public void setNumberOfWorkers(int arg0);
    /**
     * @param arg0
     * @throws IOException
     * @see com.fatwire.benchmark.FatWireBenchmark#setScript(java.lang.String)
     */
    public void setScript(String arg0);
    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setType(java.lang.String)
     */
    public void setType(String arg0);
    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setUrl(java.lang.String)
     */
    public void setUrl(String arg0);
    
}

