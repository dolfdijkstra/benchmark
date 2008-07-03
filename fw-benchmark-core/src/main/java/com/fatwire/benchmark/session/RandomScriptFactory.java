package com.fatwire.benchmark.session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class RandomScriptFactory implements ScriptFactory {

    private final String filename;

    private final long defaultReadTime;

    /**
     * @param filename
     * @param defaultReadTime
     */
    public RandomScriptFactory(final String filename, final long defaultReadTime) {
        super();
        this.filename = filename;
        this.defaultReadTime = defaultReadTime;
    }

    public Script getScript() throws Exception {
        final List<Page> pages = new LinkedList<Page>();
        final BufferedReader r = new BufferedReader(new FileReader(filename));
        String u = null;
        while ((u = r.readLine()) != null) {
            if (u.length() > 2) {
                final Page page = new Page(URI.create(u));
                page.setReadTime(defaultReadTime);
                pages.add(page);
            }

        }
        r.close();
        return new RandomScript(pages);

    }

}
