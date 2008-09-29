package com.fatwire.benchmark.script;

import java.util.List;

import com.fatwire.benchmark.HttpWorker;
import com.fatwire.benchmark.session.Page;
import com.fatwire.benchmark.session.ScriptSession;
import com.fatwire.benchmark.session.ScriptSessionImpl;

public class HttperfScript implements Script {
    private final List<Page> pages;

    public HttperfScript(List<Page> pages) {
        this.pages = pages;
    }

    public ScriptSession getNextSession(final HttpWorker worker) {
        return new ScriptSessionImpl(pages.iterator());
    }

    public long getDefaultDelay() {
        return 0;
    }

}
