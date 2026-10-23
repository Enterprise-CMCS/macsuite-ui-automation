package com.macsuite.common;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.config.TestConfig;
import com.macsuite.core.PlaywrightManager;
import com.microsoft.playwright.Page;

import io.cucumber.java.Scenario;

/**
 * Interaction-level screenshot utility.
 *
 * Modes (set via SCREENSHOT_MODE system property):
 * "error" — screenshot only on scenario failure (default)
 * "interaction" — screenshot at named checkpoints in step definitions
 * "step" — screenshot after every Cucumber step (verbose)
 *
 * Call ScreenshotHelper.take("Human Readable Label") from any step def.
 * The label appears in the Cucumber/Allure report; a sanitised version is
 * used for the file name under target/screenshots/.
 */
public class ScreenshotHelper {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotHelper.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");

    private static final ThreadLocal<Scenario> scenarioHolder = new ThreadLocal<>();
    private static final ThreadLocal<AtomicInteger> counterHolder = new ThreadLocal<>();

    /** Called from CommonHooks @Before to register the current scenario. */
    public static void bind(Scenario scenario) {
        scenarioHolder.set(scenario);
        counterHolder.set(new AtomicInteger(0));
    }

    /** Called from CommonHooks @After to release the scenario reference. */
    public static void clear() {
        scenarioHolder.remove();
        counterHolder.remove();
    }

    /**
     * Get the current scenario from ThreadLocal storage.
     * Used by page objects to attach screenshots to the Cucumber report.
     * 
     * @return The current Scenario instance, or null if no scenario is bound
     */
    public static Scenario getScenario() {
        return scenarioHolder.get();
    }

    /**
     * Take a named screenshot if SCREENSHOT_MODE is "interaction" or "step".
     *
     * @param label Human-readable label shown in the report (e.g. "Stage: Triage")
     */
    public static void take(String label) {
        String mode = TestConfig.getScreenshotMode();
        if (!"interaction".equals(mode) && !"step".equals(mode)) {
            return;
        }
        capture(label);
    }

    private static void capture(String label) {
        try {
            byte[] bytes = PlaywrightManager.page().screenshot(
                    new Page.ScreenshotOptions().setFullPage(true));

            // Attach to Cucumber / Allure report with the human-readable label
            Scenario scenario = scenarioHolder.get();
            if (scenario != null) {
                scenario.attach(bytes, "image/png", label);
            }

            // Save to disk with a unique, filesystem-safe name
            AtomicInteger counter = counterHolder.get();
            int step = (counter != null) ? counter.incrementAndGet() : 0;
            String time = LocalTime.now().format(TIME_FMT);
            String safe = label.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "_")
                    .replaceAll("^_+|_+$", "");
            if (safe.length() > 50) {
                safe = safe.substring(0, 50);
            }
            String fileName = String.format("%02d_%s_%s", step, safe, time);

            java.nio.file.Path dir = Paths.get("target/screenshots");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.write(dir.resolve(fileName + ".png"), bytes);

            logger.info("Screenshot [{}] → {}.png", label, fileName);
        } catch (Exception e) {
            logger.debug("Could not capture screenshot '{}': {}", label, e.getMessage());
        }
    }

    /** Truncate a string for use in a screenshot label without losing the start. */
    public static String truncate(String text, int maxLen) {
        if (text == null)
            return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "…";
    }
}
