# MACSuite UI Automation Framework

MACSuite_UI_Automation is a Playwright + Cucumber + TestNG framework for Salesforce-based MACSuite modules.

## Current Scope

Implemented modules in this repository:

- BCT
- Access Control
- Vendor Engagement
- Hiring Metrics Hub
- Inquiry Tracker
- Outreach
- PIER
- PRA

Remaining migration scope is tracked in `MIGRATION_PLAN.md`.

## Stack

- Java 25 (latest LTS)
- Playwright 1.47.0
- Cucumber 7.18.0
- TestNG 7.10.2
- Maven
- Allure

## Project Structure

```text
MACSuite_UI_Automation/
├── src/test/java/com/macsuite/core/
│   └── PlaywrightManager.java
├── src/test/java/com/macsuite/
│   ├── common/
│   ├── access_control/
│   ├── bct/
│   ├── hiring_metrics/
│   ├── inquiry_tracker/
│   ├── outreach/
│   ├── pier/
│   ├── pra/
│   ├── vendor_engagement/
│   └── runners/
├── src/test/resources/
│   ├── config/
│   ├── features/
│   │   ├── access-control/
│   │   ├── bct/
│   │   ├── hiring-metrics/
│   │   ├── inquiry-tracker/
│   │   ├── outreach/
│   │   ├── pier/
│   │   ├── pra/
│   │   └── vendor-engagement/
│   └── logback.xml
├── pom.xml
├── testng.xml
├── testng-parallel-lanes.xml
└── README.md
```

## Setup

### Prerequisites

- Java 25+
- Maven 3.9+
- Valid Salesforce credentials in `.env`

### Core Configuration

Typical `.env` values:

```bash
HEADLESS=true
APP_URL=https://cmcsmcqa.lightning.force.com
APP_URL_UAT=https://cmcsmcuat.lightning.force.com
APP_URL_SEI=https://seimcuat.lightning.force.com
```

Role-specific credentials are also required, for example:

```bash
BCT_QA_USERNAME=user@company.com
BCT_QA_PASSWORD=password
SO_UAT_USERNAME=user@company.com
SO_UAT_PASSWORD=password
```

## Running Tests

Run the default suite (sequential for stability):

```bash
mvn clean test
```

Run by tag:

```bash
mvn clean test -Dtags="@bct"
mvn clean test -Dtags="@access-control"
mvn clean test -Dtags="@vendor-engagement"
mvn clean test -Dtags="@hiring-metrics"
mvn clean test -Dtags="@inquiry-tracker"
mvn clean test -Dtags="@outreach"
mvn clean test -Dtags="@pier"
mvn clean test -Dtags="@pra"
mvn clean test -Dtags="@critical"
mvn clean test -Dtags="@MFP_POH"
mvn clean test -Dtags="@demo"
mvn clean test -Dtags="@bct-reports-all"
mvn clean test -Dtags="@cmcs-ta-list-view-operation"
mvn clean test -Dtags="@cmcs-ta-reports-validation"
mvn test -Dtags="@cmcs-ta-wftcl-creation"
mvn test -Dtags="@cmcs-ta-response-details"
```

Run with tag combinations:

```bash
mvn clean test -Dcucumber.filter.tags="@bct and @critical"
mvn clean test -Dcucumber.filter.tags="@demo or @critical"
mvn clean test -Dcucumber.filter.tags="@smoke and not @ignore"
```

Run by tag with parallel execution:

```bash
mvn clean test -Dcucumber.filter.tags="@demo" -Dthreads=4
```

### Parallel Lane Execution

Use `testng-parallel-lanes.xml` when the full suite needs to finish faster than a single-browser sequential run. This suite runs four TestNG lanes at the same time:

- Reports and dashboard lane: fast dashboard/report validation scenarios
- CMCS TA E2E lane: `cmcs-ta-e2e-full-flow.feature`, including the long 20+ minute lifecycle
- Regression lane 1: BCT, WFTCL, Hiring Metrics, PRA, and CMCS TA WFTCL non-report workflows
- Regression lane 2: Outreach, PIER, SDP, and Access Control non-report workflows

Recommended fast-run command:

```bash
mvn clean test "-DsuiteXmlFile=testng-parallel-lanes.xml" "-DHEADLESS=true" "-DSLOW_MODE=0" "-DHIGHLIGHT_ELEMENTS=false" "-DSCREENSHOT_MODE=error"
```

In PowerShell, keep each `-D...` property wrapped in quotes. Otherwise Maven can receive part of the argument as a separate lifecycle phase, such as `.xml`.

Notes:

- The default `testng.xml` is unchanged and remains the standard stable run.
- Each lane runs its assigned scenarios sequentially, while TestNG runs the four lanes in parallel.
- The framework currently creates and closes a browser per scenario through `CommonHooks`; this avoids shared Salesforce state between scenarios while still reducing total wall-clock time.
- If Salesforce or the local machine struggles with four browsers, run the default suite or reduce the lane count in `testng-parallel-lanes.xml`.

Run with browser visible:

```bash
mvn clean test -DHEADLESS=false
```

Run with slow mode:

```bash
mvn clean test -DSLOW_MODE=1000
```

Run with parallel execution (faster but resource-intensive):

```bash
mvn clean test -Dthreads=2  # 2 parallel threads
mvn clean test -Dthreads=4  # 4 parallel threads
```

Run with custom memory allocation:

```bash
mvn clean test -DargLine="-Xmx4g"
```

## Reports

Generate and view Allure report:

```bash
mvn allure:report
mvn allure:serve
```

Run tests and generate report (combined):

```bash
# Run all tests then generate report
mvn clean test; mvn allure:report

# Run @demo tests then serve report
mvn clean test -Dcucumber.filter.tags="@demo"; mvn allure:serve

# Run @demo tests and open report (one line)
mvn clean test -Dcucumber.filter.tags="@demo" && mvn allure:serve
```

## Troubleshooting

### Tests Pass in Eclipse but Fail in Maven CMD

**Symptoms**: Tests run successfully in Eclipse IDE but fail with `EOFException` or timeout errors when running `mvn clean test`

**Root Causes**:

- Parallel execution with insufficient resources
- Maven Surefire JVM crashes due to memory constraints
- Browser instance accumulation across parallel threads

**Fixes Applied**:

1. **Sequential execution by default** (thread count = 1)
   - Use `-Dthreads=N` to enable parallel execution with N threads
2. **Memory allocation** (3GB heap with G1GC in pom.xml)
   - Override with `-DargLine="-Xmx4g"` if needed
3. **Single fork with reuse** (`forkCount=1`, `reuseForks=true`)
   - Prevents resource fragmentation across multiple JVM forks

**Recommended Commands**:

```bash
# Stable sequential execution (default)
mvn clean test

# Fast parallel execution (requires more resources)
mvn clean test -Dthreads=2

# Maximum stability for large test suites
mvn clean test -DargLine="-Xmx4g" -DHEADLESS=true
```

### Common Errors

**`java.io.EOFException` in Surefire**: Increase memory with `-DargLine="-Xmx4g"`

**`TimeoutError` during screenshot**: Disable parallel execution with default settings

**Browser not closing**: Verify `PlaywrightManager.close()` is called in hooks

### Tag Filtering Not Working

**Symptom**: Running a tag-filtered command executes all tests instead of just the tagged tests

**Solution**: Use Cucumber's native `cucumber.filter.tags` property. Ensure you:

1. Use `mvn clean test` (not just `mvn test`) to clear previous results
2. Use `-Dcucumber.filter.tags="@demo"`, not the old `-Dtags="@demo"` alias
3. Verify your feature files have the correct tags

**Tag Expression Examples**:

```bash
# Single tag
mvn clean test -Dcucumber.filter.tags="@demo"

# AND condition (must have both tags)
mvn clean test -Dcucumber.filter.tags="@bct and @critical"

# OR condition (can have either tag)
mvn clean test -Dcucumber.filter.tags="@demo or @smoke"

# NOT condition (exclude tag)
mvn clean test -Dcucumber.filter.tags="not @ignore"

# Complex expressions
mvn clean test -Dcucumber.filter.tags="(@bct or @demo) and not @ignore"
```

## Implemented Features

### BCT

Feature file: `src/test/resources/features/bct/bct-contract-creation.feature`

- contract creation
- edit flow
- notes
- field history tracking
- action menu

### Access Control

Feature file: `src/test/resources/features/access-control/access-control-user-validation.feature`

- role-based app visibility
- environment access validation
- unauthorized app checks

### Vendor Engagement

Feature file: `src/test/resources/features/vendor-engagement/vendor-engagement-meeting.feature`

- meeting creation
- detail verification

### Hiring Metrics Hub

Feature file: `src/test/resources/features/hiring-metrics/hiring-metrics-hub-workflow.feature`

- record creation
- multi-stage status transitions
- notes workflow
- field history validation

### Inquiry Tracker

Feature files:

- `src/test/resources/features/inquiry-tracker/inquiry-tracker-general-creation.feature`
- `src/test/resources/features/inquiry-tracker/inquiry-tracker-escalation.feature`
- `src/test/resources/features/inquiry-tracker/inquiry-tracker-stage-navigation.feature`

- inquiry creation and required field handling
- escalation and stage movement
- stage-navigation behavior

### Outreach

Feature files:

- `src/test/resources/features/outreach/outreach-emergency-smoke.feature`
- `src/test/resources/features/outreach/outreach-legacy-mapped.feature`

- emergency outreach lifecycle smoke flow
- legacy mapping placeholder scenarios (ignored)

### PIER

Feature file: `src/test/resources/features/pier/pier-issue-smoke.feature`

- issue smoke workflow

### PRA

Feature files:

- `src/test/resources/features/pra/pra-icr-workflow.feature`
- `src/test/resources/features/pra/pra-icr-standalone-workflow.feature`
- `src/test/resources/features/pra/pra-icr-macpro-workflow.feature`

- ICR workflow coverage across variants

## Framework Notes

- `PlaywrightManager` owns thread-safe browser lifecycle.
- `FormUtils` centralizes Salesforce form interactions.
- Shared behavior belongs in common helpers, not duplicated in module code.

## Documentation Policy

Root-level markdown documentation is intentionally limited to:

- `README.md`
- `MIGRATION_PLAN.md`

One-off migration notes, implementation summaries, and temporary planning snapshots should not be kept in the repo root.

## Debug Utilities

Pause browser with Playwright Inspector:

```java
page().pause();  // Browser pauses and opens Playwright Inspector
```

### Take Screenshots

```java
screenshot("contract-form-filled");
// Saves to: target/screenshots/{timestamp}_contract-form-filled.png
```

## Additional Troubleshooting

### Tests fail with "Credentials not found"

**Solution:** Ensure `.env` file is in project root with all role/environment combinations filled:

```bash
{ROLE}_{ENVIRONMENT}_USERNAME=...
{ROLE}_{ENVIRONMENT}_PASSWORD=...
```

### "Insufficient Privileges" during login

**Solution:** Verify the role user has access to the target application in Salesforce Setup.

### Locator timeout on Save button

**Solution:** Salesforce Lightning DOM changes frequently. Update `ContractFormPage.java`:

```java
// Add new locator strategy to P1→P4 cascade
private static final String SAVE_BUTTON = "button:has-text('Save')";
```

## Continuous Integration

### GitHub Actions Example

```yaml
name: MACSuite Tests
on: [pull_request, push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '25'
      - name: Run tests
        run: mvn clean test
        env:
          BCT_QA_USERNAME: ${{ secrets.BCT_QA_USERNAME }}
          BCT_QA_PASSWORD: ${{ secrets.BCT_QA_PASSWORD }}
          # ... (27 credentials)
```

## Best Practices

1. **Thread Safety:** All tests run in parallel via TestNG. No shared browser state.
2. **Maintainability:** Page Object Model keeps locators centralized.
3. **Readability:** Gherkin scenarios describe test intent clearly.
4. **Reliability:** P1→P4 locator fallbacks handle framework changes.
5. **Isolation:** Each test sets up/tears down its own browser instance.

## Contributing

- Follow existing code patterns (StepBase, Page Objects)
- Add new scenarios to feature files
- Update `.env` template for new credentials
- Run `mvn clean test` before committing

## Support

For issues or questions, contact the QA Automation team.

---

**Version:** 1.0.0-RELEASE  
**Last Updated:** March 18, 2026  
**Maintained By:** QA Automation Engineering
