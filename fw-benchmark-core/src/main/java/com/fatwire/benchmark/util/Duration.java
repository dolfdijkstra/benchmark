package com.fatwire.benchmark.util;

public class Duration {
    private long hours = 0;

    private long minutes = 0;

    private long seconds = 0;

    private long milliseconds = 0;

    private long microseconds = 0;

    private long nanoseconds = 0;

    /**
     * @param p the time precision of the elapsed value
     * @param elapsed
     */
    public Duration(TimePrecision p, long elapsed) {
        long rest = elapsed;
        switch (p) {
        case NANOSECOND:
            nanoseconds = rest % 1000;
            rest = rest / 1000;
            if (rest == 0)
                break;
        case MICROSECOND:
            microseconds = rest % 1000;
            rest = rest / 1000;
            if (rest == 0)
                break;
        case MILLISECOND:
            milliseconds = rest % 1000;
            rest = rest / 1000;
            if (rest == 0)
                break;
        case SECOND:
            seconds = rest % 60;
            rest = rest / 60;
            if (rest == 0)
                break;
        case MINUTE:
            minutes = rest % 60;
            hours = rest / 60;
            break;
        }
    }

    /**
     * @return the hours
     */
    public long getHours() {
        return hours;
    }

    /**
     * @return the microseconds
     */
    public long getMicroseconds() {
        return microseconds;
    }

    /**
     * @return the milliseconds
     */
    public long getMilliseconds() {
        return milliseconds;
    }

    /**
     * @return the minutes
     */
    public long getMinutes() {
        return minutes;
    }

    /**
     * @return the nanoseconds
     */
    public long getNanoseconds() {
        return nanoseconds;
    }

    /**
     * @return the seconds
     */
    public long getSeconds() {
        return seconds;
    }

}
