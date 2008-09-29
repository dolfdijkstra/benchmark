package com.fatwire.benchmark.script;

import java.util.List;

import com.fatwire.benchmark.HttpWorker;
import com.fatwire.benchmark.session.Page;
import com.fatwire.benchmark.session.ScriptSession;
import com.fatwire.benchmark.session.ScriptSessionImpl;
import com.fatwire.benchmark.util.RandomizerIterator;

public class RandomScript extends SimpleScript {

    public RandomScript(List<Page> pages,long defaultDelay) {
        super(pages, defaultDelay);
    }

    public ScriptSession getNextSession(final HttpWorker worker) {
        return new ScriptSessionImpl(new RandomizerIterator<Page>(pages));
    }
    public String toString() {
        return "RandomScript";
    }

}
