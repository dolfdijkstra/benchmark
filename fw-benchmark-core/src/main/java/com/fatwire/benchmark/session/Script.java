package com.fatwire.benchmark.session;

import com.fatwire.benchmark.HttpWorker;

public interface Script {

    UserSession getNextSession(HttpWorker worker);

}
