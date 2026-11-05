package com.macsuite.config;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EnvironmentResolverTest {
    @DataProvider
    public Object[][] environmentSelections() {
        return new Object[][] {
                {"SEI", "true", "UAT", "UAT"},
                {"UAT", "true", "QA", "QA"},
                {"QA", "true", "SEI", "SEI"},
                {"SEI", "true", " qa ", "QA"},
                {"SEI", null, "UAT", "SEI"},
                {"UAT", "false", "QA", "UAT"},
                {"SEI", "true", null, "SEI"},
                {"QA", "true", "", "QA"}
        };
    }

    @Test(dataProvider = "environmentSelections")
    public void overridesFeatureEnvironmentOnlyInConfiguredGithubActionsRuns(
            String featureEnvironment, String githubActions, String pipelineEnvironment, String expected) {
        Assert.assertEquals(EnvironmentResolver.resolveEnvironment(
                featureEnvironment, githubActions, pipelineEnvironment), expected);
    }

    @Test
    public void rejectsUnsupportedPipelineEnvironment() {
        Assert.expectThrows(IllegalArgumentException.class,
                () -> EnvironmentResolver.resolveEnvironment("UAT", "true", "PROD"));
    }
}