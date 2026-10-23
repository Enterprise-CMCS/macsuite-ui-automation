---
name: automation-debug-test-case
description: "Use when debugging a failing MACSuite automated test case, unstable locator, or chained scenario run in the Java Playwright BDD framework; requires Playwright CLI snapshots or traces before locator changes and isolates the failing layer first."
allowed-tools: read, search, execute
user-invocable: true
---

# Automation Debug Test Case

## Goal
Find the exact failing layer and fix only that layer.

## Use When
- A scenario fails during execution
- A locator times out or resolves hidden content
- A chained scenario starts at the wrong step
- A test passes only when prerequisites are run together

## Debug Workflow
1. Reproduce the failure with the narrowest tag, feature, or scenario scope
2. Use the installed `playwright-cli` / `@playwright/cli` snapshot, annotate, trace, or screenshot evidence if UI state is unclear
3. Identify whether the failure is in the feature, step, page, helper, or shared utility layer
4. Make the smallest fix that addresses the observed failure
5. Re-run the same focused validation immediately

## Debug Rules
- Do not guess at selectors from error text alone
- Do not broaden scope before the first focused validation
- Do not refactor stable code while chasing a single failure
- Preserve prerequisite chaining for dependent tags
- Prefer semantic waits and visible-only locators over timing workarounds

## Evidence Checklist
- Failing step text
- Screenshot or snapshot of the live UI
- Trace or console output when the page state is ambiguous
- Exact file and method that owns the failing behavior

## Expected Output
- Root-cause hypothesis
- Evidence used to confirm it
- Minimal code change
- Validation result
