package com.fatwire.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fatwire.benchmark.util.TimePrecision;
import com.fatwire.benchmark.util.TimeUtil;

public class WorkerManager implements Runnable {
    private final Log log = LogFactory.getLog(this.getClass());

    interface WorkerFactory {

        HttpWorker createWorker();
    }

    interface WorkerListener {

        void started(HttpWorker worker);

        void finished(HttpWorker worker);
    }

    private Schedule schedule;

    private Condition cond;

    private WorkerFactory factory;

    private ThreadPoolExecutor workerPool;

    private final AtomicInteger runningWorkers = new AtomicInteger();

    private final WorkerListener listener = new WorkerListener() {

        public void finished(HttpWorker worker) {
            runningWorkers.decrementAndGet();
        }

        public void started(HttpWorker worker) {
            runningWorkers.incrementAndGet();

        }

    };

    List<HttpWorker> workers = new ArrayList<HttpWorker>();

    public void run() {
        bootPool();
        int l = 0;
        if (!workers.isEmpty()) {
            throw new IllegalStateException("workers not empty on startup.");
        }
        Leg old = new Leg(0, 0, 0, 0);
        while (cond.isTrue() && l < schedule.getLegs().length) {
            Leg leg = schedule.getLegs()[l];
            log.debug("leg[" + l + "]: " + leg);

            int increase = leg.getNumberOfWorkers() - old.getNumberOfWorkers();

            log.debug("increase: " + increase);
            if (increase != 0) {

                long sleepTimeBetweenSteps = 0;

                if (leg.getStepSize() > 0) {
                    sleepTimeBetweenSteps = Math.abs((leg.getGrowthTime() * leg
                            .getStepSize())
                            / increase);
                    //log.debug("steps: " + steps);
                }

                for (int i = 0; i < Math.abs(increase); i++) {

                    if (increase > 0) {
                        HttpWorker worker = factory.createWorker();
                        workers.add(worker);
                        startWorker(worker);
                    } else {
                        HttpWorker worker = workers.remove(0);
                        stopWorker(worker);
                    }

                    if (sleepTimeBetweenSteps > 0
                            && i % leg.getStepSize() == (leg.getStepSize() - 1)) {
                        sleep(sleepTimeBetweenSteps);
                    }
                }

            }
            sleep(leg.getHoldPeriod());
            l++;
            old = leg;
        }

    }

    void bootPool() {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (Leg leg : this.schedule.getLegs()) {
            min = Math.min(min, leg.getNumberOfWorkers());
            max = Math.max(min, leg.getNumberOfWorkers());
        }
        workerPool = new ThreadPoolExecutor(min, max, 5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                    int num = 0;

                    public Thread newThread(Runnable r) {
                        return new Thread(r, "UserWorker-" + (num++));
                    }

                });
        workerPool.prestartAllCoreThreads();

    }

    private void sleep(long t) {
        if (t < 1)
            return;
        try {
            //log.trace("sleep: " + t);
            Thread.sleep(t);
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
            shutdown();
        }
    }

    private void startWorker(HttpWorker worker) {
        log.debug("started worker " + worker);
        worker.addListener(listener);
        workerPool.execute(worker);

    }

    private void stopWorker(HttpWorker worker) {
        log.debug("stopping worker " + worker);
        worker.stop();

    }

    protected void shutdown() {
        for (HttpWorker worker : workers) {
            stopWorker(worker);
        }

        workerPool.shutdown();
        while (runningWorkers.get() > 0) {
            log.trace("running workers: " +runningWorkers.get());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
                return;
            }
        }

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
     * @return the cond
     */
    public Condition getCond() {
        return cond;
    }

    /**
     * @param cond the cond to set
     */
    public void setCond(Condition cond) {
        this.cond = cond;
    }

    /**
     * @return the factory
     */
    public WorkerFactory getFactory() {
        return factory;
    }

    /**
     * @param factory the factory to set
     */
    public void setFactory(WorkerFactory factory) {
        this.factory = factory;
    }

}

/**
 * 
 * Holds a number of legs
 * 
 * 
 * @author Dolf.Dijkstra
 * @since Sep 20, 2008
 */

class Schedule {
    private final Leg[] legs;

    /**
     * @return the legs
     */
    public Leg[] getLegs() {
        return legs;
    }

    /**
     * @param legs
     */
    public Schedule(Leg[] legs) {
        super();
        this.legs = legs;
    }

    /**
     * 
     * 
     * @param s in the form '5:2m:200:2m' 
     * @return
     */

    protected static Schedule parse(String s) {
        //"5:2m:200:2m"

        String[] l = s.split(";");
        Leg[] legs = new Leg[l.length];
        for (int i = 0; i < l.length; i++) {
            String[] p = l[i].split(":");
            if (p.length != 4)
                throw new IllegalArgumentException(
                        "number of parts should be 4 from " + l[i]);
            int steps = Integer.parseInt(p[0]);
            int growTime = TimeUtil.parseTime(p[1]) * 1000;
            int numWorkers = Integer.parseInt(p[2]);
            int holdPeriod = TimeUtil.parseTime(p[3]) * 1000;
            legs[i] = new Leg(growTime, steps, numWorkers, holdPeriod);

        }
        /*
        if (legs[legs.length - 1].getNumberOfWorkers() != 0) {
            Leg[] leg2 = new Leg[legs.length+1];
            
        }
        */
        return new Schedule(legs);
    }

    public int getPeakConcurrency() {
        int peak = 0;
        for (Leg l : legs) {
            peak = Math.max(peak, l.getNumberOfWorkers());
        }

        return peak;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Leg l : legs) {
            if (!first) {
                sb.append(";");
                first = false;
            }
            sb.append(l.getStepSize()).append(":");
            sb.append(TimePrecision.MILLISECOND.toString(l.getGrowthTime())).append(":");
            sb.append(l.getNumberOfWorkers()).append(":");
            sb.append(TimePrecision.MILLISECOND.toString(l.getHoldPeriod())).append(":");

        }

        return sb.toString();
    }

}

/**
 * A leg (like in a leg of a race) that describes the number of steps 
 * and in which time the workers grow (or decrease) to a certain level 
 * and how long they stay on that level. 
 * 
 * @author Dolf.Dijkstra
 * @since Sep 20, 2008
 */

class Leg {

    /**
     * final number of workers for this leg 
     */
    private final int numberOfWorkers;

    /**
     * length of time to grow or decrease to numberOfWorkers in milliseconds
     */
    private final long growthTime;

    /**
     * Increase or decrease number of workers in steps of this size. 
     * Always positive number, increase or decrease is calculated from start and end level
     * 
     */

    private final int stepSize;

    /**
     * Length of time to hold at numberOfWorkers level in milliseconds
     */
    private final long holdPeriod;

    /**
     * @param growthTime
     * @param stepSize
     * @param numberOfWorkers
     * @param holdPeriod
     */
    public Leg(long growthTime, int stepSize, int numberOfWorkers,
            long holdPeriod) {
        super();
        this.growthTime = growthTime;
        this.stepSize = Math.abs(stepSize);
        this.numberOfWorkers = numberOfWorkers;
        this.holdPeriod = holdPeriod;
        if (numberOfWorkers < 0)
            throw new IllegalStateException(
                    "Number of workers cannot be scheduled below 0");
        if (growthTime < 0)
            throw new IllegalStateException(
                    "Growth time cannot be scheduled below 0");
        if (holdPeriod < 0)
            throw new IllegalStateException(
                    "HoldPeriod cannot be scheduled below 0");
        if (stepSize < 0)
            throw new IllegalStateException(
                    "Step size cannot be scheduled below 0");
    }

    /**
     * @return the numberOfWorkers
     */
    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    /**
     * @return the growthTime in milliseconds
     */
    public long getGrowthTime() {
        return growthTime;
    }

    /**
     * @return the stepSize
     */
    public int getStepSize() {
        return stepSize;
    }

    /**
     * @return the holdPeriod in milliseconds
     */
    public long getHoldPeriod() {
        return holdPeriod;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (growthTime ^ (growthTime >>> 32));
        result = prime * result + (int) (holdPeriod ^ (holdPeriod >>> 32));
        result = prime * result + numberOfWorkers;
        result = prime * result + stepSize;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Leg other = (Leg) obj;
        if (growthTime != other.growthTime) {
            return false;
        }
        if (holdPeriod != other.holdPeriod) {
            return false;
        }
        if (numberOfWorkers != other.numberOfWorkers) {
            return false;
        }
        if (stepSize != other.stepSize) {
            return false;
        }
        return true;
    }

    public String toString() {
        return stepSize + ":" + growthTime + "s:" + numberOfWorkers + ":"
                + holdPeriod + "s";

    }

}