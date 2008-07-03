/**
 * 
 */
package com.fatwire.benchmark;

import org.apache.commons.httpclient.HttpMethodBase;

interface HeaderDecorator {
    void addHeaders(HttpMethodBase method);
}