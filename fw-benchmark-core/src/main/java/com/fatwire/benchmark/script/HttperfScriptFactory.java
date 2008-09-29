package com.fatwire.benchmark.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.fatwire.benchmark.session.Page;

public class HttperfScriptFactory extends AbstractScriptFactory {

    /**
     * @param filename
     * @param defaultReadTime
     */
    public HttperfScriptFactory(final String filename,
            final long defaultReadTime) {
        super(filename, defaultReadTime);
    }

    public Script getScript() throws Exception {
        return new HttperfScript(readPages());
    }

    protected List<Page> readPages() throws Exception {

        final List<Page> pages = new LinkedList<Page>();

        final BufferedReader r = new BufferedReader(this.getFilename()
                .startsWith("http://") ? new InputStreamReader(new URL(
                getFilename()).openStream()) : new FileReader(getFilename()));
        String u = null;
        Page p = null;
        while ((u = r.readLine()) != null) {
            if (u.length() > 2 && !u.trim().startsWith("#")) {
                boolean isLink = false;
                if (u.startsWith("\t") || u.startsWith(" ")) {
                    isLink = true;
                }
                URI uri = createUri(u);
                if (isLink && p != null) {
                    p.addContainingUri(uri);
                } else {
                    p = createPage(u);
                }
                pages.add(createPage(u));
            }

        }
        r.close();
        return pages;
    }

    protected URI createUri(String s) {

        URI uri = URI.create(s.trim());
        if (!uri.isAbsolute()) {
            if (getHost() == null) {
                throw new IllegalStateException("host is not set");
            }
            uri = getHost().resolve(uri);
        }
        return uri;
    }

    protected Page createPage(String s) {
        final Page page = new Page(createUri(s));
        page.setReadTime(getReadTimeForPage(page));
        return page;
    }

}
