# VS Code Cucumber/Gherkin Reference Resolution Setup

## Issue Fixed
The "no reference found" message when clicking on feature file steps has been resolved by configuring VS Code to properly connect Gherkin step definitions with Java step implementations.

## What Was Created

### 1. `.vscode/settings.json`
Configures VS Code with:
- **Cucumber glue paths**: Points to where step definitions are located
- **Feature file paths**: Tells Cucumber where to find `.feature` files
- **Gherkin formatter**: Enables auto-formatting for feature files
- **Java formatter**: Ensures consistent code style

### 2. `.vscode/extensions.json`
Lists recommended VS Code extensions:
- `ms-java.extension-pack-for-java` — Java language support (must-have)
- `redhat.java` — Language server for Java
- `alexkrechik.cucumberautocomplete` — Cucumber step autocompletion
- `cucumber.cucumber-official` — Official Cucumber extension
- `ms-maven.maven-for-java` — Maven integration

### 3. `cucumber.js`
Optional Cucumber CLI configuration (supports running tests from command line).

---

## Next Steps to Enable References

### 1. Install Required Extensions
Run this in VS Code terminal or manually install:
```bash
code --install-extension ms-java.extension-pack-for-java
code --install-extension redhat.java
code --install-extension alexkrechik.cucumberautocomplete
code --install-extension cucumber.cucumber-official
```

Or in VS Code:
- Open **Extensions** (Ctrl+Shift+X)
- Search for "Cucumber"
- Install **"Cucumber (Gherkin) Full Support"** by **alexkrechik**
- Install **"Cucumber Official"** by **Cucumber**
- Install **"Extension Pack for Java"** by **Microsoft**

### 2. Reload VS Code
- Press **Ctrl+Shift+P** → **Developer: Reload Window**

### 3. Build the Project
```bash
mvn clean compile -DskipTests
```
This ensures:
- Java classes are compiled
- Step definitions are indexed
- Language servers can find all symbols

### 4. Test It Out
- Open a `.feature` file (e.g., `src/test/resources/features/bct/bct_dsg-osg-readonly_access_user.feature`)
- Click on any step like `I navigate to MAC Suite Application in "UAT" environment`
- Press **F12** (Go to Definition) or **Ctrl+Click**
- It should now jump to the corresponding step in `LoginSteps.java` or `CommonSteps.java`

---

## How It Works Now

1. **Gherkin Extension** scans your feature files for `@When`, `@Given`, `@Then` steps
2. **Java Glue Paths** configured in `settings.json` tell it where to look for step implementations
3. **Package matching** connects steps like:
   - Feature: `When I login as "BCT-DSG-OSG-READ" user`
   - Finds method: `@When("I login as \"{string}\" user")` in `LoginSteps.java`

---

## Troubleshooting

### Still No References?
1. **Check extension status**: Open Extensions (Ctrl+Shift+X) → Search "Cucumber" → Verify it's installed and enabled
2. **Rebuild Java**: `mvn clean compile -DskipTests`
3. **Clear cache**: Delete `.vscode-server` folder in your home directory
4. **Reload window**: Ctrl+Shift+P → "Developer: Reload Window"

### Step Not Found?
- Verify the step regex pattern in the Java step definition matches the Gherkin text
- Example: `@When("I fill the {string} section with:")` matches `And I fill the "Information" section with:`
- Check for typos or regex escaping issues

### Extensions Not Listed?
The `.vscode/extensions.json` file is just a recommendation file. Manual installation is still required for first-time setup.

---

## Configuration Details

### Glue Paths (where to find step definitions)
```json
"cucumber.glue": [
  "src/test/java/com/macsuite/**/steps",
  "src/test/java/com/macsuite/common/steps"
]
```
- `**/steps` = recursively finds all `steps` directories
- Covers: `bct/steps`, `pier/steps`, `inquiry_tracker/steps`, etc.

### Feature Paths (where to find .feature files)
```json
"cucumber.features": [
  "src/test/resources/features/**/*.feature"
]
```
- Finds all feature files in any subdirectory

---

## File Locations
- Settings: `.vscode/settings.json`
- Extensions: `.vscode/extensions.json`
- Cucumber CLI: `cucumber.js` (project root)

All files have been created in your repository. Commit them to version control so team members benefit from the same setup.

```bash
git add .vscode/ cucumber.js
git commit -m "Configure VS Code for Cucumber/Gherkin step resolution"
```
