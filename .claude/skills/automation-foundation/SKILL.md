---
name: automation-foundation
description: "Use when creating, modifying, or debugging MACSuite Java Playwright BDD automation; establishes architecture-first rules, anti-hallucination checks, role boundaries, and Playwright CLI verification requirements for test automation work."
allowed-tools: read, search, execute
user-invocable: true
---

# Automation Foundation

## Purpose
Use this skill as the shared standard for all MACSuite automation work.

## When to Use
- Creating a new automated test case
- Debugging a failing test case
- Modifying an existing test case
- Reviewing or refining locators, step definitions, or page objects
- Verifying UI state before implementing a locator fix

## Framework Rules
- Follow the architecture chain: `StepBase` / `ModuleSteps<T>` -> step definitions -> `SfPage` -> module page -> helper
- Reuse shared utilities before adding new code
- Keep business logic in pages and helpers, not in steps
- Preserve chained-flow scenarios; do not force `Background` on legacy flows
- Prefer semantic locators and Salesforce-aware shared helpers before direct fallbacks
- Treat business-facing locator text as feature-file input when it varies by scenario or environment

## Anti-Hallucination Rules
- Never guess a locator, workflow, class, or field value without evidence
- Never invent a method or helper that is not already present or justified by the framework
- Use repository evidence, snapshots, traces, screenshots, or logs before changing locators
- If UI understanding is required, use the installed `playwright-cli` / `@playwright/cli` workflow to inspect the live page before editing code
- If a locator times out, resolves hidden content, or is ambiguous, stop and verify the live UI first

## Role Boundaries
- Automation Architect: decides the safe path, validates reuse, and approves the smallest change
- Test Case Builder: adds or extends feature, step, page, and helper coverage
- Test Case Debugger: reproduces failures, gathers evidence, and isolates the failing layer
- Test Case Maintainer: makes targeted fixes without rewriting stable framework behavior
- UI Locator Investigator: uses Playwright CLI to confirm live locators and UI state

## Playwright CLI Requirement
Use the installed `playwright-cli` as the first-choice UI evidence tool whenever locator discovery or page understanding is required.
Use `@playwright/cli` wording interchangeably with the installed command in agent-facing guidance so the workflow remains discoverable.
Prefer:
- `snapshot`
- `show --annotate`
- `tracing-start` / `tracing-stop`
- screenshots
- console and request inspection

## Output Expectations
- State the hypothesis before editing
- Choose the smallest safe change
- Validate after every substantive edit
- Summarize what changed and what remains risky
