package com.macsuite.runners;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.TestNG;
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

    @Test
    public void stopsAfterOneRetryWithTheSkipListenerRegistered() {
        RetryFailureFixture.attempts.set(0);
        TestNG runner = new TestNG();
        runner.setUseDefaultListeners(false);
        runner.setVerbose(0);
        runner.addListener(new ScenarioRetryListener());
        runner.setTestClasses(new Class<?>[] { RetryFailureFixture.class });

        runner.run();

        Assert.assertEquals(RetryFailureFixture.attempts.get(), 2);
        Assert.assertTrue(runner.hasFailure());
    }
}

class RetryFailureFixture {
    static final AtomicInteger attempts = new AtomicInteger();

    @Test(retryAnalyzer = ScenarioRetryAnalyzer.class)
    public void failFirstTwoAttempts() {
        Assert.assertTrue(attempts.incrementAndGet() > 2, "Intentional failure");
    }
}
