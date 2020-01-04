package org.httpgun.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
    public static String friendlyDouble(double value) {
        return String.format("%.2f", value);
    }
}
