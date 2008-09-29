package com.fatwire.benchmark;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fatwire.benchmark.WorkerManager.WorkerListener;
import com.fatwire.benchmark.script.Script;
import com.fatwire.benchmark.session.Page;
import com.fatwire.benchmark.session.ScriptSession;
import com.fatwire.benchmark.util.HiResStopWatch;
import com.fatwire.benchmark.util.TimePrecision;
import com.fatwire.benchmark.util.TrafficLight;

public class HttpWorker implements Runnable {
    private static final Log LOG = LogFactory.getLog(HttpWorker.class);

    private final String name;

    private final HttpClient client;

    private final TrafficLight light;

    private final CompoundCondition cond;

    private DefaultHeaderDecorator headerDecorator = new DefaultHeaderDecorator();

    private UserAgentCache cache;

    private final Script script;

    private final HiResStopWatch stopwatch = new HiResStopWatch(
            TimePrecision.MILLISECOND);

    private Set<HttpMethodListener> listeners = new CopyOnWriteArraySet<HttpMethodListener>();

    private Set<WorkerListener> lifecycleListeners = new CopyOnWriteArraySet<WorkerListener>();

    Object sleepLock = new Object();

    static class CompoundCondition implements Condition {

        private final Condition delegate;

        private boolean stopWorker = false;

        /**
         * @param delegate
         */
        public CompoundCondition(Condition delegate) {
            super();
            this.delegate = delegate;
        }

        /**
         * @return
         * @see com.fatwire.benchmark.Condition#getNum()
         */
        public int getNum() {
            return delegate.getNum();
        }

        /**
         * @return
         * @see com.fatwire.benchmark.Condition#increment()
         */
        public int increment() {
            return delegate.increment();
        }

        /**
         * @return
         * @see com.fatwire.benchmark.Condition#isTrue()
         */
        public boolean isTrue() {
            return (!stopWorker) && delegate.isTrue();
        }

        void stop() {
            stopWorker = true;
        }

    }

    /**
     * @param connectionManager
     * @param uris
     * @param counter
     * @param light
     * @param cond
     */
    public HttpWorker(String name,
            final HttpConnectionManager connectionManager, Script script,
            final TrafficLight light, Condition cond,
            UserAgentCache userAgentCache) {
        super();
        this.name = name;
        client = new HttpClient(connectionManager);
        this.cache = userAgentCache;
        this.light = light;
        this.cond = new CompoundCondition(cond);
        this.script = script;

    }

    public void run() {
        try {
            for (WorkerListener l : lifecycleListeners) {
                l.started(this);
            }
            light.waitForGreenLight();
            LOG.debug(getName() + " is starting");
            ScriptSession session;
            while (cond.isTrue()
                    && (session = script.getNextSession(this)) != null) {
                cache.clear();
                while (cond.isTrue() && !session.finished()) {
                    Page page = session.getNextPage();
                    //get.getParams().setVersion(HttpVersion.HTTP_1_0);
                    try {
                        download(new GetMethod(page.getPageUri()
                                .toASCIIString()));
                        for (URI u : page.getContainingUris()) {
                            download(new GetMethod(u.toASCIIString()));
                        }

                    } catch (final Throwable t) {
                        LOG.error(t, t);
                    }
                    if (page.getReadTime() > 0 && cond.isTrue()
                            && !session.finished()) {
                        try {
                            synchronized (sleepLock) {
                                sleepLock.wait(page.getReadTime());
                            }
                        } catch (InterruptedException e) {
                            LOG.info(e.getMessage());
                        }
                    }
                }
                cache.clear();
            }
        } finally {
            for (WorkerListener l : lifecycleListeners) {
                l.finished(this);
            }
            LOG.debug(getName() + " is finished");
        }

    }

    protected void download(final GetMethod method) throws IOException {
        cond.increment();

        RequestProcessedEvent event = null;
        try {
            stopwatch.start();
            long tStart = System.currentTimeMillis();
            dispatch(new RequestStartingEvent(this, tStart));
            this.headerDecorator.addHeaders(method);
            cache.processRequest(method);

            //int status = 
            client.executeMethod(method);
            HttpRequest req = new HttpRequest(method);
            HttpResponse res = new HttpResponse(method);
            HttpTransaction trans = new HttpTransaction(req, res, tStart,
                    stopwatch.getIntermediate(), getName());
            cache.processResponse(method);
            event = new RequestProcessedEvent(this, trans);
        } catch (IOException e) {
            LOG.error(e, e);
        } finally {
            method.releaseConnection();
            stopwatch.stop();
            dispatch(event);

        }

    }

    protected void dispatch(RequestStartingEvent event) {
        if (event != null) {
            for (HttpMethodListener listener : this.listeners) {
                listener.beforeExecute(event);
            }
        }

    }

    protected void dispatch(RequestProcessedEvent event) {
        if (event != null) {
            for (HttpMethodListener listener : this.listeners) {
                listener.executePerformed(event);
            }
        }

    }

    public void addListener(HttpMethodListener listener) {
        this.listeners.add(listener);
    }

    public void addListener(WorkerListener listener) {
        this.lifecycleListeners.add(listener);
    }

    public void removeListener(WorkerListener listener) {
        this.lifecycleListeners.remove(listener);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public synchronized void stop() {
        LOG.debug("stopping " + this.getName());
        cond.stop();
        synchronized (sleepLock) {
            sleepLock.notifyAll();
        }
        
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getName();
    }
}
