# Core outputs - these are now maps with repo keys
output "project_names" {
  description = "CodeBuild project names (map: repo-key => project-name)"
  value       = module.github_runner.project_names
}

output "project_arns" {
  description = "CodeBuild project ARNs (map: repo-key => project-arn)"
  value       = module.github_runner.project_arns
}

output "runner_labels" {
  description = "Runner labels for GitHub Actions workflows (map: repo-key => runner-label)"
  value       = module.github_runner.runner_labels
}

# Monitoring and debugging
output "log_groups" {
  description = "CloudWatch log groups (map: repo-key => log-group-name)"
  value       = module.github_runner.log_groups
}

output "cache_bucket" {
  description = "S3 cache bucket name (if enabled)"
  value       = module.github_runner.cache_bucket
}

# Security
output "service_role_arn" {
  description = "CodeBuild service role ARN"
  value       = module.github_runner.service_role_arn
}

output "codebuild_security_group_id" {
  description = "CodeBuild project security group ID"
  value       = module.github_runner.codebuild_security_group_id
}

# GitHub integration
output "github_repository_urls" {
  description = "GitHub repository URLs (map: repo-key => repo-url)"
  value       = module.github_runner.github_repository_urls
}

output "auth_method" {
  description = "Authentication method in use"
  value       = module.github_runner.auth_method
}

output "github_connection_arn" {
  description = "GitHub App CodeConnections ARN (if using github_app auth)"
  value       = module.github_runner.github_connection_arn
  sensitive   = true
}

output "github_connection_status" {
  description = "CodeConnections connection status"
  value       = module.github_runner.github_connection_status
}

# Webhooks
output "webhooks_created" {
  description = "Webhooks created status (map: repo-key => webhook-created)"
  value       = module.github_runner.webhooks_created
}

# Docker
output "docker_modes" {
  description = "Docker mode per repo (map: repo-key => docker-mode)"
  value       = module.github_runner.docker_modes
}

output "docker_server_fleet_arn" {
  description = "Docker Server fleet ARN (if enabled)"
  value       = module.github_runner.docker_server_fleet_arn
}

# Setup instructions
output "setup_complete" {
  description = "Setup status and next steps"
  value       = module.github_runner.setup_complete
}

output "usage_instructions" {
  description = "How to use this runner in GitHub Actions"
  value       = module.github_runner.usage_instructions
}
