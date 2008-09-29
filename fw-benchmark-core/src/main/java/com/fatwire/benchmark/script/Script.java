package com.fatwire.benchmark.script;

import com.fatwire.benchmark.HttpWorker;
import com.fatwire.benchmark.session.ScriptSession;

public interface Script {

    ScriptSession getNextSession(HttpWorker worker);

    long getDefaultDelay();

}
