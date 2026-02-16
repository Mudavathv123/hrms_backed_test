# Environment Configuration Templates

## 🛠️ Development Environment

Create `.env.development`:
```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/hrm
DB_USERNAME=root
DB_PASSWORD=dev_password

# Server
SERVER_PORT=8080

# JWT
JWT_SECRET=dev-secret-key-change-in-production
JWT_EXPIRATION=86400000

# File Storage (Local for development)
FILE_STORAGE=local
FILE_UPLOAD_DIR=uploads/

# Logging
LOG_LEVEL=DEBUG

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://localhost:5173

# File Size (MB)
NUMBER_IMAGE_MAX_SIZE=5
FILE_DOCUMENT_MAX_SIZE=10
FILE_PAYSLIP_MAX_SIZE=20

# Java
JAVA_OPTS=-XX:+UseG1GC -XX:MaxRAMPercentage=50.0 -XX:InitialRAMPercentage=25.0
```

## 🧪 Testing Environment

Create `.env.testing`:
```bash
# Database (H2 in-memory for tests)
DB_URL=jdbc:h2:mem:testdb
DB_USERNAME=sa
DB_PASSWORD=

# Server
SERVER_PORT=8081

# JWT
JWT_SECRET=test-secret-key-for-unit-tests
JWT_EXPIRATION=3600000

# File Storage (Local for tests)
FILE_STORAGE=local
FILE_UPLOAD_DIR=/tmp/test-uploads/

# Logging (Less verbose for tests)
LOG_LEVEL=WARN

# CORS (Permissive for tests)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001

# File Size (MB) - Smaller for faster tests
FILE_IMAGE_MAX_SIZE=5
FILE_DOCUMENT_MAX_SIZE=10
FILE_PAYSLIP_MAX_SIZE=20

# Java (Lower memory for test environment)
JAVA_OPTS=-XX:+UseG1GC -XX:MaxRAMPercentage=25.0 -XX:InitialRAMPercentage=10.0
```

## 🚀 Production Environment (AWS)

Create `.env.production`:
```bash
# ===== DATABASE (AWS RDS) =====
DB_URL=jdbc:mysql://hrm-db.c9xyz123.ap-south-1.rds.amazonaws.com:3306/hrm
DB_USERNAME=admin
DB_PASSWORD=your-very-secure-password-here-use-secrets-manager

# Server
SERVER_PORT=8080

# ===== JWT (IMPORTANT: Change these!) =====
JWT_SECRET=change-this-to-a-very-long-random-string-minimum-32-characters-recommended-64-or-more
JWT_EXPIRATION=86400000

# ===== AWS S3 FILE STORAGE =====
FILE_STORAGE=s3
AWS_REGION=ap-south-1
AWS_S3_BUCKET=hrms-employee-docs-production
AWS_ACCESS_KEY_ID=your-iam-user-access-key
AWS_SECRET_ACCESS_KEY=your-iam-user-secret-key

# File Size Limits (MB)
FILE_IMAGE_MAX_SIZE=5
FILE_DOCUMENT_MAX_SIZE=10
FILE_PAYSLIP_MAX_SIZE=20

# ===== LOGGING =====
LOG_LEVEL=INFO

# ===== CORS (Your production domains) =====
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com,https://admin.yourdomain.com

# ===== DATABASE POOL =====
DB_POOL_SIZE=20
DB_CONNECTION_TIMEOUT=30

# ===== JAVA JVM (Production optimized) =====
JAVA_OPTS=-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0 -XX:+ParallelRefProcEnabled

# ===== SECURITY =====
SERVER_SSL_ENABLED=false  # Use ALB/reverse proxy for SSL instead
TZ=Asia/Kolkata

# ===== OPTIONAL: CloudWatch Monitoring =====
# AWS_CLOUDWATCH_ENABLED=true
# AWS_CLOUDWATCH_REGION=ap-south-1
# AWS_CLOUDWATCH_LOG_GROUP=/aws/ecs/hrm-backend
```

## 🌐 Staging Environment (Pre-Production)

Create `.env.staging`:
```bash
# Database (AWS RDS - Staging instance)
DB_URL=jdbc:mysql://hrm-db-staging.c9xyz123.ap-south-1.rds.amazonaws.com:3306/hrm
DB_USERNAME=staging_admin
DB_PASSWORD=staging-password-from-secrets

# Server
SERVER_PORT=8080

# JWT
JWT_SECRET=staging-secret-key-similar-production-but-not-same
JWT_EXPIRATION=86400000

# File Storage (S3 - Staging bucket)
FILE_STORAGE=s3
AWS_REGION=ap-south-1
AWS_S3_BUCKET=hrms-employee-docs-staging
AWS_ACCESS_KEY_ID=staging-iam-access-key
AWS_SECRET_ACCESS_KEY=staging-iam-secret-key

# File Size (MB)
FILE_IMAGE_MAX_SIZE=5
FILE_DOCUMENT_MAX_SIZE=10
FILE_PAYSLIP_MAX_SIZE=20

# Logging
LOG_LEVEL=INFO

# CORS (Staging frontend domains)
CORS_ALLOWED_ORIGINS=https://staging.yourdomain.com,https://admin-staging.yourdomain.com

# Database Pool
DB_POOL_SIZE=15
DB_CONNECTION_TIMEOUT=30

# Java
JAVA_OPTS=-XX:+UseG1GC -XX:MaxRAMPercentage=70.0 -XX:InitialRAMPercentage=25.0

# Timezone
TZ=Asia/Kolkata
```

---

## 📋 Configuration Priority Order

When running the application:
1. **Environment Variables** (highest priority)
2. `.env` file
3. `application.properties` (defaults)

Example:
```bash
# Override with command line
export FILE_STORAGE=s3
docker-compose up

# Or in docker-compose override
environment:
  - FILE_STORAGE=s3
```

---

## 🔐 Secrets Management (AWS)

Instead of hardcoding credentials, use AWS Secrets Manager:

```bash
# Create secret
aws secretsmanager create-secret \
  --name hrm/database/password \
  --secret-string "your-db-password" \
  --region ap-south-1

aws secretsmanager create-secret \
  --name hrm/jwt/secret \
  --secret-string "your-jwt-secret" \
  --region ap-south-1

aws secretsmanager create-secret \
  --name hrm/aws/s3-access-key \
  --secret-string "your-s3-access-key" \
  --region ap-south-1
```

Reference in ECS Task Definition:
```json
{
  "name": "DB_PASSWORD",
  "valueFrom": "arn:aws:secretsmanager:ap-south-1:account-id:secret:hrm/database/password"
}
```

---

## 🔄 Environment Variable Substitution

All properties support variable substitution:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/hrm}
aws.s3.bucket=${AWS_S3_BUCKET:hrms-default}
jwt.secret=${JWT_SECRET:default-secret-key}
```

The format is: `${VARIABLE_NAME:default_value}`

---

## ✅ Validation Checklist

### Development
- [ ] DB_URL points to localhost
- [ ] FILE_STORAGE=local
- [ ] JWT_SECRET is simple (dev only!)
- [ ] CORS includes localhost:3000

### Staging
- [ ] DB_URL points to RDS staging
- [ ] FILE_STORAGE=s3
- [ ] S3 bucket is staging bucket
- [ ] JWT_SECRET is strong (32+ chars)
- [ ] CORS includes staging domain

### Production
- [ ] DB_URL points to RDS production
- [ ] FILE_STORAGE=s3
- [ ] S3 bucket is production bucket
- [ ] JWT_SECRET is very strong (64+ chars)
- [ ] CORS only includes production domain
- [ ] No hardcoded credentials (use IAM/Secrets)
- [ ] AWS access keys are service-specific
- [ ] Logging is set to INFO
- [ ] SSL/TLS is enabled on ALB

---

## 🚀 Deployment Commands

### Local Development
```bash
export $(cat .env.development)
docker-compose up -d
```

### Staging Deployment
```bash
export $(cat .env.staging)
docker build -t hrm-backend:staging .
docker tag hrm-backend:staging your-account.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:staging
docker push your-account.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:staging
# Deploy to ECS with staging task definition
```

### Production Deployment
```bash
export $(cat .env.production)
docker build -t hrm-backend:latest .
docker tag hrm-backend:latest your-account.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:latest
docker push your-account.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:latest
# Deploy to ECS with production task definition
```

---

## 🔍 Verify Configuration

```bash
# Check environment variables are set
printenv | grep -i hrm
printenv | grep DATABASE

# Check docker-compose configuration
docker-compose config | grep -A 50 environment:

# Test database connection
docker-compose exec hrm-backend bash -c "java -jar app.jar --test.datasource"

# Test S3 connectivity
docker-compose exec hrm-backend bash -c "aws s3 ls s3://your-bucket"
```

---

**Note**: Never commit `.env` files to version control. Only commit `.env.example` as a template!
