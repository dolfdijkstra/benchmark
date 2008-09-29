package com.fatwire.benchmark.session;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class Page {

    private final URI pageUri;

    private final List<URI> containingUris = new LinkedList<URI>();

    private long readTime = 0;

    /**
     * @param pageUri
     */
    public Page(URI pageUri) {
        super();
        this.pageUri = pageUri;
    }

    /**
     * @return the containingUris
     */
    public List<URI> getContainingUris() {
        return containingUris;
    }

    /**
     * @param containingUris the containingUris to set
     */
    public void addContainingUris(List<URI> containingUris) {
        this.containingUris.addAll(containingUris);
    }

    public void addContainingUri(URI containingUri) {
        this.containingUris.add(containingUri);
    }

    /**
     * @return the pageUri
     */
    public URI getPageUri() {
        return pageUri;
    }

    /**
     * The time a user takes to read the page
     * 
     * @return the readTime
     */
    public long getReadTime() {
        return readTime;
    }

    /**
     * @param readTime the readTime to set
     */
    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((containingUris == null) ? 0 : containingUris.hashCode());
        result = prime * result + ((pageUri == null) ? 0 : pageUri.hashCode());
        result = prime * result + (int) (readTime ^ (readTime >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Page)) {
            return false;
        }
        Page other = (Page) obj;
        if (containingUris == null) {
            if (other.containingUris != null) {
                return false;
            }
        } else if (!containingUris.equals(other.containingUris)) {
            return false;
        }
        if (pageUri == null) {
            if (other.pageUri != null) {
                return false;
            }
        } else if (!pageUri.equals(other.pageUri)) {
            return false;
        }
        if (readTime != other.readTime) {
            return false;
        }
        return true;
    }

}
