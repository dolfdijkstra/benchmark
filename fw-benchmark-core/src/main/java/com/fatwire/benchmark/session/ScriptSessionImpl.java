package com.fatwire.benchmark.session;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScriptSessionImpl implements ScriptSession {
    private static final Log LOG = LogFactory.getLog(ScriptSessionImpl.class);

    private Page last;

    private final Iterator<Page> itor;

    /**
     * @param itor
     */
    public ScriptSessionImpl(final Iterator<Page> pages) {
        super();
        this.itor = pages;
    }

    /* (non-Javadoc)
     * @see com.fatwire.benchmark.session.ScriptSession#finished()
     */
    public boolean finished() {
        return !itor.hasNext();
    }

    /* (non-Javadoc)
     * @see com.fatwire.benchmark.session.ScriptSession#getNextPage()
     */
    public Page getNextPage() {
        if (finished()) {
            throw new IllegalStateException("Session is finished.");
        }
        /*
        if (last != null && last.getReadTime() > 0) {
            try {
                Thread.sleep(last.getReadTime());
            } catch (InterruptedException e) {
                LOG.warn(e,e);
            }
        }
        */
        last = itor.next();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Next page: " + String.valueOf(last));
        }
        return last;
    }

}
