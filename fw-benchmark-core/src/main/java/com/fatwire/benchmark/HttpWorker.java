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

import com.fatwire.benchmark.session.NullUserAgentCache;
import com.fatwire.benchmark.session.Script;
import com.fatwire.benchmark.session.UserSession;
import com.fatwire.benchmark.util.HiResStopWatch;
import com.fatwire.benchmark.util.TimePrecision;
import com.fatwire.benchmark.util.TrafficLight;

public class HttpWorker implements Runnable {
    private static final Log LOG = LogFactory.getLog(HttpWorker.class);

    private final String name;

    private final HttpClient client;

    private UserSession session;

    private final TrafficLight light;

    private final Condition cond;

    private DefaultHeaderDecorator headerDecorator = new DefaultHeaderDecorator();

    //private UserAgentCache cache = new UserAgentCacheImpl();
    private UserAgentCache cache = new NullUserAgentCache();
    private final HiResStopWatch stopwatch = new HiResStopWatch(
            TimePrecision.MILLISECOND);

    private Set<HttpMethodListener> listeners = new CopyOnWriteArraySet<HttpMethodListener>();

    /**
     * @param connectionManager
     * @param uris
     * @param counter
     * @param light
     * @param cond
     */
    public HttpWorker(String name,
            final HttpConnectionManager connectionManager, Script script,
            final TrafficLight light, Condition cond) {
        super();
        this.name = name;
        client = new HttpClient(connectionManager);

        this.light = light;
        this.cond = cond;
        this.session = script.getNextSession(this);

    }

    public void run() {
        light.waitForGreenLight();
        LOG.debug(getName() + " is starting");
        while (cond.isTrue() && !session.finished()) {
            cond.increment();
            URI nextUri = session.getNextPage().getPageUri();
            final GetMethod get = new GetMethod(nextUri.toASCIIString());
            //get.getParams().setVersion(HttpVersion.HTTP_1_0);
            try {
                download(get);
            } catch (final Throwable t) {
                LOG.error(t, t);
            }
        }
        cache.clear();
        LOG.debug(getName() + " is finished");
    }

    protected void download(final GetMethod method) throws IOException {
        RequestProcessedEvent event = null;
        try {
            stopwatch.start();
            long tStart = System.currentTimeMillis();
            dispatch(new RequestStartingEvent(this,tStart));
            this.headerDecorator.addHeaders(method);
            cache.processRequest(method);

            //int status = 
            client.executeMethod(method);
            HttpRequest req = new HttpRequest(method);
            HttpResponse res = new HttpResponse(method);
            HttpTransaction trans = new HttpTransaction(req, res, tStart,stopwatch
                    .getIntermediate(), getName());
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
    protected void dispatch(RequestStartingEvent event){
        if (event != null) {
            for (HttpMethodListener listener : this.listeners) {
                listener.beforeExecute(event);
            }
        }
        
    }

    protected void dispatch(RequestProcessedEvent event){
        if (event != null) {
            for (HttpMethodListener listener : this.listeners) {
                listener.executePerformed(event);
            }
        }
        
    }

    public void addListener(HttpMethodListener listener) {
        this.listeners.add(listener);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}
