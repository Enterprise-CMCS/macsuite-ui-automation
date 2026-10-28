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

### Static Egress for MFP UAT

The MFP POH workflow targets the `mfp-uat-linux` runner label. Provision this
runner before dispatching the workflow; changing the label alone does not create
a runner or reserve an IP range.

1. In your GitHub organization, create a Linux x64 larger runner with a
   GitHub-owned Ubuntu image and the smallest suitable size. Larger runner and
   static IP availability depend on your organization's plan and permissions.
2. Name it `mfp-uat-linux` and confirm that its assigned runner label matches
   `runs-on` in the workflow. Enable unique, static public IP address ranges and
   set maximum concurrency to 1 initially.
3. Assign it to a dedicated MFP UAT runner group and grant this repository access
   (and authorize this workflow if the group restricts workflow access).
4. Retrieve every assigned static IP range from the runner's settings. Ask the
   Salesforce administrator to approve and add those ranges under Setup > Network
   Access in the UAT sandbox. Convert CIDRs to start/end IPs if required by that UI.
   Do not allowlist all GitHub-hosted runner ranges.
5. If the automation user's profile has Login IP Ranges, have the administrator
   check those restrictions separately. Network Access trust does not override
   profile restrictions or mandatory MFA/session policies.
6. Dispatch the initial login scenario below. Compare the workflow's existing
   `Display GitHub Runner IP` output with the assigned ranges and Salesforce
   Login History's Source IP and Status. Confirm successful login before running
   the full feature chain.

Expected behavior: trusted static egress addresses prevent new-IP computer
activation challenges where the sandbox's policy permits. This does not disable
mandatory MFA; keep the approved TOTP configuration below when required. An email
computer-activation code is not an authenticator TOTP code.

If the job remains queued, check runner provisioning, label, group access, and
available capacity. There is intentionally no `ubuntu-latest` fallback, because
that would reintroduce changing outbound IPs.

Salesforce Login History distinguishes two separate failures:

- `Failed: Computer activation required`: investigate trusted IP ranges and the
  organization's identity-verification policy.
- `Invalid Password`: verify the UAT account's exact username and password in
  GitHub secrets and check account status with the administrator. Static IPs
  cannot fix rejected credentials; avoid repeated retries that could lock it out.

### Authenticator Secrets

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