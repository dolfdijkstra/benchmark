package com.fatwire.benchmark;

import java.net.URI;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;

public class HttpRequest {
    private Header[] headers;

    private HttpVersion httpVersion;

    private URI uri;
    


    HttpRequest(GetMethod get) throws URIException {
        headers = get.getRequestHeaders();
        httpVersion = get.getEffectiveVersion();
        uri = URI.create(get.getURI().toString());
    }



    /**
     * @return the headers
     */
    public Header[] getHeaders() {
        return headers;
    }



    /**
     * @return the httpVersion
     */
    public HttpVersion getHttpVersion() {
        return httpVersion;
    }



    /**
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }
}
