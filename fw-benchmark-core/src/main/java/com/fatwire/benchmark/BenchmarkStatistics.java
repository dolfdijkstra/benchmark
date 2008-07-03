package com.fatwire.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SynchronizedDescriptiveStatistics;

import com.fatwire.benchmark.util.FactoryMap;

public class BenchmarkStatistics {

    private static class UriStat {
        private final DescriptiveStatistics perf = new SynchronizedDescriptiveStatistics();

        private final DescriptiveStatistics bytes = new SynchronizedDescriptiveStatistics();

        private final FactoryMap<Integer, AtomicInteger> statusCounter = new FactoryMap<Integer, AtomicInteger>(
                new TreeMap<Integer, AtomicInteger>(),
                new FactoryMap.Factory<Integer, AtomicInteger>() {

                    public AtomicInteger create(Integer t) {
                        return new AtomicInteger();
                    }
                });

        private final AtomicInteger exceptionCounter = new AtomicInteger();

        private final AtomicInteger closes = new AtomicInteger();

        private void incrementStatus(final int status) {
            final AtomicInteger s = statusCounter.get(status);
            s.incrementAndGet();
        }

        public void incrementException() {
            exceptionCounter.incrementAndGet();

        }

        public int getExceptionCount() {
            return exceptionCounter.get();
        }

        public void addDownloadTime(final long time) {
            perf.addValue(time);

        }

        public void addDownloadedBytes(final long byteCount) {
            if (byteCount > 0) {
                bytes.addValue(byteCount);
            }

        }

        public void incrementConnectionClosed() {
            closes.incrementAndGet();
        }

        public void incrementForTransactionPoint(TransactionPoint tp) {
            addDownloadTime(tp.getElapsed());
            addDownloadedBytes(tp.getBodySize());
            incrementStatus(tp.getStatus());
            if (tp.isHasException()) {
                incrementException();
            }
            if (tp.isClosed()) {
                incrementConnectionClosed();
            }

        }

    }

    private final UriStat total = new UriStat();

    private UriStat timed = new UriStat();

    private final Map<URI, UriStat> urlStat = new ConcurrentHashMap<URI, UriStat>();

    private final ConcurrentLinkedQueue<TransactionPoint> history = new ConcurrentLinkedQueue<TransactionPoint>();

    private Timer timer;

    private File intervalStatFile;
    private File historyFile;
    private AtomicInteger concurrencyCounter = new AtomicInteger();

    private class TimedPrinterTask extends TimerTask {



        public void run() {
            final UriStat t = timed;
            timed = new UriStat();
            report(t);
        }

        private void report(final UriStat s) {
            int count304 = 0;
            count304 = s.statusCounter.get(304).get();
            Writer pw = null;
            try {
                pw = new FileWriter(intervalStatFile, true);
                pw.write(Long.toString(System.currentTimeMillis()));
                pw.write('\t');
                pw.write(Integer.toString(concurrencyCounter.get()));
                pw.write('\t');
                pw.write(String.format(
                        "%1$5d\t%2$4.2f\t%3$8.2f\t%4$5d\t%5$5d\t%6$5d", s.perf
                                .getN(), s.perf.getMean(), s.bytes.getMean(),
                        count304, s.closes.get(), s.exceptionCounter.get()));
                pw.write('\r');
                pw.write('\n');

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (pw != null) {
                    try {
                        pw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void init() {
        timer = new Timer(true);
        this.intervalStatFile= new File("interval-stat.log");
        if (intervalStatFile.exists()){
            intervalStatFile.delete();
        }

        this.historyFile = new File("history.log");
        if (historyFile.exists()){
            historyFile.delete();
        }
        timer.scheduleAtFixedRate(new TimedPrinterTask(),
                5000L, 5000L);

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                writeHistory();

            }

        }, 1000L, 1000L);

    }

    public void shutdown() {
        writeHistory();
        timer.cancel();

    }

    private static class TransactionPoint {
        final long timeStamp;

        final URI uri;

        final int status;

        final long elapsed;

        final int bodySize;

        boolean closed = false;

        boolean hasException = false;

        TransactionPoint(final HttpTransaction httpTransaction) {
            this.timeStamp = httpTransaction.getStartTime();
            uri = httpTransaction.getRequest().getUri();
            status = httpTransaction.getResponse().getStatusCode();
            elapsed = httpTransaction.getDownloadTime();
            bodySize = httpTransaction.getResponse().getBody().length;

            for (final Header header : httpTransaction.getResponse()
                    .getHeaders()) {
                if ("Connection".equalsIgnoreCase(header.getName())) {
                    if ("close".equalsIgnoreCase(header.getValue())) {
                        closed = true;
                    }
                    break;
                }
            }
            if (httpTransaction.getResponse().getException() != null) {
                hasException = true;
            }

        }

        /**
         * @return the bodySize
         */
        public int getBodySize() {
            return bodySize;
        }

        /**
         * @return the closed
         */
        public boolean isClosed() {
            return closed;
        }

        /**
         * @return the elapsed
         */
        public long getElapsed() {
            return elapsed;
        }

        /**
         * @return the hasException
         */
        public boolean isHasException() {
            return hasException;
        }

        /**
         * @return the status
         */
        public int getStatus() {
            return status;
        }

        /**
         * @return the timeStamp
         */
        public long getTimeStamp() {
            return timeStamp;
        }

        /**
         * @return the uri
         */
        public URI getUri() {
            return uri;
        }

    }

    public void finished(final HttpTransaction httpTransaction) {
        concurrencyCounter.decrementAndGet();
        TransactionPoint tp = new TransactionPoint(httpTransaction);

        final UriStat s = get(tp.getUri());
        s.incrementForTransactionPoint(tp);
        total.incrementForTransactionPoint(tp);
        timed.incrementForTransactionPoint(tp);
        this.history.add(tp);

    }

    void writeHistory() {

        TransactionPoint tp = null;
        Writer w = null;
        try {
            w = new FileWriter(historyFile, true);
            while ((tp = history.poll()) != null) {
                w.write(Long.toString(tp.getTimeStamp()));
                w.write('\t');
                w.write(Long.toString(tp.getElapsed()));
                w.write('\t');
                w.write(Integer.toString(tp.getStatus()));
                w.write('\t');
                w.write(Integer.toString(tp.getBodySize()));
                w.write('\t');
                if (tp.getUri() != null) {
                    w.write(tp.getUri().toASCIIString());
                } else {

                }
                w.write('\r');
                w.write('\n');

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private UriStat get(final URI uri) {
        UriStat s = urlStat.get(uri);
        if (s == null) {
            s = new UriStat();
            urlStat.put(uri, s);
        }
        return s;
    }

    public synchronized void report(final PrintStream pw) {
        new Reporter(pw).report();
    }

    private class Reporter {
        private PrintStream pw;

        /**
         * @param pw
         * @param statistics 
         */
        public Reporter(final PrintStream pw) {
            super();
            this.pw = pw;
        }

        public void report() {
            reportTotals(total);
            reportStatus();
            pw.println();
            int maxUriSize = 0;
            for (final URI u : urlStat.keySet()) {
                maxUriSize = Math.max(maxUriSize, u.toString().length());
            }

            pw.println(padded("uri", maxUriSize)
                    + "\tcount\tmean\tbytes mean\t304s\tcloses\texceptions");
            for (final Map.Entry<URI, UriStat> entry : new TreeMap<URI, UriStat>(
                    urlStat).entrySet()) {
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
            for (final Map.Entry<Integer, AtomicInteger> entry : total.statusCounter
                    .entrySet()) {
                pw.print(padded(entry.getKey().toString(), maxSize));
                pw.print('\t');
                pw.print(entry.getValue());
                pw.println();
            }
        }

        private String padded(final String s, final int size) {
            final char[] pad = new char[size + 5 - s.length()];
            Arrays.fill(pad, ' ');
            return new StringBuilder(s).append(pad).toString();

        }

        private void report(final UriStat s) {
            int count304 = 0;
            count304 = s.statusCounter.get(304).get();

            pw.print(String.format(
                    "%1$5d\t%2$4.2f\t%3$8.2f\t%4$5d\t%5$5d\t%6$5d", s.perf
                            .getN(), s.perf.getMean(), s.bytes.getMean(),
                    count304, s.closes.get(), s.exceptionCounter.get()));

        }

        private void reportTotals(final UriStat s) {

            pw.println(String.format("Count:                          %1$10d",
                    s.perf.getN()));
            pw.println(String.format(
                    "Total download time:            %1$10.3f ms", s.perf
                            .getSum()));

            pw.println(String.format(
                    "Bytes Read:                     %1$10.3e bytes", s.bytes
                            .getSum()));
            pw.println(String.format(
                    "Average Bytes Read:             %1$10.2f bytes", s.bytes
                            .getMean()));
            pw.println(String.format(
                    "Mean Time per request:          %1$10.2f ms", s.perf
                            .getMean()));
            pw.println(String.format(
                    "Min Time per request:           %1$10.2f ms", s.perf
                            .getMin()));
            pw.println(String.format(
                    "Max Time per request:           %1$10.2f ms", s.perf
                            .getMax()));
            pw.println(String.format(
                    "Std dev:                        %1$10.2f ms", s.perf
                            .getStandardDeviation()));
            pw.println(String.format(
                    "Skewness:                       %1$10.2f", s.perf
                            .getSkewness()));
            pw.println(String.format(
                    "Kurtosis:                       %1$10.2f", s.perf
                            .getKurtosis()));
            pw.println(String.format("Connection \'close\' responses: %1$10d",
                    s.closes.get()));
            pw.println(String.format("Exceptions reading body:        %1$10d",
                    s.exceptionCounter.get()));

        }

    }

    public void start(long startTime) {
        concurrencyCounter.incrementAndGet();

    }
}