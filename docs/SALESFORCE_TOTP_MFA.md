# Salesforce TOTP MFA

Use this only for an administrator-approved automation account enrolled in a
third-party one-time password authenticator. It does not handle push approval,
passkeys, biometric prompts, or authenticator registration.

## Configure the Account

1. Ask your Salesforce administrator to approve the automation account and method.
2. In that user's personal settings, open Advanced User Details.
3. Connect App Registration: One-Time Password Authenticator, if available.
4. Select the manual setup option and securely retain its Base32 setup key.
5. Complete enrollment using a code from the registered authenticator app.

Never paste the setup key or a verification code into chat, source files, or logs.
Keep runner clocks synchronized. Use only permissions required for the tests.

## Local Configuration

This repository keeps its existing credential names; the generic username/password
names in the external guide are not used here.

```properties
MFP_UAT_USERNAME=<automation-user>
MFP_UAT_PASSWORD=<password>
APP_URL_UAT=<sandbox-login-url>
MFP_UAT_TOTP_SECRET=<base32-setup-key-for-this-user>
```

Set these in the local environment or uncommitted local environment file. Each
role/environment resolves its matching `{ROLE}_{ENV}_TOTP_SECRET`. If absent or
blank, `SALESFORCE_TOTP_SECRET` is the optional fallback. Use role-specific keys
when different personas have different Salesforce accounts.

## GitHub Actions

1. Open Settings > Secrets and variables > Actions in the repository.
2. Add `MFP_UAT_TOTP_SECRET` with the enrolled user's Base32 setup key, not a
   six-digit verification code. Alternatively add `SALESFORCE_TOTP_SECRET` for
   the single-account fallback.
3. Keep the existing UAT username, password and URL secrets.
4. Run the workflow with `@MFP_POH_Accounts_ListView_Test` for the initial login
   scenario. Dependent scenarios must still include their prerequisite scenario.

Both TOTP secrets are optional. A login without an MFA challenge works without
either. If a verification-code challenge appears without a configured key, the
test fails with a configuration message instead of silently skipping MFA.

## Behavior and Diagnostics

The login waits for App Launcher, Salesforce credential rejection, or the labeled
Verification Code input. If challenged, it generates a six-digit HMAC-SHA1 TOTP
using a 30-second period, submits Verify, and waits for App Launcher. No automatic
retry is performed to avoid repeated rejected attempts against the account.

The sensitive fill helper does not log code values and masks the code input for
screenshots/video. The standard Cucumber login step discards its authentication
trace on success or failure before starting a fresh trace for subsequent actions.
Consequently login navigation/network history is deliberately absent from saved
scenario traces. Direct calls to LoginPage outside that step must likewise avoid
recording authentication traces. Treat all browser artifacts as sensitive and
restrict artifact access. Do not enable Playwright API/protocol debug logging for
authentication runs; it can record fill arguments.

If verification fails, check account enrollment, which role-specific secret was
configured, and runner clock synchronization. Timeouts do not prove the code was
rejected. Capture sanitized UI evidence before adding selectors for another MFA
screen. TOTP handling has local fixture and RFC-vector coverage; a live Salesforce
run is still needed to validate your organization's actual authentication policy.