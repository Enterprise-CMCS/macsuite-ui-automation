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
        return shouldRetry(scenarioKey(result), result.getStatus());
    }

    static boolean shouldRetry(String scenarioKey, int status) {
        if (status != ITestResult.FAILURE) {
            return false;
        }

        AtomicInteger retryCount = RETRY_COUNTS.computeIfAbsent(scenarioKey, key -> new AtomicInteger());
        return retryCount.getAndIncrement() == 0;
    }

    static void clearAllRetries() {
        RETRY_COUNTS.clear();
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
