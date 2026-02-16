# HRM Backend - AWS Production Deployment Guide

## Overview

This HRM Backend is now configured for production deployment on AWS with:
- **AWS S3** integration for file storage (profile images, payslips, documents)
- **RDS MySQL** database support
- **Docker** containerization with multi-stage builds
- **Environment-based** configuration
- **Production-ready** security and CORS settings

## Key Features

### 1. AWS S3 Integration
- **Profile Images**: Max 5MB (JPEG/PNG)
- **Documents**: Max 10MB (PDF, Word)
- **Payslips**: Max 20MB (PDF)
- Automatic presigned URL generation for secure file access (15 minutes)
- Fallback to local storage if S3 is unavailable

### 2. File Storage Configuration
```properties
file.storage=s3                    # Use AWS S3
aws.region=ap-south-1
aws.s3.bucket=hrms-employee-docs
aws.s3.access-key=${AWS_ACCESS_KEY_ID}
aws.s3.secret-key=${AWS_SECRET_ACCESS_KEY}
```

### 3. Environment Variables

#### Database
```bash
DB_URL=jdbc:mysql://rds-endpoint:3306/hrm
DB_USERNAME=admin
DB_PASSWORD=your-secure-password
```

#### AWS S3
```bash
FILE_STORAGE=s3
AWS_REGION=ap-south-1
AWS_S3_BUCKET=hrms-employee-docs
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```

#### Security
```bash
JWT_SECRET=your-long-secure-secret
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

## Deployment Steps

### 1. Prerequisites
- AWS Account with S3 and RDS access
- Docker & Docker Compose installed
- AWS CLI configured with credentials

### 2. Create AWS Resources

#### Create S3 Bucket
```bash
aws s3 mb s3://hrms-employee-docs --region ap-south-1
```

#### Configure Bucket Policy
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::YOUR_ACCOUNT_ID:role/ECSTaskRole"
      },
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::hrms-employee-docs",
        "arn:aws:s3:::hrms-employee-docs/*"
      ]
    }
  ]
}
```

#### Create RDS MySQL Instance
```bash
aws rds create-db-instance \
  --db-instance-identifier hrm-database \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0.33 \
  --master-username admin \
  --master-user-password 'your-secure-password' \
  --allocated-storage 20 \
  --region ap-south-1
```

### 3. Build and Push Docker Image

#### Build Docker Image
```bash
docker build -t hrm-backend:latest .
```

#### Tag for ECR
```bash
aws ecr create-repository --repository-name hrm-backend --region ap-south-1

docker tag hrm-backend:latest your-account-id.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:latest

aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin your-account-id.dkr.ecr.ap-south-1.amazonaws.com

docker push your-account-id.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:latest
```

### 4. Deploy using Docker Compose

#### Create .env file
```bash
cp .env.example .env
# Edit .env with your values
nano .env
```

#### Start Services
```bash
docker-compose up -d
```

#### Verify Deployment
```bash
docker-compose logs -f hrm-backend
```

### 5. Deploy to AWS ECS

#### Create Task Definition (if using ECS)

```json
{
  "family": "hrm-backend",
  "containerDefinitions": [
    {
      "name": "hrm-backend",
      "image": "your-account-id.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "hostPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "FILE_STORAGE",
          "value": "s3"
        },
        {
          "name": "AWS_REGION",
          "value": "ap-south-1"
        },
        {
          "name": "AWS_S3_BUCKET",
          "value": "hrms-employee-docs"
        },
        {
          "name": "DB_URL",
          "value": "jdbc:mysql://your-rds-endpoint:3306/hrm"
        }
      ],
      "secrets": [
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:ap-south-1:account-id:secret:hrm/jwt-secret"
        },
        {
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:ap-south-1:account-id:secret:hrm/db-password"
        },
        {
          "name": "AWS_ACCESS_KEY_ID",
          "valueFrom": "arn:aws:secretsmanager:ap-south-1:account-id:secret:hrm/aws-access-key"
        },
        {
          "name": "AWS_SECRET_ACCESS_KEY",
          "valueFrom": "arn:aws:secretsmanager:ap-south-1:account-id:secret:hrm/aws-secret-key"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/hrm-backend",
          "awslogs-region": "ap-south-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ],
  "taskRoleArn": "arn:aws:iam::account-id:role/ECSTaskRole",
  "executionRoleArn": "arn:aws:iam::account-id:role/ecsTaskExecutionRole"
}
```

## Profile Image Upload

### Upload Profile Image
```bash
curl -X POST http://localhost:8080/api/employees/{employeeId}/avatar \
  -H "Authorization: Bearer {jwt-token}" \
  -F "file=@/path/to/profile.jpg"
```

### Response
```json
{
  "status": "success",
  "data": {
    "id": "employee-uuid",
    "avatar": "profile-images/uuid.jpg",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

## Payslip Generation

### Generate Payslip
```bash
curl -X POST http://localhost:8080/api/payroll/generate \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "uuid",
    "month": 1,
    "year": 2024
  }'
```

### Features
- Auto-generates PDF payslips
- Stores in S3 with automatic file naming
- Presigned URLs for secure access
- Falls back to local storage if S3 unavailable

## Security Best Practices

### 1. Use AWS IAM Roles
Instead of hardcoding credentials, use IAM role for ECS/EC2:
```bash
# In ECS task role, attach policy instead of using access keys
```

### 2. Use AWS Secrets Manager
Store sensitive credentials in Secrets Manager:
```bash
aws secretsmanager create-secret \
  --name hrm/jwt-secret \
  --secret-string "your-jwt-secret"
```

### 3. CORS Configuration
Update CORS origins for your domain:
```properties
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

### 4. HTTPS/TLS
Use AWS ALB with SSL certificate:
```bash
# Configure in Launch Configuration/Template
```

## Monitoring & Logging

### CloudWatch Logs
Check logs in CloudWatch:
```bash
aws logs tail /ecs/hrm-backend --follow
```

### Health Check
```bash
curl http://localhost:8080/api/health
```

## Troubleshooting

### S3 Upload Fails
1. Check IAM role permissions
2. Verify bucket exists and is accessible
3. Check file size limits
4. Review CloudWatch logs

### Database Connection Issues
1. Verify RDS security group allows access
2. Check DB credentials
3. Ensure network connectivity
4. Review RDS parameter group

### File Size Errors
- Profile images: Max 5MB
- Documents: Max 10MB
- Payslips: Max 20MB

Update in `application.properties` if needed

## Local Development

### Using Local Storage
```properties
file.storage=local
file.upload.dir=uploads/
```

### Docker Compose
```bash
docker-compose up -d
docker-compose logs -f
```

## Performance Optimization

### JVM Configuration
```bash
JAVA_OPTS=-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0
```

### Database Connection Pool
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
```

### S3 Configuration
- Enable presigned URLs for efficient access
- Use regional endpoints
- Enable S3 encryption at rest

## Scaling Considerations

1. **Horizontal Scaling**: Use AWS ALB + ECS/Fargate
2. **Database Scaling**: Use RDS read replicas
3. **File Storage**: S3 auto-scales
4. **Caching**: Implement Redis for session cache
5. **CDN**: Use CloudFront for file distribution

## Support & Documentation

- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [Spring AWS Integration](https://spring.io/projects/spring-cloud-aws)
- [Docker Official Documentation](https://docs.docker.com/)

---

**Version**: 1.0
**Last Updated**: February 2024
