package com.fatwire.benchmark.script;


public class SimpleScriptFactory extends AbstractScriptFactory {

    /**
     * @param filename
     * @param defaultReadTime
     */
    public SimpleScriptFactory(final String filename, final long defaultReadTime) {
        super(filename,defaultReadTime);
    }

    public Script getScript() throws Exception {
        return new SimpleScript(readPages(), this.getDefaultReadTime());

    }

}
