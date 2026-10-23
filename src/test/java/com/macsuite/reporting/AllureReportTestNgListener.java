package com.macsuite.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;

/**
 * Generates an Allure HTML report automatically when the TestNG execution ends.
 */
public class AllureReportTestNgListener implements IExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(AllureReportTestNgListener.class);
    private static final DateTimeFormatter REPORT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final AtomicBoolean REPORT_GENERATION_ATTEMPTED = new AtomicBoolean(false);

    @Override
    public void onExecutionFinish() {
        if (!REPORT_GENERATION_ATTEMPTED.compareAndSet(false, true)) {
            return;
        }
        generateReport();
    }

    public void generateReport() {
        Path resultsDirectory = Paths
                .get(System.getProperty("allure.results.directory", "target/allure-results"))
                .toAbsolutePath()
                .normalize();

        if (!hasAllureResults(resultsDirectory)) {
            logger.warn("Skipping Allure report generation because no result files were found in {}", resultsDirectory);
            return;
        }

        Path reportRoot = Paths.get(System.getProperty("allure.report.base.directory", "report"))
                .toAbsolutePath()
                .normalize();
        Path reportDirectory = reportRoot.resolve("report_" + LocalDateTime.now().format(REPORT_TIMESTAMP));

        try {
            Files.createDirectories(reportRoot);
        } catch (IOException e) {
            logger.error("Unable to create Allure report root directory: {}", reportRoot, e);
            return;
        }

        Path allureCliHome = resolveAllureCliHome();
        if (allureCliHome == null) {
            logger.error("Allure CLI home was not found. Report generation skipped.");
            return;
        }

        try {
            logger.info("Generating Allure report at {}", reportDirectory);
            int exitCode = new ProcessBuilder(buildCommand(allureCliHome, resultsDirectory, reportDirectory))
                    .redirectErrorStream(true)
                    .inheritIO()
                    .start()
                    .waitFor();
            if (exitCode == 0) {
                logger.info("Allure report generated successfully: {}", reportDirectory);
            } else {
                logger.error("Allure report generation failed with exit code {}", exitCode);
            }
        } catch (IOException e) {
            logger.error("Failed to start the Allure CLI process.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Allure report generation was interrupted.", e);
        }
    }

    private static boolean hasAllureResults(Path resultsDirectory) {
        if (!Files.isDirectory(resultsDirectory)) {
            return false;
        }

        try (Stream<Path> files = Files.list(resultsDirectory)) {
            return files.anyMatch(Files::isRegularFile);
        } catch (IOException e) {
            logger.error("Unable to inspect Allure results directory: {}", resultsDirectory, e);
            return false;
        }
    }

    private static Path resolveAllureCliHome() {
        String configuredHome = System.getProperty("allure.cli.home");
        if (configuredHome != null && !configuredHome.isBlank()) {
            Path home = Paths.get(configuredHome).toAbsolutePath().normalize();
            if (Files.isDirectory(home)) {
                return home;
            }
        }

        String environmentHome = System.getenv("ALLURE_HOME");
        if (environmentHome != null && !environmentHome.isBlank()) {
            Path home = Paths.get(environmentHome).toAbsolutePath().normalize();
            if (Files.isDirectory(home)) {
                return home;
            }
        }

        return null;
    }

    private static List<String> buildCommand(Path allureCliHome, Path resultsDirectory, Path reportDirectory) {
        Path binDirectory = allureCliHome.resolve("bin");
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        List<String> command = new ArrayList<>();

        if (windows) {
            command.add("cmd");
            command.add("/c");
            command.add(binDirectory.resolve("allure.bat").toString());
        } else {
            command.add(binDirectory.resolve("allure").toString());
        }

        command.add("generate");
        command.add(resultsDirectory.toString());
        command.add("-o");
        command.add(reportDirectory.toString());
        command.add("--clean");
        command.add("--single-file");
        return command;
    }
}