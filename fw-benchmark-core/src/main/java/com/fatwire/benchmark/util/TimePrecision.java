/**
 * 
 */
package com.fatwire.benchmark.util;

public enum TimePrecision {

    MINUTE((long) 6E+10), SECOND((long) 1E+9), MILLISECOND((long) 1E+6), MICROSECOND(
            (long) 1E+3), NANOSECOND(1L);
    private final long p;

    private TimePrecision(final long p) {
        this.p = p;
    }

    public long calculateElapsed(final long l) {
        return l / p;
    }

    public String toString(final long elapsed) {
        long h = 0;
        long m = 0;
        long s = 0;
        long ms = 0;
        long us = 0;
        long ns = 0;
        long rest = elapsed;
        switch (this) {
        case NANOSECOND:
            ns = rest % 1000;
            rest = rest / 1000;
            if (rest == 0)
                break;
        case MICROSECOND:
            us = rest % 1000;
            rest = rest / 1000;
            if (rest == 0)
                break;
        case MILLISECOND:
            ms = rest % 1000;
            rest = rest / 1000;
            if (rest == 0)
                break;
        case SECOND:
            s = rest % 60;
            rest = rest / 60;
            if (rest == 0)
                break;
        case MINUTE:
            m = rest % 60;
            rest = rest / 60;
            if (rest == 0)
                break;
            h = rest;

        }

        StringBuilder b = new StringBuilder();

        b.append(h).append("h:");
        if (m < 10) {
            b.append("0");
        }
        b.append(m).append("m");
        if (this == MINUTE) {
            return b.toString();
        }
        b.append(":");
        if (s < 10) {
            b.append("0");
        }

        b.append(s).append("s");
        if (this == SECOND) {
            return b.toString();
        }
        b.append(":");
        if (ms < 10) {
            b.append("00");
        } else if (ms < 100) {
            b.append("0");
        }

        b.append(ms).append("ms");
        if (this == MILLISECOND) {
            return b.toString();
        }

        b.append(":");
        if (us < 10) {
            b.append("00");
        } else if (us < 100) {
            b.append("0");
        }

        b.append(us).append("\u00b5s");
        if (this == MICROSECOND) {
            return b.toString();
        }
        b.append(":");
        if (ns < 10) {
            b.append("00");
        } else if (ns < 100) {
            b.append("0");
        }

        b.append(ns).append("ns");
        return b.toString();
    }
}