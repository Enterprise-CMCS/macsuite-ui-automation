# AWS Configuration
aws_region = "us-east-1"

# GitHub Configuration
github_owner = "Enterprise-CMCS"

# Repositories Map - Define runners for each repository
repositories = {
  "macpro-macsuite-integrations" = {
    github_repository      = "macpro-macsuite-integrations"
    project_name           = "macsuite-mcreview"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macsuite-mcreview-mulesoft" = {
    github_repository      = "macsuite-mcreview-mulesoft"
    project_name           = "macsuite-mcreview-bidirection"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macsuite-onemac-smart-integrations" = {
    github_repository      = "macsuite-onemac-smart-integrations"
    project_name           = "macsuite-onemac-smart"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macsuite-smart-sf-sapi" = {
    github_repository      = "macsuite-smart-sf-sapi"
    project_name           = "macsuite-smart-sf-sapi"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macsuite-onemac-smart-papi" = {
    github_repository      = "macsuite-onemac-smart-papi"
    project_name           = "macsuite-onemac-smart-papi"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macsuite-mactrack-mulesoft" = {
    github_repository      = "macsuite-mactrack-mulesoft"
    project_name           = "macsuite-mactrack-mulesoft"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macsuite-sdp-arms-papi" = {
    github_repository      = "macsuite-sdp-arms-papi"
    project_name           = "macsuite-sdp-arms-papi"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macsuite-sdp-sapi" = {
    github_repository      = "macsuite-sdp-sapi"
    project_name           = "macsuite-sdp-sapi"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macpro-smart-sf-sapi" = {
    github_repository      = "macpro-smart-sf-sapi"
    project_name           = "macpro-smart-sf-sapi"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  },
  "macsuite-ui-automation" = {
    github_repository      = "macsuite-ui-automation"
    project_name           = "macsuite-ui-automation"
    compute_type           = "BUILD_GENERAL1_MEDIUM"
    concurrent_build_limit = 10
    skip_webhook_creation  = false # Set to false after authentication is set up
    enable_docker_server   = false
  }
}

# Authentication Configuration
auth_method            = "github_app"
github_connection_arn  = ""                    # Provide existing connection ARN or use github_connection_name
github_connection_name = "macsuite-runner-dev" # Will create new connection (max 32 chars)
#skip_webhook_creation  = false# Set to false after authentication is set up

# Environment
environment = "dev"

# VPC Configuration
enable_vpc = true
vpc_config = {
  vpc_id = "vpc-0556ac8db732cd983" # Update with actual VPC ID
  subnet_ids = [
    "subnet-0be3d34e8f528310f", # Update with actual subnet IDs
    "subnet-00f8cb23f01dd2626"
  ]
  security_group_ids = [] # Leave empty to use managed security groups
}
managed_security_groups = true

# Performance Configuration
compute_type           = "BUILD_GENERAL1_MEDIUM"
build_image            = "aws/codebuild/standard:7.0"
concurrent_build_limit = 20

# Cache and Features
cache_type    = "S3"
enable_docker = true

# Logging
log_retention_days = 7

# Tags
tags = {
  Team       = "DevOps"
  Project    = "GitHub-Runner"
  Owner      = "Enterprise-CMCS"
  Repository = "macsuite-mcreview-mulesoft"
}