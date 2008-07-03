package com.fatwire.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpResponse {
    private static final Log LOG = LogFactory.getLog(HttpResponse.class);

    private int statusCode;

    private Header[] headers;

    private byte[] body = new byte[0];

    private Exception exception;

    private HttpVersion httpVersion;

    HttpResponse(final GetMethod get) {
        if (!get.isRequestSent()) {
            throw new IllegalStateException("Request is not send.");
        }
        statusCode = get.getStatusCode();
        headers = get.getResponseHeaders();
        httpVersion = get.getEffectiveVersion();
        final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        try {
            final InputStream instream = get.getResponseBodyAsStream();
            if (instream != null) {
                final byte[] buffer = new byte[4096];
                int len;
                while ((len = instream.read(buffer)) > 0) {
                    outstream.write(buffer, 0, len);
                }

            }
        } catch (final Exception e) {
            try {
                final StringBuilder b = new StringBuilder();
                b.append(get.getURI());
                b.append("\r\n");
                b.append(get.getStatusLine());
                b.append("\r\n");
                for (final Header h : headers) {
                    b.append(h);
                }
                b.append(outstream.size());
                b.append("\r\n");
                LOG.warn(b.toString(), e);
                exception = e;
            } catch (final Exception e1) {
                LOG.error(e, e);
            }
            //abort on error, close connection
            //otherwise most likely a connection reset will be received
            get.abort();
        }

        body = outstream.toByteArray();

    }

    /**
     * @return the body
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * @return the headers
     */
    public Header[] getHeaders() {
        return headers;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @return the httpVersion
     */
    public HttpVersion getHttpVersion() {
        return httpVersion;
    }
}
