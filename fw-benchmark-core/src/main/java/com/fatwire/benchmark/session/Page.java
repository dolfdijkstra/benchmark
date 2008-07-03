package com.fatwire.benchmark.session;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class Page {

    private final URI pageUri;

    private List<URI> containingUris = new LinkedList<URI>();

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

}
