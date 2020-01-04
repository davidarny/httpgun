package org.httpgun;

public class StringUtils {
    private StringUtils() {
    }

    public static String friendlyDouble(double value) {
        return String.format("%.2f", value);
    }
}
