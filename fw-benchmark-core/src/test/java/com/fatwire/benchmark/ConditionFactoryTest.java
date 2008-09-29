package com.fatwire.benchmark;

import junit.framework.TestCase;

public class ConditionFactoryTest extends TestCase {
    ConditionFactory f = new ConditionFactory();

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateTime() {
        Condition c = f.create("15s");
        if (c instanceof TimeConditional) {
            ((TimeConditional) c).kill();
        }else {
            fail("not a TimeConditional returned.");
        }

    }

    public void testParseTime15S() {
        check(15, "15s");
    }

    public void testParseTime500S() {
        check(500, "500s");
    }

    public void testParseTime30m15s() {
        check(15 + (30 * 60), "30m15s");
    }

    public void testParseTime12h30m15s() {
        check(15 + (30 * 60) + (12 * 3600), "12h30m15s");
    }

    public void testParseTime6m() {
        check(6 * 60, "6m");
    }

    void check(int exp, String s) {
        int v = f.parseTime(s);
        assertEquals(exp, v);

    }
}
