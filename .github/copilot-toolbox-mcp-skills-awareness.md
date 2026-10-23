# GitHub Copilot Toolbox — MCP & Skills awareness

_Generated: 2026-09-14T11:36:23.898Z_

## How to use this report

- **Saved copy:** This file is **`.github/copilot-toolbox-mcp-skills-awareness.md`** — refreshed whenever the toolbox runs an MCP & Skills scan (including on workspace open when auto-scan is enabled). It is meant for **Copilot workspace context** together with `.github/copilot-instructions.md` (which gets a shorter replaceable summary when auto-merge is on).
- **MCP:** Lists **configured** servers from `mcp.json`. **Live tool use** still requires **Copilot Chat → Agent** with those servers **trusted/started** in the MCP tools UI.
- **Skills:** **On-disk** folders with `SKILL.md`. Copilot does not auto-load them; attach `SKILL.md` or paths in chat when useful.
- **Task routing:** When the user’s request matches a server’s purpose (e.g. Confluence → Confluence/Atlassian MCP), prefer that **server id** from the tables below.

---

## MCP — workspace

Workspace `mcp.json` _(folder: MACSuite_UI_Automation)_

- **c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.vscode\mcp.json** — _File missing_

_No active workspace servers in mcp.json._

## MCP — user profile

- **C:\Users\PankajGhosh\AppData\Roaming\Code\User\mcp.json** — _File missing_

_No active user-scoped servers in mcp.json._

## Skills (local `SKILL.md` folders)

### Project-scoped

- **automation-debug-test-case** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\automation-debug-test-case`
  - Use when debugging a failing MACSuite automated test case, unstable locator, or chained scenario run in the Java Playwright BDD framework; requires Playwright CLI snapshots or traces before locator changes and isolates t

- **automation-foundation** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\automation-foundation`
  - Use when creating, modifying, or debugging MACSuite Java Playwright BDD automation; establishes architecture-first rules, anti-hallucination checks, role boundaries, and Playwright CLI verification requirements for test 

- **automation-modify-test-case** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\automation-modify-test-case`
  - Use when modifying an existing MACSuite automated test case, feature scenario, step definition, or page object while preserving framework stability, chained flows, and shared abstractions.

- **automation-new-test-case** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\automation-new-test-case`
  - Use when adding a new MACSuite automated test case, feature scenario, step definition, or page action for the Java Playwright BDD framework; requires Playwright CLI verification for UI discovery and strict reuse of exist

- **playwright-cli** — `c:\Users\PankajGhosh\Automation\MACSuite_UI_Automation\.claude\skills\playwright-cli`
  - Automate browser interactions, test web pages and work with Playwright tests.

### User-scoped

_None found._

---

## Suggested next steps

- **MCP:** Command Palette → `MCP: List Servers` (or this extension’s hub **MCP** tab) → start/trust servers in **Copilot Chat → Agent → tools**.
- **Edit config:** `MCP: Open Workspace Folder MCP Configuration` / `MCP: Open User Configuration`.
- **Refresh this report:** run **Intelligence — scan MCP & Skills awareness** again after changing `mcp.json` or adding skills.

_Report from GitHub Copilot Toolbox extension._
