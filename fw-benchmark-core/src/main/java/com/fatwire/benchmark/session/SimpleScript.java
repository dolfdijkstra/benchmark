package com.fatwire.benchmark.session;

import java.util.List;

import com.fatwire.benchmark.HttpWorker;
import com.fatwire.benchmark.util.EternalIterator;

public class SimpleScript implements Script {
    private final List<Page> pages;

    public SimpleScript(List<Page> pages) {
        this.pages = pages;
    }

    public UserSession getNextSession(final HttpWorker worker) {
        return new UserSessionImpl(new EternalIterator<Page>(pages));
    }

}
