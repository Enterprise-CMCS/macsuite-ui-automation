package com.macsuite.runners;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import io.cucumber.testng.PickleWrapper;

public class ScenarioRetryAnalyzer implements IRetryAnalyzer {

    private static final ConcurrentMap<String, AtomicInteger> RETRY_COUNTS = new ConcurrentHashMap<>();

    @Override
    public boolean retry(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters.length > 0 && parameters[0] instanceof PickleWrapper pickleWrapper
                && pickleWrapper.getPickle().getTags().contains("@no-retry")) {
            return false;
        }
        return shouldRetry(scenarioKey(result));
    }

    static boolean shouldRetry(String scenarioKey) {
        AtomicInteger retryCount = RETRY_COUNTS.computeIfAbsent(scenarioKey, key -> new AtomicInteger());
        boolean shouldRetry = retryCount.getAndIncrement() == 0;
        if (!shouldRetry) {
            RETRY_COUNTS.remove(scenarioKey, retryCount);
        }
        return shouldRetry;
    }

    static void clearRetry(ITestResult result) {
        RETRY_COUNTS.remove(scenarioKey(result));
    }

    private static String scenarioKey(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters.length > 0 && parameters[0] instanceof PickleWrapper pickleWrapper) {
            var pickle = pickleWrapper.getPickle();
            return result.getTestClass().getName() + "|" + pickle.getUri() + "|" + pickle.getLine() + "|"
                    + pickle.getName();
        }
        return result.getTestClass().getName() + "|" + result.getMethod().getQualifiedName();
    }
}
