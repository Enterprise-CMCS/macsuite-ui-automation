# MACSuite UI Automation — Copilot Instructions

> These rules apply to every automation change in this repository. Follow them in order of priority.

---

## 1. Architecture Overview

```
StepBase / ModuleSteps<T>        ← step definition base classes
    └── *Steps (per module)      ← Cucumber @Given/@When/@Then
          └── SfPage (abstract)  ← base page object (page, helper, formUtils)
                └── *Page        ← module page objects
                      └── *Helper classes (workspace, response, status…)

Shared utilities:
  FormUtils       – field fill orchestration (delegates to ↓)
  DropdownUtils   – combobox / Lightning dropdown selection
  LookupUtils     – lookup field search + selection
  RichTextUtils   – Quill editor fill + verification
  SfHelper        – low-level click/fill/toast helpers
  TestConfig      – runtime flags (headless, highlight, slow mode)
  PlaywrightManager – browser/context/page lifecycle (ThreadLocal)
```

---

## 2. DRY — Reuse Before Creating

**Before adding anything new, check in this order:**

1. `SfPage` base methods (`fillForm`, `fillField`, `clickSave`, `clickEditIfVisible`, `verifyStageIsActive`, `waitForToast`…)
2. `FormUtils` public API (`fillField`, `fillFieldsInSection`, `fillRichTextField`, `verifySuccessMessage`)
3. `SfHelper` (`clickButton`, `selectDropdown`, `selectLookup`, `fillText`, `fillRichText`)
4. Module-specific `*Page` methods
5. Module-specific `*Helper` classes

Only create a new method when none of the above covers the case. One reusable improvement beats multiple narrow methods.

---

## 3. Playwright Locator Policy

### Priority order (always follow P1 → P4)

| Priority | Strategy | Example |
|----------|----------|---------|
| **P1** | Semantic role + accessible name | `page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first()` |
| **P2** | Label / placeholder | `page.getByLabel("Inquiry Title", new Page.GetByLabelOptions().setExact(false)).first()` |
| **P3** | Stable data attribute or ARIA attribute | `page.locator("[data-label='Stage']")`, `page.locator("button[name='SaveEdit']")` |
| **P4** | Contextual anchoring — scope to stable parent, then apply P1/P2 inside | `modal.getByRole(AriaRole.BUTTON, …)`, `section.getByLabel(…)` |

**Never use:**
- Dynamic IDs (e.g., `#aura123456`, `#00D…`)
- XPath as a primary strategy (use `.locator("xpath=…")` only as a last resort fallback within a scoped locator)
- Hard-coded `Thread.sleep` for synchronization (use `locator.waitFor()` instead)

### Pattern examples

```java
// P1 – button
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New").setExact(true)).first().click();

// P2 – text input
page.getByLabel("Inquiry Title", new Page.GetByLabelOptions().setExact(false)).first().fill(value);

// P2 – combobox via label
context.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("Group").setExact(true)).first();

// P3 – stable CSS attribute
page.locator("button.test-id__inline-edit-trigger[title='Edit Inquiry Title']").first();

// P4 – contextual anchoring
Locator modal = page.getByRole(AriaRole.DIALOG).last();
modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Submit").setExact(true)).first().click();

// Regex for partial/case-insensitive match
page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions()
    .setName(Pattern.compile("CMCS TA", Pattern.CASE_INSENSITIVE))).first();
```

### Waiting — always use semantic waits

```java
// Wait for element to appear
locator.waitFor(new Locator.WaitForOptions().setTimeout(10000));

// Wait for element to disappear
locator.waitFor(new Locator.WaitForOptions()
    .setState(WaitForSelectorState.HIDDEN).setTimeout(5000));

// Wait for page function
page.waitForFunction("() => document.querySelector('…') !== null");
```

---

## 4. Page Object Rules

- Every module page extends `SfPage` — never hold a raw `Page` reference separately; use the inherited `page` field.
- Never call `page.waitForTimeout()` — it is deprecated. Use `locator.waitFor()` or `Thread.sleep` only for fixed-duration visual delays (e.g., inside `highlightElement`).
- Prefer `formUtils.fillField(label, value)` for standard field fills. Override `fillField` in module pages only when Salesforce inline-edit requires an extra click before the base fill.
- Module helper classes (`*WorkspaceHelper`, `*ResponseHelper`, etc.) receive the `Page` instance from the parent page object constructor — do not use `PlaywrightManager.page()` inside helpers.
- Keep page methods focused on one user action. Split into helper classes when the page object exceeds ~300 lines of real logic.

---

## 5. Step Definition Rules

- Extend `ModuleSteps<T>` and implement `createModulePage()` — never instantiate page objects directly in step definitions.
- Step text must be business-readable (non-technical). Use consistent verbs: **navigate / open / click / fill / select / verify / confirm**.
- Reuse existing `@Given`, `@When`, `@Then` steps across modules whenever the action is identical.
- Never duplicate a step that already exists in `CommonSteps` or `LoginSteps`.
- Do not add `@ignore`-tagged placeholder steps. If a step is not yet implemented, omit it entirely or raise a tracked issue.

```java
// Correct — delegates to page object
@When("I fill Emergency OutReach details with:")
public void fillEmergencyOutreachDetails(DataTable dataTable) {
    getModulePage().fillEmergencyOutreachDetails(dataTable.asMap(String.class, String.class));
}

// Wrong — business logic in step definition
@When("I fill Emergency OutReach details with:")
public void fillEmergencyOutreachDetails(DataTable dataTable) {
    page().getByLabel("Event Name").fill(dataTable.asMap().get("Event Name")); // ← never do this
}
```

---

## 6. Element Highlighting

`highlightElement(locator, label)` is available in `FormUtils`, `SfHelper`, `DropdownUtils`, and `LookupUtils`. It is gated by `TestConfig.isHighlightEnabled()` (`HIGHLIGHT_ELEMENTS=true` by default).

Call it **before** interacting with any element in a helper method — not after. It restores the original style automatically. Never call it in step definitions or page objects directly; let the utility layer handle it.

```java
// In a helper/utility method
highlightElement(combobox, fieldName);
combobox.click(...);
```

---

## 7. Slow Mode

Playwright native slow motion is configured in `PlaywrightManager` and defaults to `TestConfig.DEFAULT_SLOW_MODE_DELAY` (500 ms). Override at runtime with `-DslowMo=N`. Do **not** add manual `Thread.sleep` calls to simulate slow mode.

---

## 8. Salesforce Lightning Patterns

### Inline edit
Always check for an inline-edit trigger button before filling a detail-page field:
```java
Locator inlineEditBtn = page.getByRole(AriaRole.BUTTON,
    new Page.GetByRoleOptions()
        .setName(Pattern.compile("Edit\\s+" + Pattern.quote(label), Pattern.CASE_INSENSITIVE))).first();
if (inlineEditBtn.count() > 0 && inlineEditBtn.isVisible()) {
    inlineEditBtn.scrollIntoViewIfNeeded();
    inlineEditBtn.click();
}
super.fillField(label, value);
```

### Dependent comboboxes (Category / Sub Category)
Wait for the dependent field to appear before attempting to fill it:
```java
formUtils.fillField("Group", group);
waitForFieldLabelToAppear("Category", 15000);
selectCategory(category);
waitForFieldLabelToAppear("Sub Category", 15000);
```

### Toasts
Use `formUtils.verifySuccessMessage()` or `helper.waitForToast("success", message)` — never poll for toast text manually.

### iframes (dashboards, reports)
Scope locators to the frame before searching:
```java
Locator frame = page.locator("iframe[title='dashboard']").first();
frame.contentFrame().getByText("TA CMCS Inquiries").first()
    .waitFor(new Locator.WaitForOptions().setTimeout(10000));
```

### Stage verification
```java
page.getByRole(AriaRole.TAB,
    new Page.GetByRoleOptions().setName(expectedStage).setSelected(true))
    .first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
```

---

## 9. Gherkin Standards

- Feature files live in `src/test/resources/features/<module>/`.
- Background steps handle navigation and login — do not repeat them in scenarios.
- Exception for legacy chained-flow features: when scenario sequencing is intentionally dependent (for example migrated Selenium flows that rely on prior scenario context), preserve the chain and do not force `Background` conversion unless explicitly requested.
- Use `DataTable` (pipe format) for multi-field forms; use inline parameters for single values.
- Field names in Gherkin must exactly match the UI label (verified against the live DOM or legacy source).
- Tag conventions: `@regression`, `@smoke`, `@critical`, `@ignore` (for known-broken only, never for not-yet-implemented).
- Keep scenarios in standard Given/When/Then flow: start with setup (`Given`), continue with actions (`When`/`And`), and place assertions (`Then`) after the relevant action; do not begin a scenario with `Then` unless the step is intentionally a follow-on assertion after setup already performed in prior steps.

### Dependent-Tag Debug Rule
- If a user asks to run/debug a dependent scenario tag (for example the second scenario in a chained feature), run prerequisite tag(s) in the same command using tag expressions so execution starts from the login/setup scenario and continues in one browser session.

### PIER Chained Scenario Prerequisite Matrix
- For `src/test/resources/features/pier/pier_states_and_cases.feature`, treat scenarios as dependent and execute prerequisites in feature order.
- If target is `@pier_issue_complete`, run tags: `@pier_home_header or @pier_issue_complete`.
- If target is `@pier_cases_lifecycle`, run tags: `@pier_home_header or @pier_issue_complete or @pier_cases_lifecycle`.
- Keep this chain intact for debug and non-debug runs unless the user explicitly requests isolated execution.

---

## 10. Definition of Done

Every automation change must satisfy all of the following before merge:

- [ ] **DRY** — existing methods/steps/utilities were checked; no logic duplicated.
- [ ] **Locator hierarchy** — P1→P4 strategy followed; no dynamic IDs or unscoped CSS-only selectors.
- [ ] **No `waitForTimeout`** — all waits use `locator.waitFor()` or semantic Playwright waits.
- [ ] **Highlight coverage** — `highlightElement` called in any new helper interaction method.
- [ ] **Page object boundary** — business logic lives in page/helper classes, not in step definitions.
- [ ] **Field names verified** — Gherkin labels match the live UI (not inferred).
- [ ] **Transition waits** — modal/form transitions wait on the target UI state, not generic timing.
- [ ] **Compilation clean** — `mvn -DskipTests test-compile` passes with BUILD SUCCESS.
- [ ] **Scope** — changes are limited to the requested behavior; no unrelated refactors.
- [ ] **Soft validation** — count/field-history validation should prefer `SoftValidationCollector` so all issues surface together after the scenario instead of failing on the first mismatch.

---

## 11. Feature-Driven Locator Values

- This framework was migrated from Selenium and contains legacy hardcoded values in some locators.
- When locator behavior depends on business-facing text (for example list view names, menu names, folder names, tabs, visible option values), pass that value from the feature file via step parameters or DataTable values.
- Keep hardcoded only stable technical anchors that should not vary (ARIA roles, structural containers, stable data attributes, fixed control metadata).
- For any locator value intentionally kept hardcoded, document the reason in the PR/summary so reviewers can verify it is truly invariant.















































































































<!-- github-copilot-toolbox:mcp-skills-awareness-begin -->

### MCP & Skills awareness (GitHub Copilot Toolbox)

_Last synced: 2026-09-14T11:36:24.020Z._

- **Full report:** `.github/copilot-toolbox-mcp-skills-awareness.md` in this workspace (auto-overwritten on each scan). Use it as ground truth for configured servers and skill folders.
- **MCP:** For **live tools**, use **Copilot Chat → Agent** and **trust/start** the right servers in the MCP UI.
- **When the user’s task matches a server** (e.g. “open this Confluence page” and a **Confluence** / **Atlassian** MCP is listed), **prefer that server id** and plan on Agent + MCP for actions—not only file search.
- **Skills:** Folders below contain `SKILL.md`; attach or cite paths in chat when relevant.

#### Workspace MCP

- `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.vscode\mcp.json` _(workspace: MACSuite_UI_Automation)_ — _file missing_

_No active workspace servers in mcp.json._

#### User MCP

- `C:\Users\PankajGhosh\AppData\Roaming\Code\User\mcp.json` — _file missing_

_No active user-scoped servers in mcp.json._

#### Project skills

- **automation-debug-test-case** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\automation-debug-test-case` — Use when debugging a failing MACSuite automated test case, unstable locator, or chained scenario run in the Java Playwright BDD framework; requires Playwright CLI snapshots or traces before locator changes and isolates t

- **automation-foundation** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\automation-foundation` — Use when creating, modifying, or debugging MACSuite Java Playwright BDD automation; establishes architecture-first rules, anti-hallucination checks, role boundaries, and Playwright CLI verification requirements for test 

- **automation-modify-test-case** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\automation-modify-test-case` — Use when modifying an existing MACSuite automated test case, feature scenario, step definition, or page object while preserving framework stability, chained flows, and shared abstractions.

- **automation-new-test-case** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\automation-new-test-case` — Use when adding a new MACSuite automated test case, feature scenario, step definition, or page action for the Java Playwright BDD framework; requires Playwright CLI verification for UI discovery and strict reuse of exist

- **playwright-cli** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\playwright-cli` — Automate browser interactions, test web pages and work with Playwright tests.

#### User skills

_None found._

<!-- github-copilot-toolbox:mcp-skills-awareness-end -->
