package com.fatwire.benchmark;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;

public interface UserAgentCache {

    public static final String HEADER_DATE = "Date";

    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_EXPIRES = "Expires";

    public void processRequest(final GetMethod get) throws URIException;

    public void processResponse(final GetMethod get) throws URIException;

    public void clear();

}