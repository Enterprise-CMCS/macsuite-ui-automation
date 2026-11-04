variable "aws_region" {
  description = "AWS region for the backend resources"
  type        = string
  default     = "us-east-1"
}

variable "bucket_name" {
  description = "Name of the S3 bucket for Terraform state"
  type        = string
  default     = "macpro-macsuite-terraform-state"
}