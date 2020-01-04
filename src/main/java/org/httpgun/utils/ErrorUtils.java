package org.httpgun.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
@UtilityClass
public class ErrorUtils {
    public static void log(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage());
    }
}
