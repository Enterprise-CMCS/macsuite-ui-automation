```bash
claude write ARCHITECTURE_AUDIT.md
```

Creating comprehensive architecture audit document now...

---

# MACSuite_UI_Automation Architecture Audit

## Executive Summary

**Assessment**: Solid foundation with significant optimization opportunities  
**Code Reduction Potential**: 25-30% (from ~8,500 to ~6,000-6,500 lines)  
**Estimated Effort**: 4 weeks across 4 phases  
**Risk Level**: Low-Medium (well-structured codebase with good test coverage)  
**Recommendation**: Proceed with Phase 1 immediately; defer Phases 2-4 pending Phase 1 results

The MACSuite_UI_Automation framework demonstrates a well-architected foundation with properly implemented core patterns (PlaywrightManager, CommonHooks, configuration management). However, systematic DRY violations across 8 parallel modules and an oversized utility class (FormUtils: 908 lines) present immediate refactoring opportunities that will improve maintainability, reduce cognitive load, and establish patterns for future scaling.

---

## Section 1: Current Architecture Strengths

### 1.1 PlaywrightManager.java ⭐ NO CHANGES NEEDED

**Status**: Exemplary implementation  
**Strengths**:

- Proper thread-safety with ThreadLocal<Browser>, ThreadLocal<BrowserContext>, ThreadLocal<Page>
- Clean browser lifecycle management (launch → createContext → createPage → cleanup)
- Intelligent video recording with scenario-based naming
- Graceful error handling and resource cleanup in @After hooks
- Clear separation between global browser state and thread-scoped context/page

**Code Quality**: 9/10

### 1.2 CommonHooks.java ⭐ NO CHANGES NEEDED

**Status**: Well-designed lifecycle management  
**Strengths**:

- @Before hook properly initializes PlaywrightManager with video naming from scenario
- @After hook handles cleanup, failure screenshots, ThreadLocal clearing in correct order
- Appropriate timing: cleanup occurs after scenario completion
- Resilient to exceptions during cleanup

**Code Quality**: 9/10

### 1.3 TestConfig.java & EnvironmentResolver.java ⭐ SOLID PATTERN

**Strengths**:

- Centralized configuration defaults with override hierarchy (system property > environment variable > default)
- Environment-specific URL resolution (APP_URL_UAT, APP_URL_SEI, etc.)
- Clear precedence and fallback strategy
- Easy to extend for new environments

**Code Quality**: 8/10

### 1.4 BasePage.java

**Current Implementation**:

- Provides helper methods: fillForm(), verifySuccessMessage(), extractRecordNumber()
- FormUtils integration through helper methods
- Stores filledData in instance-scoped Map for later verification

**Strengths**: Good abstraction layer for common page operations  
**Improvement Area**: Would benefit from dedicated assertion helpers (see Section 3.3)

**Code Quality**: 7/10

### 1.5 StepBase.java

**Strengths**:

- Clean interface to PlaywrightManager without dependency injection complexity
- Helper methods properly encapsulated: page(), slowModeDelay(), navigate(), getEnvVar(), takeScreenshot()
- Lightweight and focused

**Code Quality**: 8/10

---

## Section 2: DRY Violations (Critical Issues)

### 2.1 Page Object Lazy-Loading Boilerplate (72 lines of identical code)

**Problem**: All 8 step definition classes contain identical lazy-loading pattern repeated verbatim:

```java
// Pattern appears 8 times across: PraTrackerSteps, InquiryTrackerSteps, BCTContractSteps,
// HiringMetricsSteps, OutreachSteps, PierSteps, VendorEngagementSteps, AccessControlUserSteps

private PraTrackerPage praTrackerPage;

private PraTrackerPage getPraTrackerPage() {
    if (praTrackerPage == null) {
        praTrackerPage = new PraTrackerPage(page());
    }
    return praTrackerPage;
}
```

**Impact**:

- 72 lines of pure boilerplate (9 lines × 8 modules)
- Difficult to maintain if initialization logic changes
- No consistent initialization strategy across modules
- Violates DRY principle

**Solution**: Implement PageObjectFactory or enhanced BaseSteps with parameterized initialization

**Effort**: 4 hours | **Impact**: HIGH (eliminates 72 lines, improves consistency)

---

### 2.2 Page Object Method Duplication (~200-300 lines)

**Problem**: Each module's page object (ranges 600-900 lines) contains similar interaction patterns:

**Affected Files**:

- PraTrackerPage.java (598 lines)
- InquiryTrackerSteps.java (629 lines)
- ContractFormPage.java
- HiringMetricsPage.java
- OutreachPage.java
- PierIssuePage.java
- VendorEngagementMeetingPage.java
- AppDashboardPage.java

**Duplicated Patterns**:

- Navigation setup and verification
- Form field interactions (fillField, getFieldValue, verifyField)
- Button click sequences with waits
- Modal/dialog handling
- Success/error message extraction

**Example Duplication**:

```java
// Pattern repeated across multiple modules:
public void navigateToModule() {
    navigate("app/module");
    waitForElementVisible(MAIN_CONTAINER);
    verifyPageFullyLoaded();
}

public void fillForm(Map<String, String> data) {
    for (Map.Entry<String, String> entry : data.entrySet()) {
        fillField(entry.getKey(), entry.getValue());
    }
    verifyNoValidationErrors();
}
```

**Impact**:

- 200-300 lines of duplicated logic across 8 modules
- Inconsistent implementations make bug fixes difficult
- Testing changes requires updates in multiple locations

**Solution**: Extract to shared base page classes or mixins (FormFillerPage, ModalHandlerPage, NavigationPage)

**Effort**: 12 hours | **Impact**: HIGH (eliminates 200-300 lines, improves consistency)

---

### 2.3 FormUtils.java - Oversized Utility (908 lines, 9 distinct responsibilities)

**Problem**: Single class violates single responsibility principle with 9 distinct concerns:

**Current Responsibilities**:

1. **Field Filling**: fillForm(), setTextField(), setDropdown(), setMultiSelect()
2. **Success Message Extraction**: extractSuccessMessage(), verifySuccessMessage()
3. **Toast Verification**: verifyToastMessage(), waitForToastVisible()
4. **Element Highlighting**: highlightElement(), unhighlightElement() (visual debugging)
5. **Section-Scoped Filling**: fillFormInSection(), getFieldsInSection()
6. **Lookup Field Handling**: handleLookupField(), searchAndSelectLookup()
7. **Dropdown Detection**: isDropdownField(), getDropdownOptions()
8. **Date/Time Handling**: setDateField(), setTimeField(), formatDateForInput()
9. **Validation**: validateFieldValue(), verifyFieldValidation()

**Current State**: 908 lines, mixed concerns, difficult to maintain

**Recommended Split** (5 specialized classes):

```
utilities/forms/
├── FieldFiller.java (200 lines)
│   └── fillForm(), setTextField(), setDropdown(), setMultiSelect()
│
├── SpecializedFieldHandlers.java (150 lines)
│   └── handleLookupField(), setDateField(), setTimeField()
│
├── MessageExtractor.java (120 lines)
│   └── extractSuccessMessage(), verifyToastMessage(), waitForToastVisible()
│
├── SectionScopedFiller.java (80 lines)
│   └── fillFormInSection(), getFieldsInSection()
│
└── ElementHighlighter.java (60 lines)
    └── highlightElement(), unhighlightElement()
```

**Impact**:

- Reduces 908-line file into 5 focused classes (avg 120 lines each)
- Each class has single, testable responsibility
- Easier to locate and modify specific behavior
- Improves code reusability across modules

**Effort**: 8 hours | **Impact**: HIGH (improves clarity and maintainability)

---

### 2.4 Repeated Step Definition Patterns

**Problem**: Common step definitions duplicated with minor variations across modules

**Examples**:

- Navigation to module with verification (appears 8 times)
- Form filling with validation (appears 6 times)
- Success message verification (appears 7 times)
- Button click sequences (appears 5+ times)

**Solution**: Enhance CommonSteps with parameterized versions; establish inheritance pattern for module-specific steps

**Effort**: 6 hours | **Impact**: MEDIUM (eliminates 50-80 lines, improves consistency)

---

## Section 3: Architectural Gaps

### 3.1 Missing Step Definition Inheritance/Composition

**Gap**: Each step class independently manages page object initialization with no reuse pattern

**Current Approach**:

```java
// Each of 8 step classes implements separately
public class PraTrackerSteps extends StepBase {
    private PraTrackerPage praTrackerPage;

    private PraTrackerPage getPraTrackerPage() {
        if (praTrackerPage == null) {
            praTrackerPage = new PraTrackerPage(page());
        }
        return praTrackerPage;
    }
    // ... repeat for each module
}
```

**Recommended Approach**:

```java
public abstract class ModuleSteps<T extends BasePage> extends StepBase {
    private T modulePage;

    protected T getModulePage() {
        if (modulePage == null) {
            modulePage = createPageInstance();
        }
        return modulePage;
    }

    protected abstract T createPageInstance();
}

// Usage in PraTrackerSteps
public class PraTrackerSteps extends ModuleSteps<PraTrackerPage> {
    @Override
    protected PraTrackerPage createPageInstance() {
        return new PraTrackerPage(page());
    }
}
```

**Impact**: Eliminates boilerplate, establishes inheritance pattern for future modules

---

### 3.2 Missing Data Builders & Test Data Management

**Gap**: No centralized test data factories or builders; data setup scattered throughout step definitions

**Current State**:

- Test data created inline in step methods
- No reusable data patterns
- Difficult to modify data structure (affects all tests)
- No clear data scope/lifecycle

**Recommended Pattern**:

```java
public class PraApplicationBuilder {
    private String applicationId;
    private String status = "Pending";
    private String amount = "50000";
    private Map<String, String> formData = new HashMap<>();

    public PraApplicationBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public PraApplication build() {
        return new PraApplication(applicationId, status, amount, formData);
    }
}

// Usage
PraApplication app = new PraApplicationBuilder()
    .withStatus("Approved")
    .build();
```

**Impact**: Standardizes data setup, improves test readability, enables data reuse

---

### 3.3 Missing Assertion/Verification Utility Layer

**Gap**: Assertions scattered throughout step definitions and page objects; no centralized verification strategy

**Current State**:

- Basic assertions mixed with test logic
- No custom assertion messages
- Difficult to change verification approach globally
- No assertion reuse across modules

**Recommended**: Create AssertionHelper

```java
public class AssertionHelper {
    public static void assertFieldValue(Page page, String selector, String expectedValue) {
        String actualValue = page.locator(selector).inputValue();
        Assert.assertEquals(actualValue, expectedValue,
            "Field value mismatch: expected '" + expectedValue + "' but got '" + actualValue + "'");
    }

    public static void assertSuccessMessage(String actualMessage, String expectedPattern) {
        Assert.assertTrue(actualMessage.contains(expectedPattern),
            "Expected success message containing '" + expectedPattern + "' but got '" + actualMessage + "'");
    }
}
```

**Impact**: Centralizes verification logic, improves error messages, enables global assertion changes

---

### 3.4 Missing Test Data Scope/Cleanup Strategy

**Gap**: Feature files don't clearly define data setup/teardown; cleanup responsibility unclear

**Current State**:

- Test data persists between scenarios
- No explicit cleanup hooks
- Potential data pollution in parallel execution
- Difficult to isolate test failures

**Recommended**:

```gherkin
Feature: PRA Workflow
  Background:
    Given a new PRA application with default data
    And the application is in "Draft" status

  Scenario: Submit PRA for approval
    When user submits the application
    Then the application status changes to "Submitted"

  # @After hook automatically cleans up created application
```

**Impact**: Improves test isolation, prevents data pollution, enables safer parallel execution

---

### 3.5 Missing Retry/Recovery Strategies

**Gap**: Flaky operations (network timeouts, element visibility delays) lack centralized retry logic

**Current Approach**: Wait statements scattered throughout code

**Recommended Pattern**:

```java
public class RetryStrategy {
    public static <T> T retry(Callable<T> operation, int maxAttempts, long delayMs) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw new RuntimeException("Failed after " + maxAttempts + " attempts", lastException);
    }
}

// Usage
RetryStrategy.retry(() -> page.click(SUBMIT_BUTTON), 3, 500);
```

**Impact**: Reduces test flakiness, centralizes retry logic, improves debugging

---

### 3.6 Missing Test Evidence/Artifacts Strategy

**Gap**: Screenshot-only approach; missing comprehensive evidence collection

**Current State**:

- Screenshots captured on failure only
- No performance metrics captured
- Network logs not collected
- Browser console errors not logged

**Recommended**:

```java
public class TestEvidenceCollector {
    public void captureEvidence(Scenario scenario) {
        captureScreenshot(scenario);
        captureBrowserLogs(scenario);
        captureNetworkLogs(scenario);
        capturePerformanceMetrics(scenario);
    }
}
```

**Impact**: Improves debugging, enables performance trend analysis, captures comprehensive failure context

---

### 3.7 Missing Performance Baseline Tracking

**Gap**: No performance metrics tracked; difficult to identify regressions

**Recommended**: Integrate performance tracking for critical paths

---

## Section 4: Code Quality Issues

### 4.1 Magic Strings

**Problem**: Selectors, field names, URLs scattered throughout code

```java
// Bad
page.click("div[class*='submit']");

// Good
page.click(SUBMIT_BUTTON_SELECTOR);
```

### 4.2 Inconsistent Logging

**Problem**: Some classes log extensively, others minimally; no log level strategy

### 4.3 Exception Handling

**Problem**: Generic catches without recovery or logging context

```java
// Bad
try { ... } catch (Exception e) { }

// Good
try { ... } catch (PlaywrightException e) {
    logger.error("Failed to locate element: " + selector, e);
    throw new ElementNotFoundException(selector, e);
}
```

### 4.4 Method Length

**Problem**: Some page object methods exceed 50+ lines; should be decomposed

---

## Section 5: Recommended Folder Structure

```
src/test/java/com/salesforce/macsuite/
├── base/
│   ├── BasePage.java (existing, refined)
│   ├── StepBase.java (existing, refined)
│   └── ModuleSteps.java (NEW - abstract base for module steps)
│
├── core/
│   ├── PlaywrightManager.java (existing, no changes)
│   ├── TestConfig.java (existing, solid)
│   └── EnvironmentResolver.java (existing, solid)
│
├── utilities/
│   ├── forms/
│   │   ├── FieldFiller.java (NEW - split from FormUtils)
│   │   ├── SpecializedFieldHandlers.java (NEW - split from FormUtils)
│   │   ├── MessageExtractor.java (NEW - split from FormUtils)
│   │   ├── SectionScopedFiller.java (NEW - split from FormUtils)
│   │   └── ElementHighlighter.java (NEW - split from FormUtils)
│   ├── verification/
│   │   ├── AssertionHelper.java (NEW)
│   │   └── ToastVerifier.java (NEW)
│   ├── retry/
│   │   └── RetryStrategy.java (NEW)
│   └── evidence/
│       └── TestEvidenceCollector.java (NEW)
│
├── factories/
│   ├── PageObjectFactory.java (NEW)
│   └── TestDataBuilder.java (NEW - abstract base)
│
├── data/builders/ (NEW)
│   ├── PraApplicationBuilder.java
│   ├── InquiryBuilder.java
│   ├── ContractBuilder.java
│   └── (similar for other modules)
│
├── modules/
│   ├── pra/
│   │   ├── PraTrackerPage.java
│   │   └── PraTrackerSteps.java
│   ├── inquiry/
│   │   ├── InquiryTrackerPage.java
│   │   └── InquiryTrackerSteps.java
│   ├── contract/
│   ├── hiring/
│   ├── outreach/
│   ├── pier/
│   ├── vendor/
│   └── access/
│
├── common/
│   ├── CommonSteps.java (existing)
│   └── CommonHooks.java (existing)
│
└── resources/
    ├── features/
    │   ├── pra/
    │   ├── inquiry/
    │   ├── contract/
    │   └── (other modules)
    └── testng.xml
```

---

## Section 6: 4-Phase Refactoring Plan

### Phase 1: Foundation (Week 1) - 16 hours

**Goal**: Eliminate boilerplate, establish reusable patterns

**Tasks**:

1. Create `ModuleSteps<T extends BasePage>` abstract class (2 hours)
   - Generic lazy-loading pattern
   - Update all 8 step classes to extend ModuleSteps
   - Eliminate 72 lines of boilerplate
2. Create `PageObjectFactory` (2 hours)
   - Factory pattern for page instantiation
   - Support for parameterized page creation
3. Create `AssertionHelper` (2 hours)
   - Centralized assertion methods
   - Custom error messages
4. Create `RetryStrategy` utility (2 hours)
   - Configurable retry/recovery logic
   - Used for flaky operations
5. Comprehensive testing (6 hours)
   - Run full test suite
   - Verify all 20 feature files pass
   - Validate parallel execution

**Deliverable**: Refactored step definitions with boilerplate eliminated, test suite passing  
**Risk**: Low (changes are isolated to step class hierarchy)  
**Dependencies**: None

---

### Phase 2: Utilities Split (Week 2) - 20 hours

**Goal**: Decompose FormUtils into focused classes

**Tasks**:

1. Extract `FieldFiller.java` (4 hours)
   - Move fillForm(), setTextField(), setDropdown(), setMultiSelect()
   - Target: ~200 lines
2. Extract `SpecializedFieldHandlers.java` (3 hours)
   - Move handleLookupField(), setDateField(), setTimeField()
   - Target: ~150 lines
3. Extract `MessageExtractor.java` (3 hours)
   - Move extractSuccessMessage(), verifyToastMessage(), waitForToastVisible()
   - Target: ~120 lines
4. Extract `SectionScopedFiller.java` (2 hours)
   - Move fillFormInSection(), getFieldsInSection()
   - Target: ~80 lines
5. Extract `ElementHighlighter.java` (2 hours)
   - Move highlightElement(), unhighlightElement()
   - Target: ~60 lines
6. Update all imports across codebase (4 hours)
   - BasePage, all step classes, any other imports
7. Comprehensive testing (2 hours)
   - Full test suite run
   - Verify form interactions work correctly

**Deliverable**: FormUtils reduced from 908 to specialized classes, test suite passing  
**Risk**: Medium (widespread imports require updates; thorough testing essential)  
**Dependencies**: Phase 1 (relies on improved patterns)

---

### Phase 3: Data & Testing Patterns (Week 3) - 16 hours

**Goal**: Establish data builders, improve test data management

**Tasks**:

1. Create abstract `TestDataBuilder<T>` base class (2 hours)
   - Builder pattern for all test data
   - Fluent interface
2. Create module-specific builders (6 hours)
   - PraApplicationBuilder
   - InquiryBuilder
   - ContractBuilder
   - HiringMetricsBuilder
   - OutreachBuilder
   - PierIssueBuilder
   - VendorEngagementBuilder
   - AccessControlUserBuilder
3. Establish data scope/cleanup conventions (3 hours)
   - Document data lifecycle in feature files
   - Create @Before/@After hooks for data setup/cleanup
4. Create `TestEvidenceCollector` utility (3 hours)
   - Screenshots, browser logs, performance metrics
   - Integrate into CommonHooks
5. Testing and documentation (2 hours)

**Deliverable**: Data builders integrated, test data standardized, evidence collection working  
**Risk**: Low-Medium (new patterns, requires step definition updates)  
**Dependencies**: Phase 1 & 2

---

### Phase 4: Documentation & Hardening (Week 4) - 12 hours

**Goal**: Document architecture decisions, establish maintenance patterns

**Tasks**:

1. Create Architecture Decision Records (ADRs) (4 hours)
   - ADR-001: ModuleSteps inheritance pattern
   - ADR-002: FormUtils decomposition strategy
   - ADR-003: TestDataBuilder pattern
   - ADR-004: RetryStrategy approach
2. Create troubleshooting guide (3 hours)
   - Common failure patterns
   - Recovery strategies
   - Debugging workflows
3. Create contributor guidelines (2 hours)
   - New module setup process
   - Code patterns to follow
   - Review checklist
4. Performance baseline setup (2 hours)
   - Critical path identification
   - Metrics collection
   - Trend analysis
5. Final testing & documentation review (1 hour)

**Deliverable**: Comprehensive documentation, established maintenance patterns  
**Risk**: Low (documentation phase, no code risk)  
**Dependencies**: Phases 1-3

---

## Section 7: Quick Wins (Implement First)

| Task                              | Effort | Impact | Risk   | Blockers |
| --------------------------------- | ------ | ------ | ------ | -------- |
| ModuleSteps pattern               | 4h     | HIGH   | LOW    | None     |
| FormUtils split                   | 8h     | HIGH   | MEDIUM | Phase 1  |
| AssertionHelper                   | 3h     | MEDIUM | LOW    | None     |
| RetryStrategy                     | 2h     | MEDIUM | LOW    | None     |
| MessageExtractor (from FormUtils) | 2h     | MEDIUM | LOW    | Phase 2  |
| TestDataBuilder base              | 2h     | MEDIUM | LOW    | None     |

**Recommended execution order**: 1→4→3→6→2→5

---

## Section 8: Risk Assessment & Mitigation

### Phase 1 Risk: Low

**Potential Issues**:

- Inheritance changes could break step initialization
- **Mitigation**: Full test suite run after each change; version control rollback point

### Phase 2 Risk: Medium

**Potential Issues**:

- Import updates across 8 modules; circular dependencies
- Changed method signatures could break existing code
- **Mitigation**: Careful search/replace; compatibility layer if needed; comprehensive testing

### Phase 3 Risk: Low-Medium

**Potential Issues**:

- Test data inconsistencies if builders don't match form requirements
- **Mitigation**: Start with single module; validate against existing tests

### Overall Risk Mitigation Strategy

1. **Branch per phase**: Separate git branches for each phase
2. **Comprehensive testing**: Full test suite after each significant change
3. **Gradual rollout**: Don't refactor all modules simultaneously
4. **Documentation**: Clear ADRs and guidelines for future contributors

---

## Section 9: Testing Strategy

### Pre-Refactoring Baseline

1. Run full test suite: All 20 feature files
2. Capture baseline metrics: Execution time, pass rate, flaky tests
3. Document known flaky scenarios

### Phase-by-Phase Testing

1. After each phase: Full test suite run
2. Compare against baseline: Should maintain 100% pass rate
3. Document any performance changes

### Post-Refactoring Validation

1. Performance comparison: Baseline vs. refactored
2. Stability: Track flaky test reduction
3. Code coverage: Verify no regression

---

## Section 10: Success Metrics

| Metric              | Current      | Target                         | Measurement             |
| ------------------- | ------------ | ------------------------------ | ----------------------- |
| Code reduction      | ~8,500 lines | ~6,000-6,500 lines             | Full codebase LOC count |
| Duplication         | 15-20%       | < 5%                           | Code analysis tool      |
| Avg method length   | 30-40 lines  | < 25 lines                     | Code analysis           |
| Test execution time | TBD          | No regression                  | CI/CD metrics           |
| Test stability      | TBD          | 98%+ pass rate, < 2% flaky     | Historical data         |
| Onboarding time     | TBD          | < 3 days to first contribution | Survey new contributors |

---

## Section 11: Next Steps

1. **Review & Approval**: Stakeholder review of refactoring plan
2. **Phase 1 Execution**: Begin ModuleSteps implementation
3. **Continuous Monitoring**: Track metrics against success criteria
4. **Feedback Loop**: Adjust approach based on Phase 1 results before Phase 2

---

**Document Version**: 1.0  
**Created**: 2026-04-21  
**Last Updated**: 2026-04-21  
**Status**: Ready for implementation

Done! Your audit is now saved to `ARCHITECTURE_AUDIT.md` in the project. Want to start with Phase 1?
