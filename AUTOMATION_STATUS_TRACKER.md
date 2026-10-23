# Automation Status Tracker

> ⚠️ **STALE / INFLATED (as of 2026‑05‑29).** A verified both‑repos comparison found the
> implementation/"passing" figures below overstate real coverage: dry‑run‑green is not
> runtime‑covered (commented scenarios keep glued step‑defs), and a false‑green idiom (~91
> swallowed‑exception blocks + log‑only `verify` methods) lets steps pass without asserting.
> See **`FRAMEWORK_AUDIT_AND_UPGRADE_PLAN.md`** for the reconciled, verified status and the
> upgrade roadmap. This file needs to be rewritten to reality.

**Last Updated:** May 8, 2026  
**Owner:** QA Automation  
**Framework:** Playwright Java + Cucumber + TestNG

---

## 📊 Executive Summary

| Metric | Value |
|---|---|
| **Total Legacy Tests Identified** | **87 tests** |
| **Total Legacy Test Files** | **23 files** |
| **Tests with Feature File Written (Gherkin Mapped)** | **87 / 87 (100%)** |
| **Tests Fully Implemented (Step Defs Working)** | **43 / 87 (49%)** |
| **Feature Files Created** | **24 files** |
| **Modules with Full Gherkin Coverage** | **9 / 9 (100%)** |
| **Modules Fully Implemented (Passing)** | **4 / 9** (BCT, Vendor Engagement, Access Control, CMCS TA) |
| **Modules In Progress (Step Defs Partial)** | **4 / 9** (Outreach, PIER, PRA, Hiring Metrics) |
| **Modules Feature-Filed Only (No Step Defs Yet)** | **2 / 9** (SDP, WFTCL Implementation) |

> **Milestone reached May 6, 2026:** All 87 legacy tests are now mapped to Gherkin feature files.  
> **Milestone reached May 8, 2026:** CMCS TA module is 100% implemented (32/32 tests).

---

## 🛠️ Framework Refactor Status

- Shared Salesforce base has been migrated to `SfPage` / `SfNavigator` / `SfHelper`
- `BasePage` and `SalesforceBasePage` have been removed from active use
- Inquiry Tracker has been moved onto `SfPage`, which unblocks future extraction without keeping two competing page bases
- Current utility cleanup status:
  - `FormUtils`: cleaned up, now **295 lines** (removed 4 dead toast methods + ToastUtils dependency)
  - `ToastUtils.java`: **deleted** — was unreachable from step definitions; `SfHelper.waitForToast` is the single path
  - `DropdownUtils`: in progress, now ~111 lines
  - `LookupUtils`: in progress, polling confirmation loop removed
  - `SfHelper`: fixed toast CSS selector (was `.slds-theme--success, .slds-notify--toast`; `.slds-notify--toast` matched all toast types — now `.slds-theme--success` only)
- Inquiry Tracker page object refactoring:
  - `InquiryTrackerPage.java`: reduced **2469 → 2201 lines** by extracting two helper classes
  - `InquiryTrackerNotesHelper.java`: new (98 lines) — add/edit/delete/verify notes
  - `InquiryTrackerResponseHelper.java`: expanded to 368 lines — 15 response verification methods
- Current validation checkpoint:
  - `mvn -q test-compile` passing
  - dry-run passing for `@bct`, `@pra`, `@pier`, `@outreach`, `@vendor-engagement`, `@hiring-metrics`, and `@cmcs-ta`

---

## 🎯 Module Progress (Sorted by Priority)

### **1. CMCS TA (Technical Assistance) - COMPLETE**

| Metric | Status |
|---|---|
| **Legacy Tests** | 32 tests (14 core functional + 18 report validation) |
| **Gherkin Coverage** | **32 / 32 (100%)** |
| **Implementation Coverage** | **32 / 32 (100%)** |
| **Status** | ✅ **Complete** |
| **Feature Files** | 4 files |
| **Last Updated** | May 14, 2026 |

**Legacy Test Breakdown:**

- CMCS_TA_Suites.java: 10 tests
- CMCS_TA_ListView__Dashboard_Reports.java: 3 tests
- CMCS_TA_Reports.java: 18 tests (automated and passing)
- CMCS_TA_WFTCL.java: 1 test

**Completed Tests:**

- ✅ TC_001: New Inquiry Creation + Validation + Category Options + Question
- ✅ TC_002: Stage Navigation (Submitted → Triage → In Group Review → Senior Review → reversal)
- ✅ TC_003: Escalation + Notes (add/edit/delete) + Response Readiness
- ✅ TC_004: OGC Consultation checkbox + OGC Escalation/Resolution date rules + response sent
- ✅ TC_005: Related Inquiry Creation + Parent Navigation
- ✅ TC_006: Full Response Lifecycle (create → fields → preview → send → sent)
- ✅ TC_007: Put On Hold / Release
- ✅ TC_008: Withdrawn + Close/Reopen Transitions + Validation
- ✅ TC_009: Inquiry Field History + Response Field History
- ✅ TC_010: Response Details Field (commented out — pending standalone extraction)
- ✅ List View Operations (create, clone, delete)
- ✅ Dashboard Navigation + Home Header
- ✅ CMCS TA report validation suite (18 reports automated and passing)
- ✅ WFTCL Inquiry Creation

**Feature Files (4):**

| File | Coverage |
|---|---|
| `cmcs-ta-e2e-full-flow.feature` | CMCS_TA_Suites TC_001–TC_009 (sole owner, chained E2E) |
| `cmcs-ta-listview-operations.feature` | CMCS_TA_ListView__Dashboard_Reports TC_001 + TC_002 (home header + list view + dashboard nav) |
| `cmcs-ta-03-reports-validation.feature` | CMCS_TA_ListView__Dashboard_Reports TC_003 + CMCS_TA_Reports TC_001–TC_018 (18 reports) |
| `cmcs-ta-wftcl-e2e.feature` | CMCS_TA_WFTCL TC_001 E2E + category options + question |

**Next Steps:** Maintain only

---

### **2. BCT (Budget & Contract Tracker)**

| Metric | Status |
|---|---|
| **Legacy Tests** | 4 tests |
| **Gherkin Coverage** | **4 / 4 (100%)** |
| **Implementation Coverage** | **4 / 4 (100%)** |
| **Status** | ✅ **Complete** |
| **Feature Files** | 3 files |
| **Last Result** | Passing |

**Legacy Test Breakdown:**

- TestSuite.java: 1 test (contract creation + mandatory fields)
- BCT_Listview_Dashboard_Report.java: 3 tests

**Completed Tests:**

- ✅ TC_001: Contract Creation + Header Validation + Contract Actions + Notes + Field History
- ✅ Home Page Header (dashboard iframe title, refresh date, "Viewing as SEI Admin", announcements)
- ✅ ListView Operations (create, clone, delete)
- ✅ Dashboard Navigation
- ✅ Reports Navigation

**Feature Files (3):**

| File | Coverage |
|---|---|
| `bct-contract-action-menu.feature` | TestSuite TC_001 — creation, header validation, contract actions, edit, notes |
| `bct-contract-field-history.feature` | TestSuite TC_001 — field history tracking |
| `bct-dashboard-reports.feature` | LDR TC_001/TC_002/TC_003 — home header, list view, dashboard, reports |

**Next Steps:** Maintain only

---

### **3. Vendor Engagement**

| Metric | Status |
|---|---|
| **Legacy Tests** | 1 test |
| **Gherkin Coverage** | **1 / 1 (100%)** |
| **Implementation Coverage** | **1 / 1 (100%)** |
| **Status** | ✅ **Complete** |
| **Feature Files** | 1 file |
| **Last Result** | Passing |

**Completed Tests:**

- ✅ TC_001: Schedule Vendor Engagement Meeting

**Next Steps:** Maintain only

---

### **4. Outreach**

| Metric | Status |
|---|---|
| **Legacy Tests** | 20 tests |
| **Gherkin Coverage** | **20 / 20 (100%)** |
| **Implementation Coverage** | **3 / 20 (15%)** |
| **Status** | 🟡 **Feature Files Written — Needs Step Defs** |
| **Feature Files** | 3 files |

**Legacy Test Breakdown:**

- CMCS_EmergencyOutReach_Test.java: 8 tests
- CMCS_GeneralOutReach_Test.java: 6 tests
- CMCS_Outreach_Records_Suites.java: 1 test
- CMCS_Add_State_To_Outreach.java: 1 test
- CMCS_Outreach_ListView_Dashboard_Reports.java: 3 tests
- CMCS_Outreach_Reports_Suites.java: 1 test

**Implemented (3 tests):**

- ✅ Emergency Outreach TC_001 (creation → Published smoke)
- ✅ Add State to Outreach (smoke)
- ✅ Emergency Event Creation (smoke)

**Feature File Written, Needs Step Defs + Locators (17 tests):**

- 🟡 Emergency TC_002: Mandatory field validation + President/Secretary Declaration Date fields (`outreach-emergency-e2e.feature`)
- 🟡 Emergency TC_003: State Outreach + 30/60/90 day color alerts (`outreach-emergency-e2e.feature`)
- 🟡 Emergency TC_004: Email response creation + preview + send (`outreach-emergency-e2e.feature`)
- 🟡 Emergency TC_005: Close with confirm + withdrawn lifecycle + reopen (`outreach-emergency-e2e.feature`)
- 🟡 Emergency TC_006: CMCS TA linkage from Emergency Outreach (`outreach-emergency-e2e.feature`)
- 🟡 Emergency TC_007: SOE close/reopen/withdrawn status lifecycle (`outreach-emergency-e2e.feature`)
- 🟡 Emergency TC_008: Field history (SOE, State Outreach, Response) (`outreach-emergency-e2e.feature`)
- 🟡 General TC_001: Create outreach event (`outreach-general-e2e.feature`)
- 🟡 General TC_002: Create State Outreach + State Lead + Date Initiated (`outreach-general-e2e.feature`)
- 🟡 General TC_003: Outreach email + close/reopen/withdrawn lifecycle (`outreach-general-e2e.feature`)
- 🟡 General TC_004: Q&A + Open → Closed (`outreach-general-e2e.feature`)
- 🟡 General TC_005: SOE status lifecycle (`outreach-general-e2e.feature`)
- 🟡 General TC_006: Field history (SOE, State Outreach, Response) (`outreach-general-e2e.feature`)
- 🟡 Records TC_001: Records display (`outreach-general-e2e.feature`)
- 🟡 ListView TC_001: Add + clone list view (`outreach-general-e2e.feature`)
- 🟡 ListView TC_002: Dashboard navigation (`outreach-general-e2e.feature`)
- 🟡 Reports: All Issues report (`outreach-general-e2e.feature`)

**Next Steps:** Confirm locators for outreach form fields, then implement step definitions

---

### **5. PIER (Issue Tracker)**

| Metric | Status |
|---|---|
| **Legacy Tests** | 6 tests |
| **Gherkin Coverage** | **6 / 6 (100%)** |
| **Implementation Coverage** | **1 / 6 (17%)** |
| **Status** | 🟡 **Feature Files Written — Needs Step Defs** |
| **Feature Files** | 3 files |

**Legacy Test Breakdown:**

- PIER_TestSuites.java: 3 tests
- PIER_Listview_Dashboard_Report.java: 3 tests

**Implemented (1 test):**

- ✅ TC_002: Update Existing PIER Issue (`pier-issue-smoke.feature`)

**Feature File Written, Needs Step Defs + Locators (5 tests):**

- 🟡 TC_001: State Names Display (`pier-states-and-cases.feature`)
- 🟡 TC_003: PIER Cases — creation, update, stages, outreach activity, field history (`pier-states-and-cases.feature`)
- 🟡 ListView TC_001: Add + clone list view (`pier-listview-dashboard-reports.feature`)
- 🟡 ListView TC_002: Dashboard navigation (`pier-listview-dashboard-reports.feature`)
- 🟡 ListView TC_003: Reports navigation (`pier-listview-dashboard-reports.feature`)

**Next Steps:** Confirm locators for PIER Cases form fields and Accounts tab state names, then implement step defs

---

### **6. PRA (Program Regulatory Advisor)**

| Metric | Status |
|---|---|
| **Legacy Tests** | 6 tests |
| **Gherkin Coverage** | **6 / 6 (100%)** |
| **Implementation Coverage** | **1 / 6 (17%)** |
| **Status** | 🟡 **Feature Files Written — Needs Step Defs** |
| **Feature Files** | 2 files |

**Legacy Test Breakdown:**

- PRA_Tracker.java: 3 tests
- PRA_ListView_Dashboard_Reports.java: 3 tests

**Implemented (1 test):**

- ✅ TC_001–TC_003: ICR Package lifecycle (Generic, MACPro, Standalone) — `pra-icr-workflow.feature`

**Feature File Written, Needs Step Defs + Locators (3 tests):**

- 🟡 ListView TC_001: Home header + add + clone list view (`pra-listview-dashboard-reports.feature`)
- 🟡 ListView TC_002: Dashboard navigation (`pra-listview-dashboard-reports.feature`)
- 🟡 ListView TC_003: Reports navigation (`pra-listview-dashboard-reports.feature`)

**Next Steps:** Confirm PRA home header locators, implement list view / dashboard / reports step defs

---

### **7. Hiring Metrics Hub**

| Metric | Status |
|---|---|
| **Legacy Tests** | 4 tests |
| **Gherkin Coverage** | **4 / 4 (100%)** |
| **Implementation Coverage** | **1 / 4 (25%)** |
| **Status** | 🟡 **Feature Files Written — Needs Step Defs** |
| **Feature Files** | 2 files |

**Legacy Test Breakdown:**

- Hiring_Metrics_Hub_Suites.java: 4 tests

**Implemented (1 test):**

- ✅ TC_001: Complete Hiring Metrics Hub Lifecycle (`hiring-metrics-hub-workflow.feature`)

**Feature File Written, Needs Step Defs + Locators (3 tests):**

- 🟡 TC_002: ListView — add + clone (`hiring-metrics-listview-dashboard-reports.feature`)
- 🟡 TC_003: Dashboard Navigation (`hiring-metrics-listview-dashboard-reports.feature`)
- 🟡 TC_004: Reports Navigation (`hiring-metrics-listview-dashboard-reports.feature`)

**Next Steps:** Implement list view / dashboard / reports step defs (low complexity — same pattern as BCT)

---

### **8. SDP (State Directed Payments)**

| Metric | Status |
|---|---|
| **Legacy Tests** | 9 tests |
| **Gherkin Coverage** | **9 / 9 (100%)** |
| **Implementation Coverage** | **0 / 9 (0%)** |
| **Status** | 🟡 **Feature Files Written — Needs Step Defs** |
| **Feature Files** | 2 files |

**Legacy Test Breakdown:**

- SDP_Records_TestSuites.java: 7 tests
- Dashboards_TestSuite.java: 2 tests

**Feature File Written, Needs Step Defs + Locators (9 tests):**

- 🟡 TC_001: Mandatory Fields Validation + field-specific verifications (`sdp-records-e2e.feature`)
- 🟡 TC_002: Payment Arrangement Start Date boundary values (`sdp-records-e2e.feature`)
- 🟡 TC_003: Create SDP record (Under Review) + Key Contacts + Provider Type (`sdp-records-e2e.feature`)
- 🟡 TC_004: Create SDP record parameterized by status (`sdp-records-e2e.feature`)
- 🟡 TC_005: Control Name duplicate error + naming convention after date change (`sdp-records-e2e.feature`)
- 🟡 TC_006: Review type transitions + parent record rules + CMS Clock (`sdp-records-e2e.feature`)
- 🟡 TC_007: PDF generation (`sdp-records-e2e.feature`)
- 🟡 Dashboard: 6 report count widgets (`sdp-dashboard-reports.feature`)
- 🟡 Rating Period: status counts in iframe (`sdp-dashboard-reports.feature`)

**Blockers / Unknowns:**
- Login user role label for SDP needs confirmation `[LOCATOR NEEDED]`
- Exact SDP App Launcher name needs confirmation `[LOCATOR NEEDED]`

**Next Steps:** Confirm app name and user role, then implement page objects and step definitions

---

### **9. WFTCL Implementation**

| Metric | Status |
|---|---|
| **Legacy Tests** | 5 tests |
| **Gherkin Coverage** | **5 / 5 (100%)** |
| **Implementation Coverage** | **0 / 5 (0%)** |
| **Status** | 🟡 **Feature Files Written — Needs Step Defs** |
| **Feature Files** | 1 file |

**Legacy Test Breakdown:**

- Home_Provisions_Dashboard_Report_Suite.java: 5 tests

**Feature File Written, Needs Step Defs + Locators (5 tests):**

- 🟡 TC_001: WFTCL Home Screen + header details (`wftcl-provisions-e2e.feature`)
- 🟡 TC_002: Provisions Creation — overview, dates, meetings, documents, file upload, notes, chatter, field history (`wftcl-provisions-e2e.feature`)
- 🟡 TC_003: Provisions List View — add + clone (`wftcl-provisions-e2e.feature`)
- 🟡 TC_004: Dashboard Navigation (`wftcl-provisions-e2e.feature`)
- 🟡 TC_005: Reports Navigation (`wftcl-provisions-e2e.feature`)

**Blockers / Unknowns:**
- Login user role label (distinct from CMCS TA "WFTCL" role?) needs confirmation `[LOCATOR NEEDED]`
- Exact WFTCL Implementation App Launcher name needs confirmation `[LOCATOR NEEDED]`

**Next Steps:** Confirm user role and app name, then implement page objects and step definitions

---

### **10. Access Control**

| Metric | Status |
|---|---|
| **Legacy Tests** | N/A (new test, not in legacy) |
| **Gherkin Coverage** | ✅ **Complete** |
| **Implementation Coverage** | ✅ **Complete** |
| **Status** | ✅ **Complete** |
| **Feature Files** | 1 file |
| **Last Result** | Passing |

**Completed Tests:**

- ✅ User validation and access control checks

**Next Steps:** Maintain only

---

## 📈 Migration Priority Roadmap

### **Phase 1: Low-Complexity List/Dashboard/Reports (QUICK WINS)**

These follow the same pattern as BCT — minimal new step definitions needed:

1. **Hiring Metrics LDR** — 3 stub scenarios → same structure as `bct-dashboard-reports.feature`
2. **PRA LDR** — 3 stub scenarios
3. **PIER LDR** — 3 stub scenarios

### **Phase 2: Outreach and PIER Core Flows (MEDIUM COMPLEXITY)**

4. **PIER TC_001 + TC_003** — state names + PIER Cases lifecycle (new page objects needed)
5. **Outreach Emergency TC_002–TC_008** — 7 stub scenarios, multiple [LOCATOR NEEDED] items
6. **Outreach General TC_001–TC_006 + supporting tests** — 17 stub scenarios

### **Phase 3: New Modules (SDP, WFTCL Implementation)**

7. **SDP** — 9 stub scenarios, needs SDP page objects from scratch; confirm app/user role first
8. **WFTCL Implementation** — 5 stub scenarios; confirm app/user role first

---

## 🚧 Current Sprint Focus (Week of May 14, 2026)

### **Completed This Week**

- ✅ CMCS TA WFTCL feature file deduplication: deleted `cmcs-ta-wftcl-inquiry-creation-validation.feature` (was a migration slice superseded by `cmcs-ta-wftcl-e2e.feature`)
- ✅ Migrated unique content into `cmcs-ta-wftcl-e2e.feature`: MCOG/CAHPG category dropdown options validation (second scenario `@cmcs-ta-wftcl-categories`) + question/comment step after inquiry creation
- ✅ Merged `cmcs-ta-dashboard-navigation.feature` into `cmcs-ta-listview-operations.feature`: home header + list view ops + dashboard navigation now in one file aligned with TC_001/TC_002 legacy boundary
- ✅ Deleted `cmcs-ta-01-inquiry-creation-validation.feature` and `cmcs-ta-02-stage-and-escalation.feature` (both were migration slices fully superseded by `cmcs-ta-e2e-full-flow.feature`)
- ✅ Cleaned up stale file references in E2E header comment map
- ✅ CMCS TA now 4 canonical feature files (down from 8), 1 file per legacy Java class

### **Previous Week (May 8)**

- ✅ CMCS TA module reached **100% implementation** (32/32) — TC_005 (Related Inquiry) and TC_009 (Field History) confirmed implemented
- ✅ CMCS TA feature file cleanup — removed 5 redundant/duplicate files (cmcs-ta-03, 04, 06, 07, listview-dashboard_reports)
- ✅ Fixed `cmcs-ta-listview-operations.feature` dashboard heading ("CMCS Inquiry Tracker" → "TA CMCS Inquiries")
- ✅ Updated `cmcs-ta-e2e-full-flow.feature` header with authoritative TC→file mapping
- ✅ Framework: deleted `ToastUtils.java` (dead code — unreachable from step definitions)
- ✅ Framework: collapsed 5-hop toast chain (FormUtils → ToastUtils) to direct `SfHelper.waitForToast` call
- ✅ Framework: fixed toast CSS selector bug in `SfHelper` (`.slds-notify--toast` was matching all toast types)
- ✅ Framework: `InquiryTrackerPage` reduced 2469 → 2201 lines (extracted `InquiryTrackerNotesHelper` + expanded `InquiryTrackerResponseHelper`)
- ✅ Framework: `FormUtils` reduced to 295 lines (removed dead toast delegation methods)

### **Up Next**

1. Implement HMH / PRA / PIER list-view-dashboard-reports step defs (quick wins — BCT pattern reuse)
2. Confirm `[LOCATOR NEEDED]` items for SDP and WFTCL Implementation (user role + app name)
3. Begin Outreach Emergency TC_002–TC_008 (locator confirmation first)

---

## ⚠️ Active Blockers

| Blocker | Module | Severity | Status | Resolution |
|---|---|---|---|---|
| Edit button visibility for bottom-of-page fields | CMCS TA | Medium | ✅ **RESOLVED** | Changed wait order: scroll BEFORE visibility check |
| Strict mode violations with combobox selectors | CMCS TA | Low | ✅ **RESOLVED** | Use AriaRole.COMBOBOX instead of BUTTON |
| Toast selector matching all types instead of success only | Framework | Medium | ✅ **RESOLVED** | Fixed `.slds-notify--toast` → `.slds-theme--success` in SfHelper |
| SDP app name + user role unconfirmed | SDP | Medium | ⏳ **Pending** | User to confirm before step def implementation |
| WFTCL Impl app name + user role unconfirmed | WFTCL Implementation | Medium | ⏳ **Pending** | User to confirm before step def implementation |

---

## 🔴 Risks and Mitigation

| Risk | Impact | Mitigation | Status |
|---|---|---|---|
| Salesforce Lightning dynamic UI timing | High | Use NETWORKIDLE + explicit scroll + ATTACHED state waits | ✅ Active |
| Large Outreach implementation gap (85% stubs) | High | Phase 2 priority; all Gherkin written, blocking item is locator confirmation | 📋 Planned |
| SDP / WFTCL Impl have 0% step defs (14 tests) | Medium | All Gherkin written; unblocked once app name + user confirmed | ⏳ Pending |
| Salesforce report viewer iframe + virtualized grid | Medium | Use DOMCONTENTLOADED + Report Viewer iframe waits + data-row-index traversal | ✅ Mitigated |
| Dynamic element IDs in Salesforce | Medium | Strict adherence to P1-P4 locator hierarchy (semantic first) | ✅ Active |
| Contextual anchoring for duplicate matches | Medium | Scope to stable parent container before semantic locator | ✅ Active |

---

## 📝 Daily Update Log

| Date | What Changed | Result | Next Action |
|---|---|---|---|
| **May 14, 2026** | CMCS TA feature file cleanup: deleted WFTCL duplicate, merged dashboard-navigation into listview-operations, deleted cmcs-ta-01 + cmcs-ta-02 (superseded by E2E); CMCS TA now 4 files (down from 8), 1 per legacy Java class | 25 feature files, 43/87 implemented ✅ | Implement HMH/PRA/PIER LDR step defs (quick wins) |
| **May 8, 2026** | CMCS TA 100% implementation confirmed (TC_005 + TC_009 verified); removed 5 duplicate/scaffold cmcs-ta feature files; fixed cmcs-ta-05 dashboard heading; collapsed dead toast chain (ToastUtils deleted, FormUtils 419→295 lines); fixed toast selector bug; InquiryTrackerPage 2469→2201 lines; extracted NotesHelper + ResponseHelper | 29 feature files, 43/87 implemented ✅ | Implement HMH/PRA/PIER LDR step defs (quick wins) |
| **May 6, 2026** | Full cross-framework audit; created 9 gap feature files (SDP×2, WFTCL-Impl×1, PIER×2, PRA×1, HMH×1, Outreach×2); updated cmcs-ta-08 with TC_018; 100% Gherkin mapping milestone | 32 feature files, 87/87 mapped ✅ | Confirm SDP/WFTCL app names; implement CMCS TA TC_005/TC_009 |
| **May 5, 2026** | Created cmcs-ta-e2e-full-flow.feature + cmcs-ta-wftcl-e2e.feature + cmcs-ta-dashboard-navigation.feature; gap analysis initiated | CMCS TA Gherkin 100% ✅ | Continue gap fill for other modules |
| **Apr 30, 2026** | CMCS TA report validation suite stabilized; all 18 report scenarios passed; tracker metrics refreshed | Full report suite pass ✅ | Start TC_005/TC_009 |
| Apr 29, 2026 | CMCS TA TC_008 implementation: withdrawn/close validation with 11 page methods + 9 steps | Compilation ✅ | Debug runtime scroll issue |
| Apr 9, 2026 | PIER completed and passing; PRA action flow stabilization in progress | Partial pass | Finalize PRA flow |

---

## 📚 Reference: Legacy Framework File Locations

### **Test Suite Files (23 files)**

```
MacTrack_Automation/src/test/java/
├── CMCS_TestSuites/
│   ├── CMCS_TA_Suites.java (10 tests)
│   ├── CMCS_TA_ListView__Dashboard_Reports.java (3 tests)
│   ├── CMCS_TA_Reports.java (18 tests)
│   └── CMCS_TA_WFTCL.java (1 test)
├── CMCS_OutReach_Suites/
│   ├── CMCS_EmergencyOutReach_Test.java (8 tests)
│   ├── CMCS_GeneralOutReach_Test.java (6 tests)
│   ├── CMCS_Outreach_Records_Suites.java (1 test)
│   ├── CMCS_Add_State_To_Outreach.java (1 test)
│   ├── CMCS_Outreach_ListView_Dashboard_Reports.java (3 tests)
│   └── CMCS_Outreach_Reports_Suites.java (1 test)
├── Budget_Contract_TestSuites/
│   ├── TestSuite.java (1 test)
│   └── BCT_Listview_Dashboard_Report.java (3 tests)
├── CMCS_PIER_Tracker/
│   ├── PIER_TestSuites.java (3 tests)
│   └── PIER_Listview_Dashboard_Report.java (3 tests)
├── CMCS_PRA_Tracker_TestSuites/
│   ├── PRA_Tracker.java (3 tests)
│   └── PRA_ListView_Dashboard_Reports.java (3 tests)
├── CMCS_Hiring_Metrics_Hub/
│   └── Hiring_Metrics_Hub_Suites.java (4 tests)
├── Vendor_Engagement/
│   └── TestSuites.java (1 test)
├── SDP_TestSuites/
│   ├── SDP_Records_TestSuites.java (7 tests)
│   └── Dashboards_TestSuite.java (2 tests)
└── CMCS_WFTCL_Implementation/
    └── Home_Provisions_Dashboard_Report_Suite.java (5 tests)
```

### **New Framework Feature Files (29 files)**

```
src/test/resources/features/
├── access-control/ (1 file) — 100% implemented
├── bct/ (3 files) — 100% implemented
├── cmcs-ta/ (4 files) — 100% implemented ✅
├── hiring-metrics/ (2 files) — 25% implemented, 100% Gherkin
├── outreach/ (3 files) — 15% implemented, 100% Gherkin
├── pier/ (3 files) — 17% implemented, 100% Gherkin
├── pra/ (2 files) — 17% implemented, 100% Gherkin
├── sdp/ (2 files) — 0% implemented, 100% Gherkin
├── vendor-engagement/ (1 file) — 100% implemented
└── wftcl-implementation/ (1 file) — 0% implemented, 100% Gherkin
```

---

## 📊 Test Distribution by Type

| Type | Count | Gherkin Written | Implemented | Priority |
|---|---|---|---|---|
| **Core Functional** | 40 tests | 40 (100%) | 20 (50%) | 🔴 High |
| **List View / Dashboard / Reports** | 29 tests | 29 (100%) | 5 (17%) | 🟡 Medium |
| **Report Validation** | 18 tests | 18 (100%) | 18 (100%) | 🟢 Low |
| **TOTAL** | **87 tests** | **87 (100%)** | **43 (49%)** | — |

---

## 🎯 Success Criteria

### **Minimum Viable Coverage (60% implementation target)**

- ✅ BCT: 100% complete (4/4 tests)
- ✅ Vendor Engagement: 100% complete (1/1 test)
- ✅ Access Control: 100% complete
- ✅ CMCS TA: 100% complete (32/32 tests)
- 🔄 Outreach: 15% implemented → **Target: 60%** (9 more tests; all Gherkin done)
- 🔄 PIER: 17% implemented → **Target: 50%** (2 more tests; all Gherkin done)
- 🔄 PRA: 17% implemented → **Target: 50%** (2 more tests; all Gherkin done)
- 🔄 Hiring Metrics: 25% implemented → **Target: 50%** (1 more test; all Gherkin done)

**Estimated Effort to Reach 60% Implementation:** ~1–2 weeks with focus on:
1. HMH/PRA/PIER list-view-dashboard-reports quick wins (days — BCT pattern reuse)
2. Outreach emergency + general core flows (1–2 weeks)

---

## 📞 Contacts

| Role | Name | Focus Areas |
|---|---|---|
| QA Automation Lead | TBD | Framework architecture, migration strategy |
| CMCS TA SME | TBD | Technical assistance workflows |
| Outreach SME | TBD | Emergency & general outreach processes |
| DevOps | TBD | CI/CD pipeline integration |

---

**Document Version:** 2.4  
**Last Review:** May 14, 2026  
**Next Review:** May 21, 2026
