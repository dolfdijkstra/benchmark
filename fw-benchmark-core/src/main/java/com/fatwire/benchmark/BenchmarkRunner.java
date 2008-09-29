package com.fatwire.benchmark;

import java.io.File;

import com.fatwire.benchmark.script.Script;

public interface BenchmarkRunner {
    /** The Plexus role identifier. */
    String ROLE = BenchmarkRunner.class.getName();

    void init() throws Exception;

    void go() throws InterruptedException;

    void shutdown();


    /**
     * @param script
     */
    public void setScript(Script script);

    /**
     * @param file
     */
    public void setReportDirectory(File file);


    public boolean isUserCache();

    /**
     * @param userCache the userCache to set
     */
    public void setUserCache(boolean userCache);

    public void setSchedule(Schedule schedule);

}
