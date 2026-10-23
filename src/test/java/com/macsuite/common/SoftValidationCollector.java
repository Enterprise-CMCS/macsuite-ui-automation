package com.macsuite.common;

import java.util.ArrayList;
import java.util.List;

public final class SoftValidationCollector {
    private static final ThreadLocal<List<String>> ISSUES = ThreadLocal.withInitial(ArrayList::new);

    private SoftValidationCollector() {
    }

    public static void addIssue(String issue) {
        if (issue != null && !issue.isBlank()) {
            ISSUES.get().add(issue);
        }
    }

    public static boolean hasIssues() {
        return !ISSUES.get().isEmpty();
    }

    public static String summary() {
        return String.join(System.lineSeparator(), ISSUES.get());
    }

    public static void clear() {
        ISSUES.remove();
    }
}
