# Terraform Backend Infrastructure

This directory contains the Terraform configuration for creating the S3 bucket and KMS key used to store Terraform state files.

## Security Features

- **KMS Encryption**: All state files are encrypted at rest using AWS KMS with automatic key rotation
- **SSL/TLS Only**: Bucket policy enforces HTTPS/TLS for all connections
- **Versioning**: Enabled to track state file history and allow rollback
- **Public Access Blocked**: All public access is blocked at the bucket level
- **Lifecycle Management**: Old versions are automatically deleted after 90 days

## Setup Instructions

### 1. Initialize and Apply (First Time Only)

This backend infrastructure must be created BEFORE other Terraform projects can use it:

```bash
cd infrastructure/terraform-backend
terraform init
terraform apply
```

### 2. Note the Outputs

After applying, note the bucket name and KMS key ARN for use in other projects.

### 3. Configure Other Projects

Other Terraform projects should reference this backend in their `main.tf`:

```hcl
terraform {
  backend "s3" {
    bucket       = "macpro-macsuite-terraform-state"
    key          = "{environment}/{project-name}/terraform.tfstate"
    region       = "us-east-1"
    encrypt      = true
    use_lockfile = true  # Terraform 1.11+ native S3 locking
  }
}
```

## Multi-Environment Support

This backend supports multiple environments using path-based isolation:

```
s3://macpro-macsuite-terraform-state/
├── dev/
│   └── github-runner/terraform.tfstate
├── qa/
│   └── github-runner/terraform.tfstate
├── uat/
│   └── github-runner/terraform.tfstate
└── prod/
    └── github-runner/terraform.tfstate
```

### Example: GitHub Runner Dev Environment

```hcl
backend "s3" {
  bucket       = "macpro-macsuite-terraform-state"
  key          = "dev/github-runner/terraform.tfstate"
  region       = "us-east-1"
  encrypt      = true
  use_lockfile = true
}
```

## Important Notes

- This configuration uses **local state** because it's the bootstrap for remote state
- The local state file (`terraform.tfstate`) should be committed to git for this backend-only project
- Once created, this infrastructure should rarely need updates
- Requires Terraform >= 1.11 for native S3 locking support

## Maintenance

### View Current State
```bash
terraform show
```

### Update Resources
```bash
terraform plan
terraform apply
```

### Destroy (Use with extreme caution!)
```bash
# This will delete ALL Terraform state files in the bucket!
terraform destroy
```