package com.macsuite.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = {"src/test/resources/features/cmcs-ta/cmcs-ta-e2e-full-flow.feature"},
    glue = {
        "com.macsuite.bct.steps",
        "com.macsuite.access_control.steps",
        "com.macsuite.vendor_engagement.steps",
        "com.macsuite.hiring_metrics.steps",
        "com.macsuite.pier.steps",
        "com.macsuite.pra.steps",
        "com.macsuite.outreach.steps",
        "com.macsuite.inquiry_tracker.steps",
        "com.macsuite.sdp.steps",
        "com.macsuite.wftcl.steps",
        "com.macsuite.common.hooks",
        "com.macsuite.common.steps"
    },
    tags = "not @ignore",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cmcs-ta-e2e/index.html",
        "json:target/cucumber-reports/cmcs-ta-e2e/cucumber.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = false,
    publish = false
)
public class CmcsTaE2ELaneRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}