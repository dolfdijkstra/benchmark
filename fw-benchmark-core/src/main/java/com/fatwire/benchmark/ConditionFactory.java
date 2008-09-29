package com.fatwire.benchmark;

import com.fatwire.benchmark.util.TimeUtil;

public class ConditionFactory {

    public Condition create(String max) {
        boolean isTime = false;
        for (char c : max.toCharArray()) {
            if (!Character.isDigit(c)) {
                isTime = true;
                break;
            }
        }
        if (isTime) {
            int t = parseTime(max);
            return new TimeConditional(t);

        }

        return new CounterConditional(Integer.parseInt(max));
    }

    protected int parseTime(String s) {
        return TimeUtil.parseTime(s);
    }
}
