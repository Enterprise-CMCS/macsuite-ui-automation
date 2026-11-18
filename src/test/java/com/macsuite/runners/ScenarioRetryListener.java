package com.macsuite.runners;

import org.testng.ITestContext;
import org.testng.ITestListener;

public class ScenarioRetryListener implements ITestListener {

    @Override
    public void onFinish(ITestContext context) {
        // Keep retry counts until the context ends, including attempts reported as skipped.
        ScenarioRetryAnalyzer.clearAllRetries();
    }
}
