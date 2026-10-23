# Authentication State Management

This directory contains pre-authenticated browser states for each role and environment combination. Using these state files eliminates the need to log in for every test, significantly improving test execution speed.

## Benefits

- **Faster execution**: Skip login for 90%+ of tests (save 5-10 seconds per test)
- **Reduced flakiness**: Fewer authentication-related timeouts
- **Cleaner test data**: Login flow tested separately, not as side effect
- **Better parallelization**: No login throttling issues

## Directory Structure

```
auth-states/
├── README.md
├── generate-auth-states.ps1     ← Script to generate all state files
├── qa/                          ← QA environment states
│   ├── bct-qa.json
│   ├── so-qa.json
│   ├── ta-qa.json
│   ├── pier-qa.json
│   ├── pra-qa.json
│   ├── hmh-qa.json
│   ├── ve-qa.json
│   ├── mfp-qa.json
│   ├── wftcl-qa.json
│   └── prareadonly-qa.json
├── uat/                         ← UAT environment states
│   ├── bct-uat.json
│   ├── so-uat.json
│   └── ... (same roles as QA)
└── sei/                         ← SEI environment states
    ├── bct-sei.json
    ├── so-sei.json
    └── ... (same roles as QA)
```

## Quick Start

### 1. Generate All Auth States (Recommended)

Run the automated script to generate all states at once:

```powershell
cd auth-states
.\generate-auth-states.ps1
```

This will:
- Read credentials from `.env` file
- Launch browser for each role/env combination
- Perform login
- Save authenticated state as JSON
- Close browser and move to next

**Time estimate**: ~5-10 minutes for all combinations

### 2. Generate Single Auth State (Manual)

For a specific role/environment:

```powershell
# Example: Generate BCT QA auth state
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd open --browser=chrome --headed https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com

# Login manually or use fill commands:
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd snapshot
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd fill e1 "pghosh@index-analytics.com.bct-qa"
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd fill e2 "Tracker_bct0406"
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd click e3

# After successful login, save state:
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd state-save auth-states/qa/bct-qa.json

# Close browser
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd close
```

## Using Auth States in Tests

### Automatic (Recommended)

In your feature file, use the `@Role` and `@Environment` tags:

```gherkin
@BCT @QA @AuthState
Feature: BCT Budget Creation
  
  Scenario: Create new budget
    Given I am logged in as BCT user  # Uses qa/bct-qa.json automatically
    When I navigate to Budget page
    # ... rest of scenario
```

The framework will automatically load the correct auth state based on tags.

### Manual (For specific scenarios)

```java
// In step definition or hook:
PlaywrightManager.init("test-video-name", "auth-states/qa/bct-qa.json");
```

## Maintenance

### When to Regenerate

Regenerate auth states when:
- ✅ Password changes for a role
- ✅ Salesforce session configuration changes
- ✅ New role/environment combination added
- ✅ Auth state file is older than 30 days (sessions may expire)

### Validation

Test if an auth state is still valid:

```powershell
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd open --browser=chrome --headed https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd state-load auth-states/qa/bct-qa.json
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd goto https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd snapshot
# Check if you're logged in or see login page
C:\Users\FerkatJawdat\AppData\Roaming\npm\playwright-cli.cmd close
```

## Security Notes

⚠️ **Auth state files contain session tokens and cookies**

- **DO NOT commit to version control** (already in `.gitignore`)
- Store in secure location for CI/CD (Azure KeyVault, GitHub Secrets)
- Regenerate periodically for security hygiene
- Each developer should generate their own states locally

## Troubleshooting

### "Authentication failed" after loading state

**Cause**: Session expired or Salesforce IP restrictions  
**Fix**: Regenerate the auth state file

### "File not found" error

**Cause**: Auth state not generated yet  
**Fix**: Run `generate-auth-states.ps1` or generate manually

### Tests still logging in

**Cause**: Feature file missing `@AuthState` tag or wrong role tag  
**Fix**: Verify tags in `.feature` file match role naming

### Browser opens but immediately closes

**Cause**: Invalid auth state JSON  
**Fix**: Delete the corrupted file and regenerate

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Generate Auth States
  env:
    BCT_QA_USERNAME: ${{ secrets.BCT_QA_USERNAME }}
    BCT_QA_PASSWORD: ${{ secrets.BCT_QA_PASSWORD }}
  run: |
    cd auth-states
    ./generate-auth-states.ps1
    
- name: Run Tests with Auth States
  run: mvn clean test -DuseAuthStates=true
```

### Azure DevOps Example

```yaml
- task: PowerShell@2
  inputs:
    filePath: 'auth-states/generate-auth-states.ps1'
  env:
    BCT_QA_USERNAME: $(BCT_QA_USERNAME)
    BCT_QA_PASSWORD: $(BCT_QA_PASSWORD)
```

## File Format

Auth state JSON structure:

```json
{
  "cookies": [
    {
      "name": "sid",
      "value": "00D...",
      "domain": ".salesforce.com",
      "path": "/",
      "expires": 1735689600,
      "httpOnly": true,
      "secure": true,
      "sameSite": "None"
    }
  ],
  "origins": [
    {
      "origin": "https://cmsapps5--cmcsmcqa.sandbox.my.salesforce.com",
      "localStorage": [
        {"name": "sid", "value": "..."},
        {"name": "inst", "value": "..."}
      ]
    }
  ]
}
```

## Support

For issues or questions, contact the automation team or create an issue in the repository.
