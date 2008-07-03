package com.fatwire.benchmark.plexus;

import java.io.IOException;

import com.fatwire.benchmark.FatWireBenchmark;

public class DefaultBenchmarkRunner implements BenchmarkRunner {
    final private FatWireBenchmark delegate;

    public DefaultBenchmarkRunner() {
        delegate = new FatWireBenchmark();
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @return
     * @see com.fatwire.benchmark.FatWireBenchmark#getDelay()
     */
    public long getDelay() {
        return delegate.getDelay();
    }

    /**
     * @return
     * @see com.fatwire.benchmark.FatWireBenchmark#getNumberOfWorkers()
     */
    public int getNumberOfWorkers() {
        return delegate.getNumberOfWorkers();
    }

    /**
     * @return
     * @see com.fatwire.benchmark.FatWireBenchmark#getType()
     */
    public String getType() {
        return delegate.getType();
    }

    /**
     * @throws InterruptedException
     * @see com.fatwire.benchmark.FatWireBenchmark#go()
     */
    public void go() throws InterruptedException {
        delegate.go();
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @throws Exception
     * @see com.fatwire.benchmark.FatWireBenchmark#init()
     */
    public void init() throws Exception {
        delegate.init();
    }

    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setDelay(long)
     */
    public void setDelay(long arg0) {
        delegate.setDelay(arg0);
    }

    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setMax(int)
     */
    public void setMax(int arg0) {
        delegate.setMax(arg0);
    }

    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setNumberOfWorkers(int)
     */
    public void setNumberOfWorkers(int arg0) {
        delegate.setNumberOfWorkers(arg0);
    }

    /**
     * @param arg0
     * @throws IOException
     * @see com.fatwire.benchmark.FatWireBenchmark#setScript(java.lang.String)
     */
    public void setScript(String arg0)  {
        delegate.setScript(arg0);
    }

    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setType(java.lang.String)
     */
    public void setType(String arg0) {
        delegate.setType(arg0);
    }

    /**
     * @param arg0
     * @see com.fatwire.benchmark.FatWireBenchmark#setUrl(java.lang.String)
     */
    public void setUrl(String arg0) {
        delegate.setUrl(arg0);
    }

    /**
     * 
     * @see com.fatwire.benchmark.FatWireBenchmark#shutdown()
     */
    public void shutdown() {
        delegate.shutdown();
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }
}
