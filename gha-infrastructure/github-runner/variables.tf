variable "aws_region" {
  description = "AWS region to deploy the runner"
  type        = string
  default     = "us-east-1"
}

variable "github_owner" {
  description = "GitHub organization or username"
  type        = string
}

variable "repositories" {
  description = "Map of repositories to create runners for"
  type = map(object({
    github_repository      = string
    project_name           = string
    compute_type           = optional(string, "BUILD_GENERAL1_MEDIUM")
    concurrent_build_limit = optional(number, 20)
    skip_webhook_creation  = optional(bool, true)
    enable_docker_server   = optional(bool, false)
  }))
  default = {}
}

variable "auth_method" {
  description = "Authentication method: 'pat' for Personal Access Token or 'github_app' for GitHub App"
  type        = string
  default     = "github_app"

  validation {
    condition     = contains(["pat", "github_app"], var.auth_method)
    error_message = "Authentication method must be either 'pat' or 'github_app'."
  }
}

variable "github_secret_name" {
  description = "AWS Secrets Manager secret name containing GitHub PAT (only for pat auth_method)"
  type        = string
  default     = ""
}

variable "github_connection_arn" {
  description = "Existing AWS CodeConnections connection ARN (for github_app auth_method)"
  type        = string
  default     = ""
}

variable "github_connection_name" {
  description = "Name for new AWS CodeConnections connection (for github_app auth_method)"
  type        = string
  default     = ""
}

variable "skip_webhook_creation" {
  description = "Skip webhook creation. Set to false after setting up authentication."
  type        = bool
  default     = true
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "enable_vpc" {
  description = "Whether to deploy runner in VPC"
  type        = bool
  default     = true
}

variable "vpc_config" {
  description = "VPC configuration (required if enable_vpc is true)"
  type = object({
    vpc_id             = string
    subnet_ids         = list(string)
    security_group_ids = list(string)
  })
  default = null
}

variable "managed_security_groups" {
  description = "Create and use managed security groups for the CodeBuild project"
  type        = bool
  default     = true
}

variable "compute_type" {
  description = "CodeBuild compute type"
  type        = string
  default     = "BUILD_GENERAL1_MEDIUM"

  validation {
    condition = contains([
      "BUILD_GENERAL1_SMALL",
      "BUILD_GENERAL1_MEDIUM",
      "BUILD_GENERAL1_LARGE",
      "BUILD_GENERAL1_XLARGE",
      "BUILD_GENERAL1_2XLARGE"
    ], var.compute_type)
    error_message = "Invalid compute type specified."
  }
}

variable "build_image" {
  description = "Docker image for CodeBuild environment (Linux only)"
  type        = string
  default     = "aws/codebuild/standard:7.0"
}

variable "concurrent_build_limit" {
  description = "Maximum number of concurrent builds"
  type        = number
  default     = 20

  validation {
    condition     = var.concurrent_build_limit >= 1 && var.concurrent_build_limit <= 100
    error_message = "Concurrent build limit must be between 1 and 100."
  }
}

variable "cache_type" {
  description = "Type of cache to use (S3, LOCAL, or NO_CACHE)"
  type        = string
  default     = "S3"

  validation {
    condition     = contains(["S3", "LOCAL", "NO_CACHE"], var.cache_type)
    error_message = "Cache type must be S3, LOCAL, or NO_CACHE."
  }
}

variable "enable_docker" {
  description = "Enable Docker in Docker (privileged mode)"
  type        = bool
  default     = true
}

variable "log_retention_days" {
  description = "CloudWatch log retention in days"
  type        = number
  default     = 7

  validation {
    condition     = contains([1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653], var.log_retention_days)
    error_message = "Log retention days must be a valid CloudWatch value."
  }
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default = {
    ManagedBy = "Terraform"
  }
}