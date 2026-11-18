package com.macsuite.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonParser;

import static org.testng.Assert.assertEquals;

public class ExecutionMetadataTest {
    @AfterMethod
    public void clearTarget() {
        ExecutionMetadata.clearTarget();
    }

    @Test
    public void tracksTheSelectedEnvironmentAndLocalExecutor() {
        ExecutionMetadata.recordTarget("sei", false);
        assertEquals(ExecutionMetadata.describe(Map.of(), "local-runner"),
                "Environment: SEI\nExecuted by: local-runner");
        ExecutionMetadata.clearTarget();
        assertEquals(ExecutionMetadata.describe(Map.of(), "local-runner"),
                "Environment: Not resolved\nExecuted by: local-runner");
    }

    @Test
    public void distinguishesScheduledRunsFromManualReruns() {
        ExecutionMetadata.recordTarget("QA", false);
        assertEquals(ExecutionMetadata.describe(Map.of("GITHUB_ACTIONS", "true",
                "GITHUB_EVENT_NAME", "schedule", "GITHUB_ACTOR", "schedule-owner"), "runner"),
                "Environment: QA\nExecuted by: GitHub Actions schedule (actor: schedule-owner)");
        assertEquals(ExecutionMetadata.describe(Map.of("GITHUB_ACTIONS", "true",
                "GITHUB_EVENT_NAME", "schedule", "GITHUB_ACTOR", "schedule-owner",
                "GITHUB_RUN_ATTEMPT", "2", "GITHUB_TRIGGERING_ACTOR", "rerun-user"), "runner"),
                "Environment: QA\nExecuted by: rerun-user (GitHub Actions)");
    }

    @Test
    public void labelsTheFallbackUrlWithoutClaimingItIsTheRequestedEnvironment() {
        ExecutionMetadata.recordTarget("UAT", true);
        assertEquals(ExecutionMetadata.describe(Map.of("GITHUB_ACTIONS", "true",
                "GITHUB_EVENT_NAME", "workflow_dispatch", "GITHUB_ACTOR", "manual-user"), "runner"),
                "Environment: Default URL (requested UAT)\nExecuted by: manual-user (GitHub Actions)");
    }

    @Test
    public void writesAllureOverviewForLocalAndGithubRuns() throws IOException {
        Path results = Files.createTempDirectory("allure-overview-test");
        Path environment = results.resolve("environment.properties");
        Path executor = results.resolve("executor.json");
        try {
            ExecutionMetadata.writeAllureOverview(results, Set.of("QA"), Map.of(), "local-runner");
            assertOverview(environment, executor, "QA", "Local: local-runner");

            ExecutionMetadata.writeAllureOverview(results, Set.of("UAT", "SEI"), Map.of(
                    "GITHUB_ACTIONS", "true", "GITHUB_EVENT_NAME", "workflow_dispatch",
                    "GITHUB_ACTOR", "initial-actor", "GITHUB_TRIGGERING_ACTOR", "rerun-user"), "ci-runner");
            assertOverview(environment, executor, "SEI, UAT", "rerun-user (GitHub Actions)");
        } finally {
            Files.deleteIfExists(executor);
            Files.deleteIfExists(environment);
            Files.deleteIfExists(results);
        }
    }

    private static void assertOverview(Path environment, Path executor, String expectedEnvironment,
            String expectedExecutor) throws IOException {
        Properties properties = new Properties();
        try (var input = Files.newInputStream(environment)) {
            properties.load(input);
        }
        assertEquals(properties.getProperty("Environment"), expectedEnvironment);
        assertEquals(JsonParser.parseString(Files.readString(executor)).getAsJsonObject()
                .get("buildName").getAsString(), expectedExecutor);
    }
}
