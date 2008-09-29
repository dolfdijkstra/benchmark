package com.fatwire.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.SynchronizedSummaryStatistics;

import com.fatwire.benchmark.util.FactoryMap;

public class BenchmarkStatistics {

    static class UriStat {
        final SynchronizedSummaryStatistics perf = new SynchronizedSummaryStatistics();

        final SynchronizedSummaryStatistics bytes = new SynchronizedSummaryStatistics();

        final FactoryMap<Integer, AtomicInteger> statusCounter = new FactoryMap<Integer, AtomicInteger>(
                new TreeMap<Integer, AtomicInteger>(),
                new FactoryMap.Factory<Integer, AtomicInteger>() {

                    public AtomicInteger create(Integer t) {
                        return new AtomicInteger();
                    }
                });

        final AtomicInteger exceptionCounter = new AtomicInteger();

        final AtomicInteger closes = new AtomicInteger();

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

    final UriStat total = new UriStat();

    private UriStat timed = new UriStat();

    final Map<URI, UriStat> urlStat = new ConcurrentHashMap<URI, UriStat>();

    private final ConcurrentLinkedQueue<TransactionPoint> history = new ConcurrentLinkedQueue<TransactionPoint>();

    private Timer timer;

    

    private File historyFile;

    private AtomicInteger concurrencyCounter = new AtomicInteger();

    private AtomicLong requestCounter = new AtomicLong();

    private final File reportDirectory;

    private long startTime;

    private long endTime;

    private class IntervalTask extends TimerTask {
        private final File intervalStatFile;
        
        /**
         * @param intervalStatFile
         */
        public IntervalTask(File intervalStatFile) {
            super();
            this.intervalStatFile = intervalStatFile;
            if (intervalStatFile.exists()) {
                intervalStatFile.delete();
            }

        }

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
                pw.write(Long.toString(requestCounter.get()));
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

    /**
     * @param reportDirectory
     */
    public BenchmarkStatistics(final File reportDirectory) {
        super();
        if (reportDirectory.exists() && reportDirectory.isDirectory()) {
            this.reportDirectory = reportDirectory;
        } else {
            throw new IllegalArgumentException(reportDirectory
                    .getAbsoluteFile()
                    + " does not exist or is not a directory ");
        }
    }

    public void init() {
        this.startTime = System.currentTimeMillis();
        timer = new Timer(true);

        timer.scheduleAtFixedRate(new IntervalTask(new File(reportDirectory, "interval-stat.log")), 5000L, 5000L);
        
        this.historyFile = new File(reportDirectory, "history.log");
        if (historyFile.exists()) {
            historyFile.delete();
        }


        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                writeHistory();

            }

        }, 1000L, 1000L);
        consumerThread = new Thread(new TransactionConsumer());
        consumerThread.start();

    }

    private Thread consumerThread;

    public void shutdown() {
        this.endTime = System.currentTimeMillis();
        writeHistory();
        timer.cancel();
        consumerThread.interrupt();

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

    private final BlockingQueue<TransactionPoint> myQueue = new LinkedBlockingQueue<TransactionPoint>();

    private final Log log = LogFactory.getLog(this.getClass());

    public void finished(final HttpTransaction httpTransaction) {
        concurrencyCounter.decrementAndGet();
        TransactionPoint tp = new TransactionPoint(httpTransaction);
        if (!myQueue.offer(tp)) {
            log.warn("Could not add TransactionPoint to the queue");
        }

    }

    class TransactionConsumer implements Runnable {
double counter=0;
long totalTime=0;
        public void run() {
            boolean c=true;
            while (c) {
                TransactionPoint tp;
                try {
                    long t = System.nanoTime();
                    tp = myQueue.take();
                    //history.add(tp);
                    total.incrementForTransactionPoint(tp);
                    timed.incrementForTransactionPoint(tp);
                    final UriStat s = get(tp.getUri());
                    s.incrementForTransactionPoint(tp);
                    counter++;
                    totalTime = (System.nanoTime()-t)/1000;
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    if (counter >0){
                     System.out.println(totalTime/counter);   
                    }
                    c=false;
                }
            }

        }

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

    public void start(long startTime) {
        concurrencyCounter.incrementAndGet();
        this.requestCounter.incrementAndGet();
    }

    public int getCurrentConcurrencyLevel() {
        return concurrencyCounter.get();
    }

    public long getRequestCount() {
        return this.requestCounter.get();
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return the endTime
     */
    public long getEndTime() {
        return endTime;
    }

}