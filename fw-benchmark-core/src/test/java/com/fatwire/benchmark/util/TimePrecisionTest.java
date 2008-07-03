package com.fatwire.benchmark.util;

import junit.framework.TestCase;

public class TimePrecisionTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCalculateElapsed() {
        TimePrecision s = TimePrecision.SECOND;
        long v=s.calculateElapsed((long)654e+9);
        assertEquals(654L,v);

    }

    public void testToString() {
        TimePrecision s = TimePrecision.SECOND;
        String v=s.toString(654);
        assertEquals("0h:10m:54s",v);
    }
    public void testToStringHour() {
        TimePrecision s = TimePrecision.SECOND;
        String v=s.toString(654 + 2*3600);
        assertEquals("2h:10m:54s",v);
    }
    public void testToStringMicro() {
        TimePrecision s = TimePrecision.MICROSECOND;
        String v=s.toString(6654);
        assertEquals("0h:00m:00s:006ms:654µs",v);
    }
    public void testToStringMilli() {
        TimePrecision s = TimePrecision.MILLISECOND;
        String v=s.toString(2654);
        assertEquals("0h:00m:02s:654ms",v);
    }
    public void testToStringNano() {
        TimePrecision s = TimePrecision.NANOSECOND;
        String v=s.toString(6654);
        assertEquals("0h:00m:00s:000ms:006µs:654ns",v);
        
    }

}
