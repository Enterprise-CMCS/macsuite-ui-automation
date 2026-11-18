package com.macsuite.runners;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class ScenarioRetryListener implements ITestListener {

    @Override
    public void onTestSuccess(ITestResult result) {
        ScenarioRetryAnalyzer.clearRetry(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // TestNG reports a failed attempt scheduled for retry as skipped.
        if (!result.wasRetried()) {
            ScenarioRetryAnalyzer.clearRetry(result);
        }
    }
}
