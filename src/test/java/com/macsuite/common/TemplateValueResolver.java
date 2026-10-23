package com.macsuite.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves dynamic placeholders in test data values.
 * Supports: ${timestamp}, ${timestamp-short}, ${random}, ${today}, ${today+N}, ${today-N}
 */
public final class TemplateValueResolver {
    private static final Pattern TODAY_PATTERN = Pattern.compile("\\$\\{today([+-]\\d+)?}");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("MMddyyHHmmss");
    private static final DateTimeFormatter TIMESTAMP_SHORT_FORMATTER = DateTimeFormatter.ofPattern("MMddyyHHmm");

    private TemplateValueResolver() {
        // Utility class
    }

    public static String resolve(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }

        if (value.contains("${timestamp-short}")) {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_SHORT_FORMATTER);
            value = value.replace("${timestamp-short}", timestamp);
        }

        if (value.contains("${timestamp}")) {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            value = value.replace("${timestamp}", timestamp);
        }

        if (value.contains("${random}")) {
            int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
            value = value.replace("${random}", String.valueOf(random));
        }

        Matcher matcher = TODAY_PATTERN.matcher(value);
        while (matcher.find()) {
            String offsetToken = matcher.group(1);
            int offsetDays = (offsetToken == null) ? 0 : Integer.parseInt(offsetToken);
            String resolvedDate = LocalDate.now().plusDays(offsetDays).format(DATE_FORMATTER);
            value = value.replace(matcher.group(0), resolvedDate);
        }

        return value;
    }
}