package com.macsuite.runners;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;

public class ScenarioRetryAnalyzerTest {

    @Test
    public void retriesEachScenarioAtMostOnce() {
        String firstScenario = "first-scenario-" + System.nanoTime();
        String secondScenario = "second-scenario-" + System.nanoTime();

        Assert.assertTrue(ScenarioRetryAnalyzer.shouldRetry(firstScenario));
        Assert.assertFalse(ScenarioRetryAnalyzer.shouldRetry(firstScenario));
        Assert.assertTrue(ScenarioRetryAnalyzer.shouldRetry(secondScenario));
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
