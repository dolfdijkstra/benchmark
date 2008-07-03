package com.fatwire.benchmark;

public class StatisticsListener implements HttpMethodListener {
    private final BenchmarkStatistics stats;

    /**
     * @param stats
     */
    public StatisticsListener(final BenchmarkStatistics stats) {
        super();
        this.stats = stats;
    }

    public void executePerformed(RequestProcessedEvent event) {
        stats.finished(event.getHttpTransaction());

    }

    public void beforeExecute(RequestStartingEvent event) {
        stats.start(event.getStartTime());
        
    }

}
