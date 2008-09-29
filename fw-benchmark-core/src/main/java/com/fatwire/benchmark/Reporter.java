/**
 * 
 */
package com.fatwire.benchmark;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.fatwire.benchmark.BenchmarkStatistics.UriStat;

class Reporter {
    /**
     * 
     */
    private final BenchmarkStatistics benchmarkStatistics;

    private PrintWriter pw;

    /**
     * @param pw
     * @param benchmarkStatistics TODO
     * @param statistics 
     */
    public Reporter(BenchmarkStatistics benchmarkStatistics,
            final PrintWriter pw) {
        super();
        this.benchmarkStatistics = benchmarkStatistics;
        this.pw = pw;
    }

    public void reportTotals() {
        reportTotals(this.benchmarkStatistics.total);
    }

    public void reportUris() {
        pw.println();
        int maxUriSize = 0;
        for (final URI u : this.benchmarkStatistics.urlStat.keySet()) {
            maxUriSize = Math.max(maxUriSize, u.toString().length());
        }

        pw.println(padded("uri", maxUriSize)
                + "\tcount\tmean\tbytes mean\t304s\tcloses\texceptions");
        for (final Map.Entry<URI, UriStat> entry : new TreeMap<URI, UriStat>(
                this.benchmarkStatistics.urlStat).entrySet()) {
            pw.print(padded(entry.getKey().toString(), maxUriSize));
            pw.print('\t');
            report(entry.getValue());
            pw.println();
        }
        pw.flush();
    }

    public void reportStatus() {
        pw.println();
        final int maxSize = "Response status".length();

        pw.println(padded("Response status", maxSize) + "\tcount");
        for (final Map.Entry<Integer, AtomicInteger> entry : this.benchmarkStatistics.total.statusCounter
                .entrySet()) {
            pw.print(padded(entry.getKey().toString(), maxSize));
            pw.print('\t');
            pw.print(entry.getValue());
            pw.println();
        }
        pw.flush();
    }

    private String padded(final String s, final int size) {
        final char[] pad = new char[size + 5 - s.length()];
        Arrays.fill(pad, ' ');
        return new StringBuilder(s).append(pad).toString();

    }

    private void report(final UriStat s) {
        int count304 = 0;
        count304 = s.statusCounter.get(304).get();

        pw.print(String.format("%1$5d\t%2$4.2f\t%3$8.2f\t%4$5d\t%5$5d\t%6$5d",
                s.perf.getN(), s.perf.getMean(), s.bytes.getMean(), count304,
                s.closes.get(), s.exceptionCounter.get()));
        pw.flush();
    }

    private void reportTotals(final UriStat s) {

        pw.println(String.format("Count:                          %1$10d",
                s.perf.getN()));
//        pw.println(String.format("Total download time:            %1$10.3f ms",
//                s.perf.getSum()));

        pw.println(String.format(
                "Bytes Read:                     %1$10.3e bytes", s.bytes
                        .getSum()));
        pw.println(String.format(
                "Average Bytes Read:             %1$10.2f bytes", s.bytes
                        .getMean()));
        pw.println(String.format("Mean Time per request:          %1$10.2f ms",
                s.perf.getMean()));
        pw.println(String.format("Min Time per request:           %1$10.2f ms",
                s.perf.getMin()));
        pw.println(String.format("Max Time per request:           %1$10.2f ms",
                s.perf.getMax()));
        pw.println(String.format("Std dev:                        %1$10.2f ms",
                s.perf.getStandardDeviation()));
        /*
        pw.println(String.format("Skewness:                       %1$10.2f",
                s.perf.getSkewness()));
        pw.println(String.format("Kurtosis:                       %1$10.2f",
                s.perf.getKurtosis()));
        */
        pw.println(String.format("Connection \'close\' responses: %1$10d",
                s.closes.get()));
        pw.println(String.format("Exceptions reading body:        %1$10d",
                s.exceptionCounter.get()));
        pw.flush();
    }

}