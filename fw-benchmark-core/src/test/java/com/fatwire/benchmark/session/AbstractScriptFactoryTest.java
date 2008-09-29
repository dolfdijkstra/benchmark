package com.fatwire.benchmark.session;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

public class AbstractScriptFactoryTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreatePage() throws URISyntaxException {
        URI h = URI.create("http://localhost:8080");
        URI host = new URI(h.getScheme(), null, h.getHost(), h.getPort(),
                null, null, null);
        URI s = URI.create("/cs/Satellite/FSII");
        assertEquals("http://localhost:8080/cs/Satellite/FSII",host.resolve(s).toASCIIString());
    }

}
