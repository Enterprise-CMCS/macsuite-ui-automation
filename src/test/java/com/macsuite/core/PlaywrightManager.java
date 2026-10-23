package com.macsuite.core;

import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.macsuite.config.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;

/**
 * Singleton PlaywrightManager for thread-safe browser instance management.
 * Uses ThreadLocal to ensure each test thread gets its own browser instance.
 * Supports screenshot and video recording for test failures.
 */
public class PlaywrightManager {
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightManager.class);
    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> videoNameThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> traceActiveThreadLocal = new ThreadLocal<>();

    private PlaywrightManager() {
        // Singleton
    }

    private static Tracing.StartOptions traceStartOptions() {
        // Keep tracing lightweight for long Salesforce scenarios to avoid Node heap
        // exhaustion.
        boolean enableSnapshots = Boolean.parseBoolean(System.getProperty("TRACE_SNAPSHOTS", "false"));
        return new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(enableSnapshots);
    }

    /**
     * Initialize Playwright and launch browser for the current thread
     */
    public static void init() {
        init(null, null);
    }

    /**
     * Initialize Playwright and launch browser for the current thread with custom
     * video name
     * 
     * @param videoName Custom video file name (without extension)
     */
    public static void init(String videoName) {
        init(videoName, null);
    }

    /**
     * Initialize Playwright and launch browser for the current thread with custom
     * video name and auth state
     * 
     * @param videoName     Custom video file name (without extension)
     * @param authStateFile Path to authentication state JSON file (e.g.,
     *                      "auth-states/qa/bct-qa.json")
     */
    public static void init(String videoName, String authStateFile) {
        if (playwrightThreadLocal.get() != null) {
            logger.warn("Playwright already initialized for thread: {}", Thread.currentThread().getId());
            return;
        }

        try {
            Playwright playwright = Playwright.create();
            playwrightThreadLocal.set(playwright);

            boolean headless = TestConfig.isHeadless();
            double slowMo = resolveSlowMo();
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(headless)
                            .setSlowMo(slowMo)
                            .setArgs(Arrays.asList(
                                    "--disable-blink-features=AutomationControlled",
                                    "--start-maximized" // Maximize window on start
                            )));
            browserThreadLocal.set(browser);

            // Generate video file name with feature-scenario-timestamp
            String videoFileName = videoName != null ? videoName : "test-" + System.currentTimeMillis();
            videoNameThreadLocal.set(videoFileName);

            // Configure browser context with optional authentication state
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(null) // Use full window size (maximized)
                    .setRecordVideoDir(Paths.get("target/videos"))
                    .setRecordVideoSize(1920, 1080);

            // Load authentication state if provided and file exists
            boolean authStateLoaded = false;
            if (authStateFile != null && !authStateFile.isEmpty()) {
                java.nio.file.Path authPath = Paths.get(authStateFile);
                if (java.nio.file.Files.exists(authPath)) {
                    contextOptions.setStorageState(authPath.toString());
                    authStateLoaded = true;
                    logger.info("Loading authentication state from: {}", authStateFile);
                } else {
                    logger.warn("Authentication state file not found: {}. Starting without saved state.",
                            authStateFile);
                }
            }

            BrowserContext context = browser.newContext(contextOptions);
            contextThreadLocal.set(context);

            context.tracing().start(traceStartOptions());
            traceActiveThreadLocal.set(true);

            Page page = context.newPage();
            page.setDefaultTimeout(30000);
            page.setDefaultNavigationTimeout(30000);
            pageThreadLocal.set(page);

            logger.info(
                    "PlaywrightManager initialized for thread: {} (headless: {}, video: {}, authState: {}, maximized: true)",
                    Thread.currentThread().getId(), headless, videoFileName, authStateLoaded ? authStateFile : "none");
        } catch (Exception e) {
            logger.error("Failed to initialize Playwright", e);
            throw new RuntimeException("Playwright initialization failed", e);
        }
    }

    private static double resolveSlowMo() {
        // Accept SLOW_MODE (canonical, matches TestConfig) or legacy slowMo alias
        String val = System.getProperty("SLOW_MODE", System.getProperty("slowMo", "800")).trim();
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException ex) {
            logger.warn("Invalid SLOW_MODE value '{}'. Falling back to 800ms.", val);
            return 800;
        }
    }

    /**
     * Get the Page object for the current thread
     */
    public static Page page() {
        Page page = pageThreadLocal.get();
        if (page == null) {
            throw new RuntimeException("Page not initialized. Call PlaywrightManager.init() first.");
        }
        return page;
    }

    /**
     * Set the active page for the current thread, for flows that open a popup.
     */
    public static void setActivePage(Page page) {
        if (page == null || page.isClosed()) {
            throw new IllegalArgumentException("Active page must be open and non-null.");
        }
        pageThreadLocal.set(page);
    }

    /**
     * Get the BrowserContext for the current thread
     */
    public static BrowserContext context() {
        BrowserContext context = contextThreadLocal.get();
        if (context == null) {
            throw new RuntimeException("BrowserContext not initialized. Call PlaywrightManager.init() first.");
        }
        return context;
    }

    /**
     * Get the Browser for the current thread
     */
    public static Browser browser() {
        Browser browser = browserThreadLocal.get();
        if (browser == null) {
            throw new RuntimeException("Browser not initialized. Call PlaywrightManager.init() first.");
        }
        return browser;
    }

    /**
     * Get the Playwright instance for the current thread
     */
    public static Playwright playwright() {
        Playwright playwright = playwrightThreadLocal.get();
        if (playwright == null) {
            throw new RuntimeException("Playwright not initialized. Call PlaywrightManager.init() first.");
        }
        return playwright;
    }

    /**
     * Check whether Playwright has been initialized on the current thread.
     */
    public static boolean isInitialized() {
        return playwrightThreadLocal.get() != null
                && browserThreadLocal.get() != null
                && contextThreadLocal.get() != null
                && pageThreadLocal.get() != null;
    }

    /**
     * Take a screenshot of the current page
     * 
     * @param screenshotName Name for the screenshot file
     * @return Path to the saved screenshot
     */
    public static String takeScreenshot(String screenshotName) {
        try {
            Page page = page();

            // Create screenshots directory if it doesn't exist
            java.nio.file.Path screenshotsDir = Paths.get("target/screenshots");
            if (!java.nio.file.Files.exists(screenshotsDir)) {
                java.nio.file.Files.createDirectories(screenshotsDir);
                logger.info("Created screenshots directory: {}", screenshotsDir);
            }

            String screenshotPath = "target/screenshots/" + screenshotName + "_" + System.currentTimeMillis() + ".png";
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)).setFullPage(true));
            logger.info("Screenshot saved: {}", screenshotPath);
            return screenshotPath;
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", screenshotName, e);
            return null;
        }
    }

    /**
     * Save the current context's authentication state (cookies + localStorage) to a
     * file.
     * Call this after a successful login to enable session reuse across scenarios.
     *
     * @param path file path to write the JSON state to; parent directories are
     *             created automatically
     */
    public static void saveAuthState(String path) {
        BrowserContext ctx = contextThreadLocal.get();
        if (ctx == null) {
            logger.warn("saveAuthState: no active context on thread {}", Thread.currentThread().getId());
            return;
        }
        try {
            java.nio.file.Path filePath = Paths.get(path);
            if (filePath.getParent() != null) {
                java.nio.file.Files.createDirectories(filePath.getParent());
            }
            ctx.storageState(new BrowserContext.StorageStateOptions().setPath(filePath));
            logger.info("Auth state saved: {}", path);
        } catch (Exception e) {
            logger.warn("Failed to save auth state to {}: {}", path, e.getMessage());
        }
    }

    /**
     * Replace the active page and context with a new context loaded from a saved
     * auth state.
     * The browser process is reused; only the BrowserContext and Page are
     * recreated.
     * Any existing tracing session is stopped before the swap.
     *
     * @param authStateFile path to the auth-state JSON produced by
     *                      {@link #saveAuthState}
     */
    public static void reinitContext(String authStateFile) {
        Page oldPage = pageThreadLocal.get();
        BrowserContext oldCtx = contextThreadLocal.get();

        // Tear down existing page and context cleanly
        if (oldPage != null) {
            try {
                oldPage.close();
            } catch (Exception ignored) {
            }
            pageThreadLocal.remove();
        }
        if (oldCtx != null) {
            Boolean traceActive = traceActiveThreadLocal.get();
            if (Boolean.TRUE.equals(traceActive)) {
                try {
                    oldCtx.tracing().stop();
                } catch (Exception ignored) {
                }
                traceActiveThreadLocal.remove();
            }
            try {
                oldCtx.close();
            } catch (Exception ignored) {
            }
            contextThreadLocal.remove();
        }

        try {
            Browser browser = browserThreadLocal.get();
            Browser.NewContextOptions opts = new Browser.NewContextOptions()
                    .setViewportSize(null)
                    .setRecordVideoDir(Paths.get("target/videos"))
                    .setRecordVideoSize(1920, 1080);

            java.nio.file.Path authPath = Paths.get(authStateFile);
            if (java.nio.file.Files.exists(authPath)) {
                opts.setStorageState(authPath.toString());
                logger.info("reinitContext: loading auth state from {}", authStateFile);
            } else {
                logger.warn("reinitContext: auth state file not found: {}", authStateFile);
            }

            BrowserContext context = browser.newContext(opts);
            contextThreadLocal.set(context);

            context.tracing().start(traceStartOptions());
            traceActiveThreadLocal.set(true);

            Page page = context.newPage();
            page.setDefaultTimeout(30000);
            page.setDefaultNavigationTimeout(30000);
            pageThreadLocal.set(page);

            logger.info("Context reinitialized with auth state on thread {}", Thread.currentThread().getId());
        } catch (Exception e) {
            logger.error("reinitContext failed", e);
            throw new RuntimeException("Failed to reinitialize context with auth state", e);
        }
    }

    /**
     * Close all resources for the current thread, optionally saving a Playwright
     * trace.
     * 
     * @param scenarioFailed true if the scenario failed — trace zip will be saved
     *                       to target/traces/
     * @param traceName      file name (without extension) for the trace zip
     */
    public static void close(boolean scenarioFailed, String traceName) {
        try {
            Page page = pageThreadLocal.get();
            // Capture the Video reference before closing the page — video().path() is only
            // available after context.close(), but the Video object must be retrieved
            // first.
            Video video = (page != null) ? page.video() : null;

            if (page != null) {
                page.close();
                pageThreadLocal.remove();
            }

            BrowserContext context = contextThreadLocal.get();
            if (context != null) {
                Boolean traceActive = traceActiveThreadLocal.get();
                if (Boolean.TRUE.equals(traceActive)) {
                    if (scenarioFailed && traceName != null) {
                        try {
                            java.nio.file.Path tracesDir = Paths.get("target/traces");
                            if (!java.nio.file.Files.exists(tracesDir)) {
                                java.nio.file.Files.createDirectories(tracesDir);
                            }
                            java.nio.file.Path tracePath = tracesDir.resolve(traceName + ".zip");
                            context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                            logger.info("Trace saved: {}", tracePath);
                        } catch (Exception e) {
                            logger.warn("Could not save trace: {}", e.getMessage());
                            context.tracing().stop();
                        }
                    } else {
                        context.tracing().stop();
                    }
                    traceActiveThreadLocal.remove();
                }

                context.close(); // video file is finalized here
                contextThreadLocal.remove();

                String videoName = videoNameThreadLocal.get();
                if (video != null && videoName != null) {
                    renameVideoFile(video, videoName);
                }
            }

            Browser browser = browserThreadLocal.get();
            if (browser != null) {
                browser.close();
                browserThreadLocal.remove();
            }

            Playwright playwright = playwrightThreadLocal.get();
            if (playwright != null) {
                playwright.close();
                playwrightThreadLocal.remove();
            }

            videoNameThreadLocal.remove();
            logger.info("PlaywrightManager closed for thread: {}", Thread.currentThread().getId());
        } catch (Exception e) {
            logger.error("Error closing PlaywrightManager", e);
        }
    }

    /**
     * Close all resources for the current thread, discarding the trace.
     */
    public static void close() {
        close(false, null);
    }

    /**
     * Stop the current trace and immediately start a fresh trace for the next
     * scenario
     * without closing browser/page resources.
     *
     * @param scenarioFailed true if the finished scenario failed
     * @param traceName      file name (without extension) for failed scenario trace
     * @return saved trace path when scenario failed and trace was written;
     *         otherwise null
     */
    public static String rotateTraceForScenario(boolean scenarioFailed, String traceName) {
        BrowserContext context = contextThreadLocal.get();
        if (context == null) {
            return null;
        }

        String savedTracePath = null;
        try {
            Boolean traceActive = traceActiveThreadLocal.get();
            if (Boolean.TRUE.equals(traceActive)) {
                if (scenarioFailed && traceName != null) {
                    java.nio.file.Path tracesDir = Paths.get("target/traces");
                    if (!java.nio.file.Files.exists(tracesDir)) {
                        java.nio.file.Files.createDirectories(tracesDir);
                    }
                    java.nio.file.Path tracePath = tracesDir.resolve(traceName + ".zip");
                    context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                    savedTracePath = tracePath.toString().replace('\\', '/');
                    logger.info("Scenario trace saved: {}", savedTracePath);
                } else {
                    context.tracing().stop();
                }
            }
        } catch (java.io.IOException e) {
            logger.warn("Could not rotate trace after scenario due to IO issue: {}", e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Could not rotate trace after scenario: {}", e.getMessage());
        }

        try {
            context.tracing().start(traceStartOptions());
            traceActiveThreadLocal.set(true);
        } catch (RuntimeException e) {
            traceActiveThreadLocal.set(false);
            logger.warn("Could not start fresh trace for next scenario: {}", e.getMessage());
        }

        return savedTracePath;
    }

    private static void renameVideoFile(Video video, String desiredName) {
        try {
            java.nio.file.Path originalPath = video.path();
            if (originalPath == null) {
                return;
            }
            java.nio.file.Path newPath = originalPath.getParent().resolve(desiredName + ".webm");
            java.nio.file.Files.move(originalPath, newPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("Video saved as: {}", newPath);
        } catch (Exception e) {
            logger.debug("Could not rename video file: {}", e.getMessage());
        }
    }
}
