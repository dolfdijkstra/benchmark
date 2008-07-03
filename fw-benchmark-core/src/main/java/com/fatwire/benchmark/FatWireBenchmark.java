package com.fatwire.benchmark;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;

import com.fatwire.benchmark.connectionmanager.BenchmarkHttpConnectionManager;
import com.fatwire.benchmark.connectionmanager.PoolManager;
import com.fatwire.benchmark.session.Page;
import com.fatwire.benchmark.session.RandomScriptFactory;
import com.fatwire.benchmark.session.Script;
import com.fatwire.benchmark.session.ScriptFactory;
import com.fatwire.benchmark.session.SimpleScript;
import com.fatwire.benchmark.session.SimpleScriptFactory;
import com.fatwire.benchmark.util.TrafficLight;

public class FatWireBenchmark {

    private BenchmarkHttpConnectionManager connectionManager;

    private PoolManager poolManager;

    private Condition cond;

    private int numberOfWorkers = 1;

    private long delay = 0;

    private int max = 1;

    private long startTime;

    private Script script;

    private String filename;

    private String url;

    private String type = "simple";

    private final BenchmarkStatistics stat = new BenchmarkStatistics();


    private long endTime;

    public void init() throws Exception {
        if (filename != null) {
            if ("simple".equals(getType())) {
                ScriptFactory sf = new SimpleScriptFactory(filename, delay);
                script = sf.getScript();
            } else if ("random".equals(getType())) {
                ScriptFactory sf = new RandomScriptFactory(filename, delay);
                script = sf.getScript();
            } else {
                throw new IllegalArgumentException("unknown script type.("
                        + getType() + ")");
            }
        } else {
            Page p = new Page(URI.create(url));
            p.setReadTime(delay);
            script = new SimpleScript(Arrays.asList(new Page[] { p }));
        }

        poolManager = new PoolManager(5);
        connectionManager = new BenchmarkHttpConnectionManager(poolManager);
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(
                getNumberOfWorkers() + 5);
        connectionManager.getParams().setMaxTotalConnections(
                getNumberOfWorkers() + 5);
        
        cond = new CounterConditional(max);
        stat.init();

    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(final String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("needs an argument");
            return;
        }
        final FatWireBenchmark benchmark = new FatWireBenchmark();
        for (int i = 0; i < args.length - 1; i++) {
            if ("-c".equals(args[i])) {
                benchmark.setNumberOfWorkers(Integer.parseInt(args[++i]));
            } else if ("-n".equals(args[i])) {
                benchmark.setMax(Integer.parseInt(args[++i]));
            } else if ("-d".equals(args[i])) {
                benchmark.setDelay(Integer.parseInt(args[++i]));
            } else if ("-url".equals(args[i])) {
                benchmark.setUrl(args[++i]);
            } else if ("-script".equals(args[i])) {
                benchmark.setScript(args[++i]);
            } else if ("-type".equals(args[i])) {
                benchmark.setType(args[++i]);

            }
        }
        final Thread hook = new Thread(new Runnable() {

            public void run() {
                System.out.println("shutting down");
                benchmark.shutdown();
            }

        });
        Runtime.getRuntime().addShutdownHook(hook);
        benchmark.init();
        benchmark.go();
        benchmark.shutdown();
        Runtime.getRuntime().removeShutdownHook(hook);
    }

    public void shutdown() {
        doShutdown();


    }

    protected void doShutdown() {
        stat.shutdown();
        connectionManager.shutdown();
        poolManager.shutdown();

    }

    public void setUrl(final String uri) {
        url = uri;

    }

    public void setScript(final String script) {
        this.filename = script;

    }

    public void go() throws InterruptedException {
        final TrafficLight light = new TrafficLight();
        startTime = System.currentTimeMillis();
        light.turnGreen();

        final HttpMethodListener statListener = new StatisticsListener(stat);
        final ResponseStatusListener statusListener = new ResponseStatusListener();
        final Thread[] t = new Thread[numberOfWorkers];
        final HttpWorker[] workers = new HttpWorker[numberOfWorkers];
        for (int i = 0; i < t.length; i++) {

            final HttpWorker worker = new HttpWorker("agent-" + i,
                    connectionManager, script, light, cond);
            worker.addListener(statListener);
            worker.addListener(statusListener);
            workers[i] = worker;

        }

        for (int i = 0; i < t.length; i++) {
            if (i > 0) {
                Thread.sleep(100);
            }
            t[i] = new Thread(workers[i], workers[i].getName());
            t[i].start();
        }
        for (int i = 0; i < t.length; i++) {
            t[i].join();
        }
        endTime = System.currentTimeMillis();
        writeReport();

    }

    protected void writeReport() {
        final PrintStream pw = System.out;
        pw.println(); //the progress bar needs a new line
        pw.println(String.format("Total number of started requests: %1$10d",
                cond.getNum()));

        pw.println(String.format(
                "Time taken for tests:             %1$10.2f s",
                (endTime - startTime) / 1000D));
        if (delay > 0) {
            pw.println(String.format(
                    "Delay:                            %1$10d ms", delay));
        }
        pw.println(String.format("Concurrency Level:                %1$10d",
                numberOfWorkers));
        pw.println();
        stat.report(pw);
    }

    /**
     * @return the cond
     */
    public Condition getCond() {
        return cond;
    }

    public void setMax(final int max) {
        this.max = max;

    }

    /**
     * @return the numberOfWorkers
     */
    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    /**
     * @param numberOfWorkers the numberOfWorkers to set
     */
    public void setNumberOfWorkers(final int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    /**
     * @return the delay
     */
    public long getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(final long delay) {
        this.delay = delay;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
}
