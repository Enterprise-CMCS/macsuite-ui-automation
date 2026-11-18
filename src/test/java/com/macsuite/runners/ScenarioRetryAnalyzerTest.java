package com.macsuite.runners;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;

public class ScenarioRetryAnalyzerTest {

    @Test
    public void retriesEachScenarioAtMostOnce() {
        String firstScenario = "first-scenario-" + System.nanoTime();
        String secondScenario = "second-scenario-" + System.nanoTime();

        Assert.assertTrue(ScenarioRetryAnalyzer.shouldRetry(firstScenario, ITestResult.FAILURE));
        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(firstScenario, ITestResult.FAILURE));
        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(firstScenario, ITestResult.FAILURE));
        Assert.assertTrue(ScenarioRetryAnalyzer.shouldRetry(secondScenario, ITestResult.FAILURE));
        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(secondScenario, ITestResult.FAILURE));
        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(secondScenario, ITestResult.FAILURE));
    }

    @Test
    public void doesNotRetryPassingOrSkippedScenarios() {
        String scenario = "completed-scenario-" + System.nanoTime();

        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(scenario, ITestResult.SUCCESS));
        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(scenario, ITestResult.SKIP));
        Assert.assertTrue(ScenarioRetryAnalyzer.shouldRetry(scenario, ITestResult.FAILURE));
        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(scenario, ITestResult.FAILURE));
        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(scenario, ITestResult.FAILURE));
    }

    @Test
    public void cucumberScenarioMethodUsesRetryAnalyzer() throws NoSuchMethodException {
        Test scenarioTest = RetryableCucumberTestRunner.class
                .getMethod("runScenario", PickleWrapper.class, FeatureWrapper.class)
                .getAnnotation(Test.class);

        Assert.assertNotNull(scenarioTest);
        Assert.assertEquals(scenarioTest.retryAnalyzer(), ScenarioRetryAnalyzer.class);
    }
}
