package com.macsuite.runners;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

import com.macsuite.reporting.AllureReportTestNgListener;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Test runner for MACSuite UI Automation.
 * 
 * Tag filtering: -Dcucumber.filter.tags="@tag" (default: "not @ignore")
 * Parallel execution: -Dthreads=N (default: 1 for sequential)
 * 
 * Examples:
 * mvn clean test -Dcucumber.filter.tags="@demo"
 * mvn clean test -Dcucumber.filter.tags="@demo" -Dthreads=4
 * mvn clean test -Dcucumber.filter.tags="@bct and @critical"
 * 
 * Note: Cucumber's native cucumber.filter.tags property overrides this default.
 */
@Listeners(AllureReportTestNgListener.class)
@CucumberOptions(

        features = { "src/test/resources/features" }, glue = {
                "com.macsuite.bct.steps",
                "com.macsuite.access_control.steps",
                "com.macsuite.vendor_engagement.steps",
                "com.macsuite.hiring_metrics.steps",
                "com.macsuite.pier.steps",
                "com.macsuite.pra.steps",
                "com.macsuite.MFP_POH.steps",
                "com.macsuite.outreach.steps",
                "com.macsuite.inquiry_tracker.steps",
                "com.macsuite.sdp.steps",
                "com.macsuite.wftcl.steps",
                "com.macsuite.common.hooks",
                "com.macsuite.common.steps"
        },
        // tags = "not @ignore",
        tags = "@pier_home_header", 
        plugin = {
                "pretty",
                "html:target/cucumber-reports/index.html",
                "json:target/cucumber-reports/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }, monochrome = false, publish = false // keep false — publishing would expose internal test data to the public
                                               // Cucumber report service
)
public class MACRunner extends AbstractTestNGCucumberTests {

    static {
        String tags = System.getProperty("tags");
        if (tags != null && !tags.trim().isEmpty()) {
            System.setProperty("cucumber.filter.tags", tags);
        }
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @AfterSuite(alwaysRun = true)
    public void generateAllureReport() {
        new AllureReportTestNgListener().onExecutionFinish();
    }
}
