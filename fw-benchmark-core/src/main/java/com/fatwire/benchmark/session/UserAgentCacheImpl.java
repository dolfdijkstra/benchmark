package com.fatwire.benchmark.session;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fatwire.benchmark.UserAgentCache;

public class UserAgentCacheImpl implements UserAgentCache {

    private static final Log LOG = LogFactory.getLog(UserAgentCacheImpl.class);

    private final Map<URI, UriCacheInfo> cacheInfo = new ConcurrentHashMap<URI, UriCacheInfo>();

    /* (non-Javadoc)
     * @see com.fatwire.benchmark.session.UserAgentCache#processRequest(org.apache.commons.httpclient.methods.GetMethod)
     */
    public void processRequest(final GetMethod get) throws URIException {
        UriCacheInfo ci = cacheInfo.get(get.getURI());
        if (ci == null) {
            ci = new UriCacheInfo();
            cacheInfo.put(get.getURI(), ci);
        }
        ci.decorateRequest(get);
    }

    /* (non-Javadoc)
     * @see com.fatwire.benchmark.session.UserAgentCache#processResponse(org.apache.commons.httpclient.methods.GetMethod)
     */
    public void processResponse(final GetMethod get) throws URIException {
        UriCacheInfo ci = cacheInfo.get(get.getURI());
        if (ci == null) {
            ci = new UriCacheInfo();
            cacheInfo.put(get.getURI(), ci);
        }
        ci.handleResponse(get);
    }

    /* (non-Javadoc)
     * @see com.fatwire.benchmark.session.UserAgentCache#clear()
     */
    public void clear() {
        cacheInfo.clear();
    }

    static class UriCacheInfo {
        private Date lastModified;

        private Date expiryDate;

        void decorateRequest(final GetMethod get) {
            LOG.trace("decorateRequest");
            if (getLastModified() != null) {
                get.setRequestHeader(new Header(
                        UserAgentCache.HEADER_IF_MODIFIED_SINCE, DateUtil
                                .formatDate(getLastModified())));
            }
        }

        boolean isCached(final Date now) {
            return (getExpiryDate() != null) && now.before(getExpiryDate());
        }

        void handleResponse(final GetMethod get) {
            LOG.trace("handleResponse");
            if (!HttpVersion.HTTP_1_1.greaterEquals(get.getEffectiveVersion())) {
                return;
            }
            /*
             * Do we have a Date header? If not we are running clockless and we bail out.
             */
            final Header dateHeader = get
                    .getResponseHeader(UserAgentCache.HEADER_DATE);
            Date serverDate = null;
            if (dateHeader == null) {
                return;
            } else if (dateHeader.getValue() == null) {
                return;
            } else {
                try {
                    serverDate = DateUtil.parseDate(dateHeader.getValue());
                } catch (final DateParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
            }

            final Header lm = get
                    .getResponseHeader(UserAgentCache.HEADER_LAST_MODIFIED);
            if ((lm != null) && (lm.getValue() != null)) {
                Date lmd;
                try {
                    lmd = DateUtil.parseDate(lm.getValue());
                    if (getLastModified() == null) {
                        setLastModified(lmd);
                    } else if (getLastModified().before(lmd)) {
                        setLastModified(lmd);
                    }
                } catch (final DateParseException e) {
                    e.printStackTrace();
                }

            }

            Header cc = get
                    .getResponseHeader(UserAgentCache.HEADER_CACHE_CONTROL);
            if ((cc != null) && (cc.getValue() != null)) {
                doCacheControlHandling(cc);
                /*
                 *  http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9
                 *  If a response includes both an Expires header and a max-age directive, the max-age directive overrides the Expires header, even if the Expires header is more restrictive
                 */
                //I deviate from that and have implemented that if there is a Cache-Control header
                //the Expires header is not evaluated
                //currently this is correct because I do not handle all Cache-Control cases
            } else if ((cc = get
                    .getResponseHeader(UserAgentCache.HEADER_EXPIRES)) != null) {
                final String val = cc.getValue();
                /*
                 *  HTTP/1.1 clients and caches MUST treat other invalid date formats, especially including the value "0", as in the past (i.e., "already expired").

                 To mark a response as "already expired," an origin server sends an Expires date that is equal to the Date header value. (See the rules for expiration calculations in section 13.2.4.)

                 To mark a response as "never expires," an origin server sends an Expires date approximately one year from the time the response is sent. HTTP/1.1 servers SHOULD NOT send Expires dates more than one year in the future.

                 The presence of an Expires header field with a date value of some time in the future on a response that otherwise would by default be non-cacheable indicates that the response is cacheable, unless indicated otherwise by a Cache-Control header field (section 14.9). 
                 */

                try {
                    final Date e = DateUtil.parseDate(val);
                    if (e.after(serverDate)) {
                        setExpiryDate(e);
                    } else {
                        setExpiryDate(null);
                    }
                } catch (final DateParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    setLastModified(null);
                }
            }

        }

        private void doCacheControlHandling(final Header cc) {
            /**
             * no-store and no-cache indicate a reponse that should not be cached/stored.
             */
            for (final HeaderElement element : cc.getElements()) {
                final String name = element.getName();
                if ("no-cache".equals(name)) {
                    setExpiryDate(null);
                    return;

                } else if ("no-store".equals(name)) {
                    setExpiryDate(null);
                    return;
                }
            }
            //we have no no-cache/no-store
            for (final HeaderElement element : cc.getElements()) {
                if ("max-age=".equals(element.getName())) {
                    final String age = element.getValue();
                    final int a = Integer.parseInt(age);
                    if (a > 0) {
                        setExpiryDate(new Date(a * 60 * 1000
                                + System.currentTimeMillis()));
                    } else {
                        setExpiryDate(null);
                    }
                }

            }

        }

        private void setLastModified(Date lastModified) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("setLastModified to " + lastModified);
            }
            this.lastModified = lastModified;
        }

        private Date getLastModified() {
            return lastModified;
        }

        private void setExpiryDate(Date expiryDate) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("setExpiryDate to " + expiryDate);
            }

            this.expiryDate = expiryDate;
        }

        private Date getExpiryDate() {
            return expiryDate;
        }

    }
}