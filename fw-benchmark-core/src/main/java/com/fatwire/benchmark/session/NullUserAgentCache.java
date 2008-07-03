package com.fatwire.benchmark.session;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.fatwire.benchmark.UserAgentCache;

public class NullUserAgentCache implements UserAgentCache {

    public void clear() {

    }

    public void processRequest(GetMethod get) throws URIException {

    }

    public void processResponse(GetMethod get) throws URIException {

    }

}
