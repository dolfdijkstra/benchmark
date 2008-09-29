package com.fatwire.benchmark.util;

public class TimeUtil {
    public static int parseTime(String s) {
        int i = 0;
        int t = 0;
        int val = 0;
        for (char c : s.toCharArray()) {
            switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                break;
            case 'h':
                val += Integer.parseInt(s.substring(t, i)) * 3600;
                t = i + 1;
                break;
            case 'm':
                val += Integer.parseInt(s.substring(t, i)) * 60;
                t = i + 1;
                break;
            case 's':
                val += Integer.parseInt(s.substring(t, i));
                t = i + 1;
                break;
            default:
                throw new IllegalArgumentException(s + " can not be parsed.");
            }
            i++;
        }
        return val;
    }

}
