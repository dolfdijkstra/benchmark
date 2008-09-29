package com.fatwire.benchmark.connectionmanager;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpConnection;

public class ConnectionHelper {
    /**
     * Since the same connection is about to be reused, make sure the
     * previous request was completely processed, and if not
     * consume it now.
     * @param conn The connection
     */
    static void finishLastResponse(HttpConnection conn) {
        InputStream lastResponse = conn.getLastResponseInputStream();
        if (lastResponse != null) {
            conn.setLastResponseInputStream(null);
            try {
                lastResponse.close();
            } catch (IOException ioe) {
                conn.close();
            }
        }
    }

}
