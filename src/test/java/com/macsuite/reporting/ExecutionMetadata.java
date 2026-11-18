package com.macsuite.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.cucumber.java.Scenario;
import io.qameta.allure.util.PropertiesUtils;

public final class ExecutionMetadata {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionMetadata.class);
    private static final ThreadLocal<String> TARGET = new ThreadLocal<>();
    private static final Set<String> RUN_TARGETS = new ConcurrentSkipListSet<>();

    private ExecutionMetadata() {
    }

    public static void recordTarget(String environment, boolean usingDefaultUrl) {
        String normalized = environment.trim().toUpperCase(Locale.ROOT);
        String target = usingDefaultUrl ? "Default URL (requested " + normalized + ")" : normalized;
        TARGET.set(target);
        RUN_TARGETS.add(target);
    }

    public static void clearTarget() {
        TARGET.remove();
    }

    public static void attach(Scenario scenario) {
        scenario.attach(describe(System.getenv(), System.getProperty("user.name")),
                "text/plain", "Execution metadata");
    }

    public static synchronized void writeAllureOverview() {
        Path resultsDirectory = Path.of(PropertiesUtils.loadAllureProperties()
                .getProperty("allure.results.directory", "allure-results"));
        try {
            writeAllureOverview(resultsDirectory, RUN_TARGETS, System.getenv(), System.getProperty("user.name"));
        } catch (IOException e) {
            logger.warn("Could not write Allure Overview metadata to {}", resultsDirectory, e);
        }
    }

    static void writeAllureOverview(Path resultsDirectory, Set<String> targets, Map<String, String> env,
            String localUser) throws IOException {
        Files.createDirectories(resultsDirectory);

        Properties properties = new Properties();
        properties.setProperty("Environment",
                targets.isEmpty() ? "Not resolved" : String.join(", ", new TreeSet<>(targets)));
        try (OutputStream output = Files.newOutputStream(resultsDirectory.resolve("environment.properties"))) {
            properties.store(output, null);
        }

        boolean githubActions = Boolean.parseBoolean(env.get("GITHUB_ACTIONS"));
        String platform = githubActions ? "GitHub Actions" : "Local";
        String executedBy = githubActions ? executor(env, localUser) : "Local: " + executor(env, localUser);
        Files.writeString(resultsDirectory.resolve("executor.json"), new Gson().toJson(Map.of(
                "name", platform,
                "type", githubActions ? "github" : "local",
                "buildName", executedBy)));
    }

    static String describe(Map<String, String> env, String localUser) {
        String target = TARGET.get();
        return "Environment: " + (target == null ? "Not resolved" : target)
                + "\nExecuted by: " + executor(env, localUser);
    }

    private static String executor(Map<String, String> env, String localUser) {
        if (!Boolean.parseBoolean(env.get("GITHUB_ACTIONS"))) {
            return available(localUser) ? localUser : "Unknown local user";
        }

        String actor = env.get("GITHUB_ACTOR");
        String triggeringActor = env.get("GITHUB_TRIGGERING_ACTOR");
        if ("schedule".equals(env.get("GITHUB_EVENT_NAME")) && env.getOrDefault("GITHUB_RUN_ATTEMPT", "1").equals("1")) {
            return available(actor) ? "GitHub Actions schedule (actor: " + actor + ")" : "GitHub Actions schedule";
        }
        if (available(triggeringActor)) {
            return triggeringActor + " (GitHub Actions)";
        }
        return available(actor) ? actor + " (GitHub Actions)" : "Unknown GitHub Actions actor";
    }

    private static boolean available(String value) {
        return value != null && !value.isBlank();
    }
}
