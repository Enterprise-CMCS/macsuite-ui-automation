# GHA Infrastructure

Terraform configuration for GitHub Actions infrastructure supporting the [Enterprise-CMCS/macsuite-mcreview-mulesoft](https://github.com/Enterprise-CMCS/macsuite-mcreview-mulesoft) repository.

## Directory Structure

```
gha-infrastructure/
├── terraform-backend/   # Bootstrap: creates the S3 bucket + KMS key for remote Terraform state
└── github-runner/       # Deploys the CodeBuild-based GitHub Actions self-hosted runner
```

> **These must be applied in order.** `terraform-backend` creates the S3 bucket that `github-runner` uses as its remote state backend.

---

## Module: `terraform-backend`

Creates the S3 bucket and KMS key used to store all Terraform state files.

### Resources Created

- **S3 Bucket** — `macpro-macsuite-terraform-state`
- **KMS Key** — for state file encryption at rest (with automatic key rotation)

### Security Features

- KMS encryption on all state files
- SSL/TLS-only bucket policy (denies non-HTTPS access)
- Versioning enabled (state file history + rollback)
- All public access blocked
- Old versions auto-deleted after 90 days

### Notes

- Uses **local state** intentionally — it bootstraps the remote state backend, so it cannot use that backend itself
- **Do not commit `terraform.tfstate` to git** — it contains AWS resource ARNs and account-specific details (`terraform.tfstate` is already in `.gitignore`)
- Requires Terraform >= 1.11

### Apply

```bash
cd terraform-backend
terraform init
terraform apply
```

---

## Module: `github-runner`

Deploys a self-hosted GitHub Actions runner using AWS CodeBuild via the [`mac-fc-github-actions-runner-aws`](https://github.com/Enterprise-CMCS/mac-fc-github-actions-runner-aws) module (`v7.2.0`).

### Configuration

| Setting | Value |
|---|---|
| AWS Account | `666736308865` |
| Region | `us-east-1` |
| Environment | `dev` |
| GitHub Org | `Enterprise-CMCS` |
| Repository | `macsuite-mcreview-bidirection` |
| CodeBuild Project | `macsuite-mcreview-bidirection-dev-runner` |
| VPC | `vpc-0556ac8db732cd983` |
| Auth Method | GitHub App (AWS CodeConnections) |
| Compute Type | `BUILD_GENERAL1_MEDIUM` |
| Remote State Bucket | `macpro-macsuite-terraform-state` |

### First-Time Setup

**Step 1 — Apply**

```bash
cd github-runner
terraform init   # connects to the S3 backend created above
terraform apply
```

**Step 2 — Authorize the GitHub App connection**

After the first `apply`, a CodeConnections connection (`macsuite-mcreview-bidirectional-dev-runner`) is created in a **PENDING** state. You must manually authorize it:

1. Go to AWS Console → **Developer Tools → Connections**
2. Find `macsuite-mcreview-bidirectional-dev-runner` and click **Update pending connection**
3. Authorize the GitHub App for the `Enterprise-CMCS` org

**Step 3 — Enable webhooks**

Once the connection is authorized, set `skip_webhook_creation = false` in `terraform.tfvars` and re-apply:

```bash
terraform apply
```

---

## ⚠️ Destroy (Extreme Caution)

```bash
# Destroying terraform-backend will delete ALL Terraform state files stored in the bucket
terraform destroy
```