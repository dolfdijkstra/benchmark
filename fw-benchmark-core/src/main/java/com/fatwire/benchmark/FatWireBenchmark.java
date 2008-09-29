package com.fatwire.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fatwire.benchmark.connectionmanager.BenchmarkHttpConnectionManager;
import com.fatwire.benchmark.connectionmanager.PoolManager;
import com.fatwire.benchmark.script.Script;
import com.fatwire.benchmark.session.NullUserAgentCache;
import com.fatwire.benchmark.session.UserAgentCacheImpl;
import com.fatwire.benchmark.util.TrafficLight;

public class FatWireBenchmark implements BenchmarkRunner {
    Log log = LogFactory.getLog(this.getClass());

    private HttpConnectionManager connectionManager;

    private long startTime;

    private Script script;

    private File reportDirectory;

    private BenchmarkStatistics stat;

    private Schedule schedule;

    private WorkerManager workManager;

    private long endTime;

    private Condition cond;

    /**
     * flag for user user-agent caching
     */
    private boolean userCache = true;

    public void init() throws Exception {
        stat.init();

    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(final String[] args) throws Exception {

        Options options = CommandLineUtils.getOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        if (args.length == 0 || cmd.hasOption('h')) {
            CommandLineUtils.showUsage(options);
            System.exit(1);
        }
        Condition cond = new CounterConditional(Integer.MAX_VALUE);
        
        final FatWireBenchmark benchmark = new FatWireBenchmark();
        CommandLineUtils.parseCommandLine(cmd, benchmark);
        
        final PoolManager poolManager = new PoolManager(5);
        final BenchmarkHttpConnectionManager connectionManager = new BenchmarkHttpConnectionManager(
                poolManager);
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(
                benchmark.getSchedule().getPeakConcurrency() + 5);
        connectionManager.getParams().setMaxTotalConnections(
                benchmark.getSchedule().getPeakConcurrency() + 5);
        benchmark.setConnectionManager(connectionManager) ;
        benchmark.setCond(cond);
        
        final Thread hook = new Thread(new Runnable() {

            public void run() {
                System.out.println("shutting down");
                benchmark.shutdown();
                connectionManager.shutdown();
                poolManager.shutdown();
            }

        });
        Runtime.getRuntime().addShutdownHook(hook);
        benchmark.init();
        benchmark.go();
        Runtime.getRuntime().removeShutdownHook(hook);
        hook.start();
        hook.join();
        System.exit(0);

    }

    public void shutdown() {
        doShutdown();

    }

    protected void doShutdown() {
        stat.shutdown();

        
    }

    public void go() throws InterruptedException {

        final TrafficLight light = new TrafficLight();
        startTime = System.currentTimeMillis();
        printSession();
        light.turnGreen();
        final HttpMethodListener statListener = new StatisticsListener(stat);
        final ResponseStatusListener statusListener = new ResponseStatusListener();

        workManager.setFactory(new WorkerManager.WorkerFactory() {
            int i = 0;

            public HttpWorker createWorker() {
                
                final HttpWorker worker = new HttpWorker("agent-" + i,
                        connectionManager, script, light, cond,
                        userCache ? new UserAgentCacheImpl()
                                : new NullUserAgentCache());
                worker.addListener(statListener);
                worker.addListener(statusListener);
                i++;
                return worker;

            }

        });
        workManager.setCond(cond);
        workManager.run();
        workManager.shutdown();
        endTime = System.currentTimeMillis();
        writeReport();

    }

    protected void printSession() {
        StringBuilder sw = new StringBuilder();
        sw.append("Now:").append(new Date().toString()).append("\r\n");
        sw.append("StartTime:").append(this.startTime).append("\r\n");
        //sw.append("Host:").append(this.host).append("\r\n");
        sw.append("Script:").append(script.toString()).append("\r\n");
        /*
        if (this.url == null) {
            sw.append("ScriptName:").append(this.filename).append("\r\n");
            sw.append("Type:").append(this.type).append("\r\n");
        } else {
            sw.append("URL:").append(this.url).append("\r\n");
        }
        */
        sw.append("Schedule:").append(schedule.toString()).append("\r\n");
        /*
        sw.append("NumberOfWorkers:").append(this.numberOfWorkers).append(
                "\r\n");

        sw.append("Max:").append(this.max).append("\r\n");
        sw.append("RampUp:").append(this.rampup).append("\r\n");
        sw.append("Delay:").append(this.delay).append("\r\n");
        */
        sw.append("UserCache:").append(this.userCache).append("\r\n");

        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(getReportDirectory(), "session.txt"));
            fw.write(sw.toString());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    protected void writeReport() {
        StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        System.out.println(); //the progress bar needs a new line
        pw.println(String.format("Total number of started requests: %1$10d",
                cond.getNum()));

        pw.println(String.format("Time taken for tests:             %1$10d s",
                (endTime - startTime) / 1000L));
        pw.println(String.format(
                "Average http req per second:      %1$10.2f t/s", (cond
                        .getNum() / ((endTime - startTime) / 1000D))));

        if (script.getDefaultDelay() > 0) {
            pw.println(String.format(
                    "Delay:                            %1$10d ms", script
                            .getDefaultDelay()));
        }
        pw.println(String.format("Concurrency Level:                %1$10d",
                schedule.getPeakConcurrency()));
        pw.println();

        Reporter rep = new Reporter(stat, pw);
        rep.reportTotals();
        rep.reportStatus();
        //print to std out
        pw.flush();
        System.out.println(sw.toString());
        rep.reportUris();
        pw.flush();
        //and write to file
        FileWriter fw = null;
        try {
            fw = new FileWriter(
                    new File(getReportDirectory(), "statistics.txt"));
            fw.write(sw.toString());

        } catch (Exception e) {
            log.error(e, e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }

    }

    /**
     * @return the cond
     */
    public Condition getCond() {
        return cond;
    }

    /**
     * @return the userCache
     */
    public boolean isUserCache() {
        return userCache;
    }

    /**
     * @param userCache the userCache to set
     */
    public void setUserCache(boolean userCache) {
        this.userCache = userCache;
    }

    /**
     * @return the schedule
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * @param schedule the schedule to set
     */
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * @return the connectionManager
     */
    public HttpConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * @param connectionManager the connectionManager to set
     */
    public void setConnectionManager(
            HttpConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * @return the script
     */
    public Script getScript() {
        return script;
    }

    /**
     * @param script the script to set
     */
    public void setScript(Script script) {
        this.script = script;
    }

    /**
     * @return the stat
     */
    public BenchmarkStatistics getStat() {
        return stat;
    }

    /**
     * @param stat the stat to set
     */
    public void setStat(BenchmarkStatistics stat) {
        this.stat = stat;
    }

    /**
     * @return the workManager
     */
    public WorkerManager getWorkManager() {
        return workManager;
    }

    /**
     * @param workManager the workManager to set
     */
    public void setWorkManager(WorkerManager workManager) {
        this.workManager = workManager;
    }

    /**
     * @param cond the cond to set
     */
    public void setCond(Condition cond) {
        this.cond = cond;
    }

    /**
     * @return the reportDirectory
     */
    public File getReportDirectory() {
        return reportDirectory;
    }

    /**
     * @param reportDirectory the reportDirectory to set
     */
    public void setReportDirectory(File reportDirectory) {
        this.reportDirectory = reportDirectory;
    }
}
