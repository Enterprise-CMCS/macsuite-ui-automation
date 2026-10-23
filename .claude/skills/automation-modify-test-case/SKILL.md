---
name: automation-modify-test-case
description: "Use when modifying an existing MACSuite automated test case, feature scenario, step definition, or page object while preserving framework stability, chained flows, and shared abstractions."
allowed-tools: read, search, execute
user-invocable: true
---

# Automation Modify Test Case

## Goal
Make the smallest safe change to an existing automation flow.

## Use When
- A field value changes
- A locator needs to be hardened
- A feature scenario needs a small update
- A page method needs a safer selector or wait strategy

## Modification Workflow
1. Read the current implementation and the existing feature flow
2. Reuse the nearest shared or module-level abstraction
3. Use the installed `playwright-cli` / `@playwright/cli` to confirm live UI state when the locator is uncertain
4. Edit the smallest layer that owns the behavior
5. Validate with a narrow compile or focused test run

## Modification Rules
- Keep backward compatibility unless the change is explicitly approved as breaking
- Do not duplicate shared utilities or page helpers
- Do not rewrite chained-flow feature architecture unless asked
- Prefer exact business labels only when they are verified against the live UI
- Keep hardcoded values limited to stable technical anchors

## Required Checks
- Existing reuse reviewed first
- UI evidence collected if needed
- Smallest-file change chosen
- Validation completed after the edit

## Expected Output
- Targeted code change
- Validation command and result
- Any residual risk noted explicitly
