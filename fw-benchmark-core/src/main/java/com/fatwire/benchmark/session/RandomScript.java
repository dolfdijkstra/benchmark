package com.fatwire.benchmark.session;

import java.util.List;

import com.fatwire.benchmark.HttpWorker;
import com.fatwire.benchmark.util.RandomizerIterator;

public class RandomScript implements Script {
    private final List<Page> pages;

    public RandomScript(List<Page> pages) {
        this.pages = pages;
    }

    public UserSession getNextSession(final HttpWorker worker) {
        return new UserSessionImpl(new RandomizerIterator<Page>(pages));
    }

}
