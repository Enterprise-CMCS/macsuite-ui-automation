package com.macsuite.core;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class for attaching Playwright traces to Allure reports.
 * Traces provide detailed debugging information including:
 * - DOM snapshots before/after each action
 * - Network activity (requests/responses)
 * - Console logs
 * - Screenshots
 * - Timing information
 */
public class TraceAttachmentHelper {
    private static final Logger logger = LoggerFactory.getLogger(TraceAttachmentHelper.class);

    private TraceAttachmentHelper() {
        // Utility class
    }

    /**
     * Attach a Playwright trace file to the Allure report.
     * The trace can be opened with: npx playwright show-trace <file.zip>
     *
     * @param tracePath Path to the trace zip file
     * @param scenarioName Name of the scenario (used in attachment name)
     * @return true if attachment succeeded, false otherwise
     */
    public static boolean attachTraceToAllure(String tracePath, String scenarioName) {
        if (tracePath == null || tracePath.isEmpty()) {
            logger.debug("No trace path provided, skipping attachment");
            return false;
        }

        Path traceFile = Paths.get(tracePath);
        if (!Files.exists(traceFile)) {
            logger.warn("Trace file not found: {}", tracePath);
            return false;
        }

        try {
            byte[] traceBytes = Files.readAllBytes(traceFile);
            attachTrace(traceBytes, scenarioName);
            logger.info("Attached trace to Allure report: {} ({} KB)",
                    scenarioName, traceBytes.length / 1024);
            return true;
        } catch (IOException e) {
            logger.error("Failed to attach trace to Allure: {}", tracePath, e);
            return false;
        }
    }

    /**
     * Attach trace bytes to Allure report with proper MIME type.
     * The @Attachment annotation makes this visible in the Allure report.
     *
     * @param traceData Trace file bytes
     * @param name Attachment name
     * @return the trace data (required by @Attachment)
     */
    @Attachment(value = "Playwright Trace - {0}", type = "application/zip", fileExtension = ".zip")
    private static byte[] attachTrace(byte[] traceData, String name) {
        return traceData;
    }

    /**
     * Attach a screenshot to the Allure report.
     *
     * @param screenshotPath Path to the screenshot file
     * @param name Screenshot name
     * @return true if attachment succeeded, false otherwise
     */
    public static boolean attachScreenshotToAllure(String screenshotPath, String name) {
        if (screenshotPath == null || screenshotPath.isEmpty()) {
            logger.debug("No screenshot path provided, skipping attachment");
            return false;
        }

        Path screenshotFile = Paths.get(screenshotPath);
        if (!Files.exists(screenshotFile)) {
            logger.warn("Screenshot file not found: {}", screenshotPath);
            return false;
        }

        try {
            byte[] screenshotBytes = Files.readAllBytes(screenshotFile);
            attachScreenshot(screenshotBytes, name);
            logger.info("Attached screenshot to Allure report: {}", name);
            return true;
        } catch (IOException e) {
            logger.error("Failed to attach screenshot to Allure: {}", screenshotPath, e);
            return false;
        }
    }

    /**
     * Attach screenshot bytes to Allure report.
     *
     * @param imageData Screenshot bytes
     * @param name Attachment name
     * @return the image data (required by @Attachment)
     */
    @Attachment(value = "Screenshot - {0}", type = "image/png")
    private static byte[] attachScreenshot(byte[] imageData, String name) {
        return imageData;
    }

    /**
     * Attach video to Allure report.
     *
     * @param videoPath Path to the video file
     * @param name Video name
     * @return true if attachment succeeded, false otherwise
     */
    public static boolean attachVideoToAllure(String videoPath, String name) {
        if (videoPath == null || videoPath.isEmpty()) {
            logger.debug("No video path provided, skipping attachment");
            return false;
        }

        Path videoFile = Paths.get(videoPath);
        if (!Files.exists(videoFile)) {
            logger.warn("Video file not found: {}", videoPath);
            return false;
        }

        try {
            // For large video files, use streaming instead of loading entire file
            Allure.addAttachment("Video - " + name, "video/webm", new FileInputStream(videoFile.toFile()), ".webm");
            logger.info("Attached video to Allure report: {}", name);
            return true;
        } catch (IOException e) {
            logger.error("Failed to attach video to Allure: {}", videoPath, e);
            return false;
        }
    }

    /**
     * Attach text content (e.g., console logs, error messages) to Allure report.
     *
     * @param content Text content
     * @param name Attachment name
     */
    public static void attachTextToAllure(String content, String name) {
        if (content == null || content.isEmpty()) {
            logger.debug("No content provided, skipping text attachment");
            return;
        }
        attachText(content, name);
        logger.debug("Attached text to Allure report: {}", name);
    }

    /**
     * Attach text content to Allure report.
     *
     * @param content Text content
     * @param name Attachment name
     * @return the content (required by @Attachment)
     */
    @Attachment(value = "{0}", type = "text/plain")
    private static String attachText(String content, String name) {
        return content;
    }

    /**
     * Generate a shareable trace viewer link.
     * The trace.playwright.dev viewer can open remote trace files.
     *
     * @param traceUrl Public URL to the trace file (e.g., from CI artifacts)
     * @return Playwright trace viewer URL
     */
    public static String generateTraceViewerLink(String traceUrl) {
        if (traceUrl == null || traceUrl.isEmpty()) {
            return null;
        }
        return "https://trace.playwright.dev/?trace=" + traceUrl;
    }

    /**
     * Attach trace viewer instructions to Allure report.
     * Useful when trace file is too large to attach directly.
     *
     * @param tracePath Local path to the trace file
     */
    public static void attachTraceInstructions(String tracePath) {
        String instructions = String.format(
                "Playwright Trace Available\n" +
                "==========================\n\n" +
                "Trace file: %s\n\n" +
                "To view the trace:\n" +
                "1. Local viewing:\n" +
                "   npx playwright show-trace %s\n\n" +
                "2. Online viewer:\n" +
                "   - Upload trace to https://trace.playwright.dev/\n" +
                "   - Or drag & drop the file into the viewer\n\n" +
                "The trace contains:\n" +
                "  • DOM snapshots before/after each action\n" +
                "  • Network activity (requests/responses)\n" +
                "  • Console logs\n" +
                "  • Screenshots at each step\n" +
                "  • Timing information\n",
                tracePath, tracePath
        );
        attachTextToAllure(instructions, "Trace Viewer Instructions");
    }
}
