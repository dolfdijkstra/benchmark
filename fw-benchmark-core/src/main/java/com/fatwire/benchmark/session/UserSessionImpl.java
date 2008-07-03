package com.fatwire.benchmark.session;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserSessionImpl implements UserSession {
    private static final Log LOG = LogFactory.getLog(UserSessionImpl.class);

    private Page last;

    private Iterator<Page> itor;

    /**
     * @param itor
     */
    public UserSessionImpl(Iterator<Page> pages) {
        super();
        this.itor = pages;
    }

    /* (non-Javadoc)
     * @see com.fatwire.benchmark.session.UserSession#finished()
     */
    public boolean finished() {
        return !itor.hasNext();
    }

    /* (non-Javadoc)
     * @see com.fatwire.benchmark.session.UserSession#getNextPage()
     */
    public Page getNextPage() {
        if (finished())
            throw new IllegalStateException("Session is finished.");
        if (last != null && last.getReadTime() > 0) {
            try {
                Thread.sleep(last.getReadTime());
            } catch (InterruptedException e) {
                LOG.warn(e,e);
            }
        }
        last = itor.next();
        return last;
    }

}
