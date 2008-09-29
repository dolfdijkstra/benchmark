package com.fatwire.benchmark.script;

import java.util.List;

import com.fatwire.benchmark.HttpWorker;
import com.fatwire.benchmark.session.Page;
import com.fatwire.benchmark.session.ScriptSession;
import com.fatwire.benchmark.session.ScriptSessionImpl;
import com.fatwire.benchmark.util.EternalIterator;

public class SimpleScript implements Script {
    protected final List<Page> pages;

    private final long defaultDelay;

    public SimpleScript(List<Page> pages, long defaultDelay) {
        this.pages = pages;
        this.defaultDelay = defaultDelay;
    }

    public ScriptSession getNextSession(final HttpWorker worker) {
        return new ScriptSessionImpl(new EternalIterator<Page>(pages));
    }

    public long getDefaultDelay() {
        return defaultDelay;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SimpleScript";
    }

}
