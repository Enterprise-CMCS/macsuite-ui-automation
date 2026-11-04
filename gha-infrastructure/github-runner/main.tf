terraform {
  required_version = ">= 1.11"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }

  backend "s3" {
    bucket       = "macpro-macsuite-terraform-state"
    key          = "dev/github-runner/terraform.tfstate"
    region       = "us-east-1"
    encrypt      = true
    use_lockfile = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.common_tags
  }
}

# Local variables for consistent tagging
locals {
  common_tags = merge(var.tags, {
    Environment = var.environment
    ManagedBy   = "Terraform"
    Component   = "github-runner"
    Module      = "mac-fc-github-actions-runner-aws"
    Version     = "v7.2.0"
  })
}

# Deploy the CodeBuild GitHub runner module
module "github_runner" {
  source = "github.com/Enterprise-CMCS/mac-fc-github-actions-runner-aws//codebuild?ref=v7.2.0"

  # GitHub Configuration
  github_owner = var.github_owner
  repositories = var.repositories

  # Authentication
  auth_method            = var.auth_method
  github_secret_name     = var.github_secret_name
  github_connection_arn  = var.github_connection_arn
  github_connection_name = var.github_connection_name
  skip_webhook_creation  = var.skip_webhook_creation

  # Environment
  environment = var.environment

  # VPC Configuration
  enable_vpc              = var.enable_vpc
  vpc_config              = var.vpc_config
  managed_security_groups = var.managed_security_groups

  # Performance Configuration (defaults for all repos)
  compute_type           = var.compute_type
  build_image            = var.build_image
  concurrent_build_limit = var.concurrent_build_limit

  # Cache Configuration
  cache_type = var.cache_type

  # Docker Configuration
  enable_docker = var.enable_docker

  # Logging
  log_retention_days = var.log_retention_days

  tags = local.common_tags
}