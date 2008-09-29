package com.fatwire.benchmark.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.fatwire.benchmark.session.Page;

public abstract class AbstractScriptFactory implements ScriptFactory {

    private final String filename;

    private final long defaultReadTime;

    private URI host;

    /**
     * @param filename
     * @param defaultReadTime
     */
    public AbstractScriptFactory(final String filename,
            final long defaultReadTime) {
        super();
        this.filename = filename;
        this.defaultReadTime = defaultReadTime;
    }

    public abstract Script getScript() throws Exception ;

    

    protected List<Page> readPages() throws Exception {

        final List<Page> pages = new LinkedList<Page>();

        final BufferedReader r = new BufferedReader(filename.startsWith("http://") ? new InputStreamReader(new URL(filename).openStream()):new FileReader(filename));
        String u = null;
        while ((u = r.readLine()) != null) {
            if (u.length() > 2 && !u.trim().startsWith("#")) {
                pages.add(createPage(u.trim()));
            }

        }
        r.close();
        return pages;
    }

    protected Page createPage(String s) {
        URI uri = URI.create(s);
        if (!uri.isAbsolute()) {
            if (host == null) {
                throw new IllegalStateException("host is not set");
            }
            uri = host.resolve(uri);
        }
        final Page page = new Page(uri);
        page.setReadTime(getReadTimeForPage(page));
        return page;
    }
    
    protected long getReadTimeForPage(Page page){
        return defaultReadTime;
    }

    /**
     * @return the host
     */
    public URI getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        URI h = URI.create(host);
        if (!h.isAbsolute())
            throw new IllegalArgumentException("host is not absolute (" + host
                    + ")");
        try {
            this.host = new URI(h.getScheme(), null, h.getHost(), h.getPort(),
                    null, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
/*
    public void configure(FatWireBenchmark benchmark) {
        if (benchmark.getHost() != null) {
            setHost(benchmark.getHost());
        }
    }
*/
    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    /**
     * @return the defaultReadTime
     */
    public long getDefaultReadTime() {
        return defaultReadTime;
    }

}
