package com.macsuite.common.hooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.common.ScreenshotHelper;
import com.macsuite.common.steps.LoginSteps;
import com.macsuite.config.EnvLoader;
import com.macsuite.config.TestConfig;
import com.macsuite.core.PlaywrightManager;
import com.macsuite.core.TraceAttachmentHelper;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;

/**
 * Common hooks for all test scenarios.
 * Handles Playwright initialization, cleanup, and failure reporting.
 */
public class CommonHooks {
    private static final Logger logger = LoggerFactory.getLogger(CommonHooks.class);
    private static final ThreadLocal<Boolean> failedStepScreenshotCaptured = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<String> activeFeatureUri = new ThreadLocal<>();

    @BeforeAll
    public static void beforeAll() {
        EnvLoader.load();
    }

    @Before
    public void setUp(Scenario scenario) {
        logger.info("Starting scenario: {} (tags: {})", scenario.getName(), scenario.getSourceTagNames());
        String featureUri = scenario.getUri().toString();
        String currentFeature = activeFeatureUri.get();

        // Reuse browser for all scenarios within the same feature file.
        // If Cucumber moves to a different feature on this thread, close and reopen
        // once.
        if (PlaywrightManager.isInitialized() && currentFeature != null && !currentFeature.equals(featureUri)) {
            logger.info("Feature changed on thread {}. Closing previous feature session: {}",
                    Thread.currentThread().getId(), currentFeature);
            PlaywrightManager.close(false, null);
            activeFeatureUri.remove();
        }

        if (!PlaywrightManager.isInitialized()) {
            // Generate video name: feature-scenario-timestamp
            String featureName = featureUri
                    .replaceAll(".*/features/", "")
                    .replaceAll(".feature$", "")
                    .replaceAll("/", "-");
            String scenarioName = scenario.getName()
                    .replaceAll("[^a-zA-Z0-9\\s]", "")
                    .replaceAll("\\s+", "_");
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new java.util.Date());
            String videoName = featureName + "-" + scenarioName + "-" + timestamp;

            PlaywrightManager.init(videoName);
            activeFeatureUri.set(featureUri);
            logger.info("Initialized browser session for feature: {}", featureName);
        } else {
            logger.info("Reusing existing browser session for feature: {}", featureUri);
        }

        ScreenshotHelper.bind(scenario);
        failedStepScreenshotCaptured.set(false);
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        String screenshotMode = TestConfig.getScreenshotMode();
        if (!"step".equals(screenshotMode) && !scenario.isFailed())
            return;
        try {
            byte[] screenshot = PlaywrightManager.page().screenshot(
                    new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(true));
            String label = scenario.isFailed() ? "FAILED STEP - " + scenario.getName() : "Screenshot";
            scenario.attach(screenshot, "image/png", label);
            if (scenario.isFailed()) {
                failedStepScreenshotCaptured.set(true);
                logger.info("Failure screenshot attached immediately after failed step: {}", scenario.getName());
            }
        } catch (Exception e) {
            logger.debug("Could not capture step screenshot: {}", e.getMessage());
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                logger.error("Scenario FAILED: {}", scenario.getName());
                if ("error".equals(TestConfig.getScreenshotMode()) && !failedStepScreenshotCaptured.get()) {
                    try {
                        byte[] screenshot = PlaywrightManager.page().screenshot(
                                new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(true));
                        scenario.attach(screenshot, "image/png", scenario.getName());
                        logger.info("Failure screenshot attached to report for: {}", scenario.getName());
                    } catch (Exception screenshotError) {
                        logger.warn("Could not attach screenshot: {}", screenshotError.getMessage());
                    }
                }
                String screenshotPath = PlaywrightManager
                        .takeScreenshot(scenario.getName().replaceAll("[^a-zA-Z0-9]", "_"));
                if (screenshotPath != null) {
                    logger.info("Failure screenshot saved to disk: {}", screenshotPath);
                }
            } else {
                logger.info("Scenario PASSED: {}", scenario.getName());
            }
        } catch (Exception e) {
            logger.error("Error in tearDown for scenario: {}", scenario.getName(), e);
        } finally {
            String traceName = scenario.getName()
                    .replaceAll("[^a-zA-Z0-9]", "_")
                    .replaceAll("_+", "_")
                    + "_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());

            String tracePath = PlaywrightManager.rotateTraceForScenario(scenario.isFailed(), traceName);

            LoginSteps.clear();
            ScreenshotHelper.clear();
            failedStepScreenshotCaptured.remove();

            // Attach Playwright trace to Allure report for failed scenarios
            if (scenario.isFailed()) {
                String tracePathForReport = (tracePath != null) ? tracePath : ("target/traces/" + traceName + ".zip");
                boolean attached = TraceAttachmentHelper.attachTraceToAllure(tracePathForReport, scenario.getName());
                if (attached) {
                    logger.info("Playwright trace attached to Allure report: {}", tracePathForReport);
                } else {
                    // If trace file is too large or missing, attach instructions instead
                    TraceAttachmentHelper.attachTraceInstructions(tracePathForReport);
                }
            }
        }
    }

    @AfterAll
    public static void afterAll() {
        if (PlaywrightManager.isInitialized()) {
            logger.info("Closing remaining browser session at end of test run on thread {}",
                    Thread.currentThread().getId());
            PlaywrightManager.close(false, null);
        }
        activeFeatureUri.remove();
    }
}
