terraform {
  required_version = ">= 1.11"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Environment = "shared"
      ManagedBy   = "Terraform"
      Component   = "terraform-backend"
      Project     = "macpro-macsuite"
    }
  }
}

# KMS key for S3 bucket encryption
resource "aws_kms_key" "terraform_state" {
  description             = "KMS key for Terraform state bucket encryption"
  deletion_window_in_days = 10
  enable_key_rotation     = true

  tags = {
    Name = "terraform-state-encryption-key"
  }
}

resource "aws_kms_alias" "terraform_state" {
  name          = "alias/terraform-state"
  target_key_id = aws_kms_key.terraform_state.key_id
}

# S3 bucket for Terraform state using the official AWS S3 module
module "terraform_state_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "~> 4.0"

  bucket = var.bucket_name

  # Block all public access
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true

  # Enable versioning for state file history
  versioning = {
    enabled = true
  }

  # Server-side encryption with KMS
  server_side_encryption_configuration = {
    rule = {
      apply_server_side_encryption_by_default = {
        sse_algorithm     = "aws:kms"
        kms_master_key_id = aws_kms_key.terraform_state.arn
      }
      bucket_key_enabled = true
    }
  }

  # Lifecycle rules
  lifecycle_rule = [
    {
      id      = "delete-old-versions"
      enabled = true

      noncurrent_version_expiration = {
        days = 90
      }
    }
  ]

  # Enable logging
  logging = {
    target_bucket = module.terraform_state_bucket.s3_bucket_id
    target_prefix = "access-logs/"
  }

  # Force SSL/TLS
  attach_policy = true
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "DenyInsecureTransport"
        Effect = "Deny"
        Principal = "*"
        Action = "s3:*"
        Resource = [
          "arn:aws:s3:::${var.bucket_name}",
          "arn:aws:s3:::${var.bucket_name}/*"
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      }
    ]
  })

  tags = {
    Name        = var.bucket_name
    Purpose     = "Terraform State Storage"
    Encryption  = "KMS"
    Versioning  = "Enabled"
  }
}

# Output the bucket details
output "bucket_name" {
  description = "The name of the S3 bucket"
  value       = module.terraform_state_bucket.s3_bucket_id
}

output "bucket_arn" {
  description = "The ARN of the S3 bucket"
  value       = module.terraform_state_bucket.s3_bucket_arn
}

output "kms_key_id" {
  description = "The ID of the KMS key used for encryption"
  value       = aws_kms_key.terraform_state.id
}

output "kms_key_arn" {
  description = "The ARN of the KMS key used for encryption"
  value       = aws_kms_key.terraform_state.arn
}