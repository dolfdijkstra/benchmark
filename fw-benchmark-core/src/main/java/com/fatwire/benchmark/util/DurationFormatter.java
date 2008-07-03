package com.fatwire.benchmark.util;

public class DurationFormatter {

    public String format(final Duration duration, final TimePrecision p) {
        final StringBuilder b = new StringBuilder();

        b.append(duration.getHours()).append("h:");
        if (duration.getMinutes() < 10) {
            b.append("0");
        }
        b.append(duration.getMinutes()).append("m");
        if (p == TimePrecision.MINUTE) {
            return b.toString();
        }
        b.append(":");
        if (duration.getSeconds() < 10) {
            b.append("0");
        }

        b.append(duration.getSeconds()).append("s");
        if (p == TimePrecision.SECOND) {
            return b.toString();
        }
        b.append(":");
        if (duration.getMilliseconds() < 10) {
            b.append("00");
        } else if (duration.getMilliseconds() < 100) {
            b.append("0");
        }

        b.append(duration.getMilliseconds()).append("ms");
        if (p == TimePrecision.MILLISECOND) {
            return b.toString();
        }

        b.append(":");
        if (duration.getMicroseconds() < 10) {
            b.append("00");
        } else if (duration.getMicroseconds() < 100) {
            b.append("0");
        }

        b.append(duration.getMicroseconds()).append("\u00b5s");
        if (p == TimePrecision.MICROSECOND) {
            return b.toString();
        }
        b.append(":");
        if (duration.getNanoseconds() < 10) {
            b.append("00");
        } else if (duration.getNanoseconds() < 100) {
            b.append("0");
        }

        b.append(duration.getNanoseconds()).append("ns");
        return b.toString();

    }
}
