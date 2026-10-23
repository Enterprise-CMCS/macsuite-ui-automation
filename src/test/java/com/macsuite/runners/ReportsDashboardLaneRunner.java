package com.macsuite.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = {"src/test/resources/features"},
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
    tags = "not @ignore and (@bct-dashboard-folder or @bct-reports-all or @hmh-dashboard or @hmh-reports or @pra-dashboard or @pra-reports or @pier-dashboard or @pier-reports or @outreach-dashboard or @outreach-reports or @cmcs-ta-dashboard-navigation or @cmcs-ta-reports-validation or @wftcl-impl-dashboard or @wftcl-impl-reports)",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/reports-dashboard/index.html",
        "json:target/cucumber-reports/reports-dashboard/cucumber.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = false,
    publish = false
)
public class ReportsDashboardLaneRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}