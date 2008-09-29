package com.fatwire.benchmark.script;

public class RandomScriptFactory extends AbstractScriptFactory {

    /**
     * @param filename
     * @param defaultReadTime
     */
    public RandomScriptFactory(final String filename, final long defaultReadTime) {
        super(filename, defaultReadTime);
    }

    public Script getScript() throws Exception {
        return new RandomScript(readPages(), this.getDefaultReadTime());
    }

}
