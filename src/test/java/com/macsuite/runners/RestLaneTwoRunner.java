package com.macsuite.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = {
        "src/test/resources/features/outreach/outreach-general-e2e.feature",
        "src/test/resources/features/outreach/outreach-emergency-e2e.feature",
        "src/test/resources/features/outreach/outreach-emergency-smoke.feature",
        "src/test/resources/features/outreach/outreach-listview-dashboard-reports.feature",
        "src/test/resources/features/pier/pier-issue-lifecycle.feature",
        "src/test/resources/features/pier/pier-states-and-cases.feature",
        "src/test/resources/features/pier/pier-listview-dashboard-reports.feature",
        "src/test/resources/features/sdp/sdp-records-e2e.feature"
    },
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
    tags = "not @ignore and not (@bct-dashboard-folder or @bct-reports-all or @hmh-dashboard or @hmh-reports or @pra-dashboard or @pra-reports or @pier-dashboard or @pier-reports or @outreach-dashboard or @outreach-reports or @cmcs-ta-dashboard-navigation or @cmcs-ta-reports-validation or @wftcl-impl-dashboard or @wftcl-impl-reports)",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/rest-lane-2/index.html",
        "json:target/cucumber-reports/rest-lane-2/cucumber.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = false,
    publish = false
)
public class RestLaneTwoRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}