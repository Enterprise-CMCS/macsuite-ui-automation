---
name: automation-new-test-case
description: "Use when adding a new MACSuite automated test case, feature scenario, step definition, or page action for the Java Playwright BDD framework; requires Playwright CLI verification for UI discovery and strict reuse of existing framework abstractions."
allowed-tools: read, search, execute
user-invocable: true
---

# Automation New Test Case

## Goal
Create a new automated scenario with the smallest framework footprint.

## Use When
- A new feature scenario needs automation
- A new step definition is required for an existing flow
- A page action is missing and must be added safely

## Workflow
1. Read the feature requirement and confirm the exact business flow
2. Check existing shared utilities and page methods first
3. Use the installed `playwright-cli` / `@playwright/cli` to inspect the live UI when locator details are unclear
4. Add or extend the feature, step, page, or helper in the correct layer
5. Keep business text in the feature file when it drives locator lookup
6. Validate with the narrowest relevant compile or test command

## Implementation Rules
- Do not put page logic in step definitions
- Do not duplicate `SfPage`, `FormUtils`, `SfHelper`, `DropdownUtils`, `LookupUtils`, or `RichTextUtils`
- Prefer stable roles, labels, and data attributes over dynamic selectors
- Keep scenario sequencing intact for legacy chained flows
- Use feature parameters or DataTables for business-facing values

## Required Checks
- Confirm no existing step already covers the action
- Confirm no existing page/helper method already solves the behavior
- Confirm live UI evidence with Playwright CLI if the locator is not already proven
- Confirm the change compiles before broader execution

## Expected Output
- New or updated feature step
- Minimal page/helper support if needed
- Validation notes with evidence source
