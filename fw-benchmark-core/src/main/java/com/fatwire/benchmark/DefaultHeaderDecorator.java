/**
 * 
 */
package com.fatwire.benchmark;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;

class DefaultHeaderDecorator implements HeaderDecorator {
    private final Header[] headers;

    /**
     * 
     */
    public DefaultHeaderDecorator() {
        super();
        headers = new Header[4];
        headers[0] = new Header(
                "Accept",
                "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        headers[1] = new Header("Accept-Language", "en-us,en;q=0.5");
        headers[2] = new Header("Accept-Encoding", "gzip,deflate");
        headers[3] = new Header("Accept-Charset",
                "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        //headers[4] = new Header("Keep-Alive", "300");
        //headers[5] = new Header("Connection", "keep-alive");
    }

    public void addHeaders(final HttpMethodBase method) {
        for (final Header header : headers) {
            method.setRequestHeader(header);
        }

    }

}