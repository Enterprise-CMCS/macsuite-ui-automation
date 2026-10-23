# MACSuite UI Automation — Framework Audit & Upgrade Plan

**Date:** 2026-05-29
**Author:** QA architecture review (Claude Opus 4.8), driven by a both-repos comparison + 9‑dimension audit
**Method:** A multi‑agent workflow read the legacy `MacTrack_Automation` (Selenium + TestNG) suite and the new `MACSuite_UI_Automation` (Playwright + Cucumber + TestNG) suite *side by side*, per module, then adversarially re‑verified every claimed gap against the new code. A parallel track audited the framework across 9 dimensions. All "critical" claims below were re‑verified by hand.

> **Supersedes / reconciles:**
> - `ARCHITECTURE_AUDIT2.md` (2026‑04‑21) — **obsolete**: it predates the `SfPage` base migration, still cites a 908‑line `FormUtils` (now 295) and a removed `BasePage`, and is a raw AI dump. Treat *this* document as the current architecture reference.
> - `AUTOMATION_STATUS_TRACKER.md` (last touched 2026‑05‑14) — **stale & inflated** (see §2).
> - `EXECUTIVE_SUMMARY.md` (2026‑04‑08, "24% / 22‑of‑91") — a stale management snapshot, not architecture.

---

## 1. Executive Summary

The framework is **structurally modern and salvageable** — ThreadLocal browser isolation, a shared `SfPage`/`SfHelper` base, Allure + trace wiring, a clean tag/runner setup. But it currently **provides false confidence**: actual test coverage is materially weaker than the tracker claims, and a pervasive *false‑green* idiom means green runs do not imply working software.

Three issues dominate everything else:

1. **🔴 Security:** a real‑format CMS government Salesforce sandbox credential is committed in two tracked docs and is therefore **already in git history**.
2. **🔴 False‑green engine:** **91** `catch (Exception ignored)` blocks + `clickSave()` treating a failed save as success + ~dozens of `verify…()` methods that log success without asserting → tests stay green when they should be red. There is **exactly one** `assertThat` (web‑first auto‑retrying assertion) in the entire codebase.
3. **🔴 Environment integrity:** `APP_URL_UAT` and `APP_URL_QA` are byte‑identical (`cmsapps5--cmcsmcqa…`), so **every "UAT" Background actually runs against QA**. UAT sign‑off results are not real UAT results.

Plus: **no CI/CD at all**, and **no test‑data cleanup** (every run permanently pollutes a shared org).

**The single most important sequencing rule:** *make existing assertions real (and let the suite go red) **before** adding any new scenarios* — and land that alongside CI + test‑data cleanup so the newly‑honest failures are visible and triageable.

---

## 2. Coverage Truth (reconciliation)

The tracker says "87/87 Gherkin‑mapped, 43/87 implemented (49%), 4 modules fully passing." The verified reality:

- **Dry‑run‑green ≠ runtime‑covered.** A Cucumber dry‑run only proves steps are *glued*. Commented‑out scenarios still have glued step‑defs + page methods (invisible to dry‑run), and `catch (Exception ignored)` + log‑only `verify` methods pass at runtime without checking anything. The 49% figure is inflated.
- **No module is truly fully‑passing with real assertions.** Even the four "complete" modules carry false‑greens: Vendor Engagement's flagship create scenario was end‑to‑end false‑green; CMCS TA is marked 100% yet TC_009/TC_010 are commented out and four response‑preview methods log success without asserting; BCT's `verifyContractNumberAndOrderNumberInHeader` waits for a static label, never the entered values.

**Verified per‑module coverage (before this session's fixes):**

| Module | Legacy tests | Truly mapped | Hollow methods | Note |
|---|---|---|---|---|
| CMCS TA | 32 | 30 | 6 | Strongest; TC_009/TC_010 commented out |
| SDP | 9 | 8 | 4 | Dashboard count‑vs‑report equality is hollow |
| Hiring Metrics | 4 | 4 | 0 | Cleanest module |
| BCT | 4 | 3 | 2 | 1 high‑sev false‑green in header check |
| WFTCL | 5 | 3 | 4 | Dashboard/report drill‑downs missing |
| PIER | 7 | 3 | 6 | Conflict‑error + Cases assertions hollow |
| PRA | 6 | 2 | 1 | MACPro/Standalone lifecycles were commented out |
| **Outreach** | 16 | **3** | **28** | Largest false‑green surface |
| Vendor Engagement | 1 | 0 | 1 | "Complete" per tracker, but verify was `assertTrue(true)` |

Scale: **21 feature files**, ~60 scenario declarations → **74 executable scenarios** (after this session's +3).

**Triage rule:** a *live false‑green* (active scenario, hollow assertion) is **worse** than *honest‑absent* (commented/missing) at equal severity — a green‑but‑empty test actively lies.

---

## 3. Critical Findings (P0)

### 3.1 🔴 Committed Salesforce credential (in git history)
- **Where:** `docs/PLAYWRIGHT_CLI_GUIDE.md:191‑192` (`#username` = `pghosh@index-analytics.com.bct-qa`, `#password` = `Tracker_bct0406`) and `auth-states/README.md:69` (username).
- `.env` itself is correctly gitignored and was **never** committed; `CredentialProvider`/`EnvLoader` are env‑driven with no hardcoded fallback. This is a **doc leak**, but a real, working‑format credential nonetheless.
- **Remediation (all three required):** (1) **rotate** the BCT‑QA password in Salesforce; (2) **purge** from history with `git filter-repo`/BFG and force‑push (team‑coordinated); (3) **redact** the literals to placeholders. Deleting the working‑tree text alone does **not** remove it from history.
- *Per the owner's instruction this session, the files were left unmodified and the leak is documented here only — items (1)–(3) remain to be actioned by the team/CMS.*

### 3.2 🔴 "UAT" actually hits QA
- `.env`: `APP_URL_UAT` and `APP_URL_QA` are identical (`https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com/`). `TestConfig.DEFAULT_APP_URL` disagrees (`cmcsmcuat`), and `EnvironmentResolver` falls back **open** to the default for any unconfigured env.
- **Impact:** every `Given I navigate … in "UAT" environment` runs against QA → UAT sign‑off is invalid.
- **Fix:** point `APP_URL_UAT` at the real UAT host; make env resolution **fail closed** (throw on a missing explicitly‑requested env rather than silently substituting).

### 3.3 🔴 False‑green engine
*(Counts here are a repo‑wide snapshot taken **before** this session's fixes — §7 removed ~3 such catches and added a handful of real assertions; the systemic pattern remains.)*
- **91** `catch (Exception ignored)` blocks; `SfPage.clickSave()` (≈:168‑186) clicks Save and returns even if Save never hides (a validation‑blocked save reads as success); many `verify…()` methods wait for a static label / omnipresent text or wrap their sole assertion in `catch{ log; return }`.
- **Exactly 1** `assertThat` in the whole codebase → essentially no web‑first auto‑retrying assertions; instead 11 `Thread.sleep` + 22 `waitForTimeout` + 16 `NETWORKIDLE` (ineffective on Lightning/Aura, which never goes idle).
- **Fix:** separate benign cleanup swallows from load‑bearing ones; never swallow the result of a primary action or assertion; adopt `PlaywrightAssertions.assertThat(locator)` for all UI checks. **This will turn many green scenarios red — that is the point.**

### 3.4 🔴 No test‑data cleanup
- `CommonHooks.tearDown` deletes nothing; only 1 of 13 record‑creating features cleans up. Every run permanently accumulates inquiries, outreach events, responses, list views, ICR packages in the shared org.
- **Fix:** register created IDs in a scenario‑scoped context and delete them in an `@After` hook, preferably via the Salesforce REST/Bulk API.

### 3.5 🔴 No CI/CD
- `.github/` contains only abandoned Copilot "java‑upgrade/modernize" cruft — no `workflows/`. Tests run only when a human runs `mvn`. The entire reporting investment (Allure, traces, screenshots) is unreachable in CI.
- **Fix:** add `.github/workflows/ci.yml` (Temurin 17 + Maven cache + Playwright browser install) running `@smoke` on PR, with `if: always()` artifact upload + Allure history.

---

## 4. Architecture Audit (by dimension)

Severity in brackets; effort S/M/L. Full evidence retained in the workflow output.

### Waits / Synchronization / Flakiness
- **[CRIT]** Swallowed exceptions hide failed Saves/Edits (see §3.3).
- **[HIGH]** `NETWORKIDLE` ineffective on Lightning; masks fixed sleeps. Replace with `assertThat` on the real post‑condition element.
- **[HIGH]** No web‑first assertions anywhere (`assertThat` used once).
- **[HIGH]** Hidden `slowMo` dependence with **three conflicting defaults** (500/1500/800 ms) and no fast/CI run. Pick one source of truth (`TestConfig.getSlowModeDelay()`), default 0 in CI.

### Configuration & Secrets
- **[CRIT]** Committed credential (§3.1).
- **[HIGH]** The PowerShell auth‑state generator (`auth-states/generate-auth-states.ps1`) is Windows/user‑path‑locked and disconnected from the real in‑code login‑and‑reuse strategy in `LoginSteps`. Keep the in‑code approach; delete/rewrite the PS1.
- **[MED]** Config resolvers fail **open** (§3.2). No `.env.example` for onboarding.
- **[LOW]** `.gitignore:30` `./claude/*/*` is a no‑op pattern → fix to `.claude/`. `EnvLoader` copies all `.env` values into JVM system properties (broadens secret surface).

### CI/CD, Build & Dependencies
- **[CRIT]** No CI (§3.5).
- **[HIGH]** Documented `-Dthreads=N` flag maps to nothing (dead config).
- **[MED]** No flaky retry/quarantine; deprecated `java-dotenv` shipped at compile scope; surefire pinned to a pre‑GA `3.0.0-M9` milestone + stale plugins; reports never persisted/published.
- **[LOW]** Delete the abandoned `.github/java-upgrade` + `.github/modernize` dirs.

### Code Quality & Structure
- **[HIGH]** `InquiryTrackerPage` is a **2,973‑line god class** (~30 responsibilities). Continue the started helper‑extraction.
- **[HIGH]** The shared `SfPage` dashboard/report/list‑view abstraction is **bypassed**: `BCTDashboardPage` overrides it; `OutreachPage`/`InquiryTrackerWorkspaceHelper` reimplement the iframe‑fallback logic → 4 divergent copies. `verifyReports*` exists under 4 names.
- **[HIGH]** Lenient assertions yield false confidence (`verifyTextContains` passes on a 3‑of‑5 keyword heuristic / whole‑page substring).
- **[MED]** 93 empty catches; inconsistent naming; `isVisibleQuiet` hand‑rolled 4×.

### Locator Strategy & UI Resilience
- **[HIGH]** Module pages reintroduce **legacy positional/structural XPath** the common layer avoids (`SdpPage` has 82 xpath locators with column‑index and `[last()]` math). Migrate to Salesforce‑stable hooks (`lightning-*`, `input[name='…__c']`, `data-row-index`).
- **[HIGH]** Framework‑wide `setExact(false)` substring labels + **759 `.first()`** calls opt out of Playwright strict mode. Default to `setExact(true)` + scoped `context()`.
- **[HIGH]** Hardcoded coupling: `"SEI Admin"` user + literal record counts (e.g. `168`, `23`, `35`) drift on any data change. Externalize the user; use tolerant counts (`> 0` / range).
- **[MED]** Iframe handling relies on positional `.last()` to dodge stale dashboard frames — centralize an `activeDashboardFrame()` filtering on visibility/`aria-hidden`.

### Playwright Lifecycle & Parallelism
- **[HIGH]** Parallelism is **advertised but inert**: `@DataProvider(parallel=true)` gated by `dataproviderthreadcount=1`; the pom `<parallel>` system property is a no‑op; `-Dthreads=N` maps to nothing.
- **[HIGH]** `PlaywrightManager.close()` teardown is not exception‑safe — a mid‑teardown failure leaks the browser/driver for the whole reused fork. Wrap each close stage in its own try/finally + clear ThreadLocals in finally.
- **[MED]** Artifact filenames are name+second‑granularity → collide/overwrite under parallelism; `reinitContext()` discards the pre‑login trace and orphans the old video.

### BDD / Gherkin Design
- **[HIGH]** Critical paths are **giant chained E2E scenarios** (`cmcs-ta-e2e-full-flow` ≈141 steps on one shared record) — a mid‑flow failure forfeits all downstream coverage and rarely names the real defect.
- **[HIGH]** No test‑data cleanup (§3.4).
- **[MED]** Tag taxonomy unreliable — the flagship `@critical` tag was commented out, so `-Dtags=@critical` silently skips the suite's most important test. Imperative click‑by‑click steps; action+assertion fused.

### Reporting & Observability
- **[HIGH]** Under the default `SCREENSHOT_MODE=interaction`, the **failure‑moment screenshot is not attached** to Allure. Always attach on `scenario.isFailed()` regardless of mode.
- **[HIGH]** No CI artifact retention; `mvn clean` destroys results; no Allure history.
- **[MED]** Videos recorded every scenario but never attached; per‑scenario logs not captured; missing Allure `categories.json`/`environment.properties`/`executor.json`.

### Test Data Management
- **[CRIT]** No cleanup → guaranteed pollution (§3.4).
- **[HIGH]** UAT→QA mislabeling (§3.2); credential matrix only covers TA/WFTCL in UAT (default & access‑control roles will throw at runtime); `TemplateValueResolver` is **non‑idempotent** (`${timestamp}` re‑resolves to a new value), which is exactly why saved‑value verification must skip `${…}` fields.

---

## 5. Prioritized Upgrade Roadmap

Sequenced so that disruptive‑but‑correct work lands behind a safety net.

| # | Item | Effort | Why / sequencing |
|---|---|---|---|
| **P0‑1** | Rotate + purge + redact the committed credential | M | Security; independent of all else — do first. |
| **P0‑2** | Stand up CI (GitHub Actions + artifact retention + Allure history) | M | The safety net for everything below. Must exist before the suite is allowed to go red. |
| **P0‑3** | Fix the UAT→QA env mislabeling; make resolution fail‑closed | S | UAT results are currently invalid. |
| **P1‑1** | Eliminate swallowed‑exception / log‑only‑success idioms; adopt `assertThat` | M–L | The core of "make green mean green." Land **with** CI + cleanup. Expect many scenarios to turn red — triage, don't revert. |
| **P1‑2** | Scenario‑scoped test‑data cleanup (`@After` + REST/Bulk API) | M | Required before honest re‑runs pollute the org further. |
| **P2‑1** | Replace `Thread.sleep`/`networkidle` with web‑first assertions; one `slowMo` source, default 0 in CI | M–L | Reduces flakiness exposed by P1‑1. Do `assertThat` first, then drop delays. |
| **P2‑2** | Consolidate dashboard/report/list‑view onto `SfPage` (delete the 4 divergent copies) | M | Do after CI catches regressions. |
| **P3‑1** | Wire real parallelism; remove dead concurrency config | M | Only after artifact‑name uniqueness + exception‑safe teardown land. |
| **P3‑2** | Split chained mega‑scenarios into independent TCs | L | Needs reliable preconditioning (depends on cleanup/data). |
| **P3‑3** | Replace positional XPath + blanket `.first()` with stable hooks / strict mode | L | Surface ambiguous selectors deliberately under CI. |

**Quick wins (≤ a day each):**
- Add a committed `.env.example` enumerating every `{ROLE}_{ENV}_USERNAME/PASSWORD` + `APP_URL_{ENV}` key.
- Fix `.gitignore` `.claude/` pattern; delete `.github/java-upgrade` + `modernize`.
- Map `-Dthreads=N` to `<dataproviderthreadcount>` (or correct the docs); delete the dead `<parallel>` no‑op.
- Restore the commented `@critical` tag on the CMCS‑TA flagship scenario.
- CMCS TA: invoke the already‑defined‑but‑unused `the inquiry title year should reflect last year` step; re‑enable the note‑delete steps.
- Update `AUTOMATION_STATUS_TRACKER.md` to reality.

---

## 6. Coverage Gap Backlog (not yet implemented)

This session implemented the highest‑confidence fixes (see §7). The remainder, prioritized:

**Outreach (largest):** populate `createStateOutreachLinkedToEvent` with state/declaration/date/contact fields (root cause of downstream gaps); de‑hollow the remaining ~24 methods — `previewOutreachEmailContent` (assert contact/CC/subject/body), `sendOutreachEmailResponse` (assert "Sent"), `createOutreachEmailResponse` (drop the swallow; assert "Draft email created successfully"), `verifyDayAlertIndicator` (edit end dates per band; assert exact RGB), `verifyMandatoryFieldErrors` (assert all 8 specific messages), `verifyCloseDateRecorded`/`…AfterWithdrawn` (assert value==today + persistence), `addQAEntryToStateOutreach`, `verifyAddStateOutreachFunctionality` (enabled/disabled checkbox rule), `navigateToAllIssuesReport` (iframe top‑vs‑bottom totals).

**CMCS TA:** uncomment + finish TC_009 (field history) and TC_010 (response details) **after** strengthening the presence‑only backing methods; restore content assertions in the 4 ResponseHelper preview/subject/address methods; assert category‑option *exactness* (count == expected, not just present).

**SDP:** de‑hollow `verifyReportCountWidget` (extract each dashboard widget count, open the report, assert equality — the legacy 6× data‑integrity check).

**PIER:** add the 5 missing field‑history assertions in `verifyIssueFieldHistory`; add a record‑title assertion after creation.

**BCT:** de‑hollow `verifyContractNumberAndOrderNumberInHeader` (assert entered values); wire `verifyBctHomePageHeaderDetails`; add `OAGM Division` to update + field‑history.

**Vendor Engagement:** add edit‑after‑create + field‑history scenarios; extend the create form to include Notes (rich‑text) so its saved value can be asserted.

**PRA:** add `OMB NoA = Approved` at the Completed stage (verify the field label on first UAT run); restore dashboard/report drill‑down (the shared `selectDashboardFromList`/`openReport`/`verifyReportContent` already exist).

---

## 7. What Changed This Session

Branch `coverage-gaps-and-framework-audit`, commit "Fill coverage gaps and remove false‑green assertions…". **Verified at the compile + Cucumber dry‑run bar only** — live UAT execution is not reachable from this environment (no VPN/auth‑state to the CMS org), and runtime assertions are **ported from legacy and NOT yet re‑verified against UAT.**

- **Shared:** promoted the proven Dashboards/Reports verification into `SfPage`; defined the lone undefined step (`I should see {string} displayed correctly`) → **0 undefined steps** across all 74 scenarios.
- **PRA:** reactivated the commented MACPro + Standalone `Examples` rows (also de‑hollows `verifyGeneratedIdentifiers`); restored the empty‑save "Complete this field." assertion; restored the announcements‑banner header check.
- **Vendor Engagement:** de‑hollowed `verifyMeetingDetails` (was `assertTrue(true)`; now asserts entered Subject/Vendor Name/Status persist, skipping `${}` templates); added the missing blank‑form required‑field scenario.
- **PIER:** made the Forward/Historical conflict‑error assertions mandatory (removed the catch‑alls); `verifyOutreachActivityTracking` now verifies the send completed + body recorded.
- **Outreach:** `verifyOutreachCannotCloseWithoutConfirmation` asserts the specific message; `verifyCloseDateCannotBeFuture` removed its swallow and asserts the exact message.

---

## 8. How to actually run (verification ceiling)

Local verification used here:
```
mvn -q test-compile
mvn -q test -Dcucumber.execution.dry-run=true -Dtags="not @ignore"   # 0 undefined/ambiguous
```
Real execution needs: VPN to the CMS Salesforce org, a populated `.env` (the role/env credential matrix), and generated auth‑states. Until CI (P0‑2) exists, "passing" means **glued + dry‑run clean**, not runtime‑verified — every new/changed assertion in §7 must be confirmed on a real UAT run.
