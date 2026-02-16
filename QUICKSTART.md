# 🚀 Quick Start Guide - HRM Backend (Production Ready)

## 5-Minute Setup

### Option 1: Local Development (Using Local Storage)

```bash
# 1. Copy environment variables
cp .env.example .env

# 2. Edit for local development
cat > .env << EOF
DB_URL=jdbc:mysql://localhost:3306/hrm
DB_USERNAME=root
DB_PASSWORD=password
FILE_STORAGE=local
JWT_SECRET=your-dev-secret-key
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
EOF

# 3. Start containers
docker-compose up -d

# 4. Wait for services to be ready
sleep 10

# 5. Check status
docker-compose ps
docker-compose logs -f hrm-backend
```

### Option 2: AWS Production (Using S3)

```bash
# 1. Set up AWS credentials
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
export AWS_REGION=ap-south-1

# 2. Create S3 bucket
aws s3 mb s3://hrms-employee-docs --region ap-south-1

# 3. Create environment file
cp .env.example .env

# 4. Edit with AWS values
cat > .env << EOF
DB_URL=jdbc:mysql://your-rds-endpoint:3306/hrm
DB_USERNAME=admin
DB_PASSWORD=your-secure-password
FILE_STORAGE=s3
AWS_REGION=ap-south-1
AWS_S3_BUCKET=hrms-employee-docs
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
JWT_SECRET=your-production-secret-key
CORS_ALLOWED_ORIGINS=https://yourdomain.com
EOF

# 5. Build Docker image
docker build -t hrm-backend:latest .

# 6. Push to ECR (optional)
aws ecr create-repository --repository-name hrm-backend --region ap-south-1
docker tag hrm-backend:latest your-account.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:latest
aws ecr get-login-password | docker login --username AWS --password-stdin your-account.dkr.ecr.ap-south-1.amazonaws.com
docker push your-account.dkr.ecr.ap-south-1.amazonaws.com/hrm-backend:latest

# 7. Deploy with docker-compose
docker-compose up -d
```

---

## 📌 Configuration Reference

### File Storage Options

#### Local Storage (Development)
```env
FILE_STORAGE=local
MAX_FILE_SIZE=20MB
MAX_REQUEST_SIZE=20MB
```
Files stored in: `./uploads/`

#### AWS S3 (Production)
```env
FILE_STORAGE=s3
AWS_REGION=ap-south-1
AWS_S3_BUCKET=hrms-employee-docs
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```

### Database Configuration

#### Local MySQL
```env
DB_URL=jdbc:mysql://localhost:3306/hrm
DB_USERNAME=root
DB_PASSWORD=password
```

#### AWS RDS
```env
DB_URL=jdbc:mysql://hrm-database.region.rds.amazonaws.com:3306/hrm
DB_USERNAME=admin
DB_PASSWORD=your-secure-password
```

### Security Keys
```env
JWT_SECRET=your-very-long-and-secure-secret-key-change-this
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

---

## 🧪 Testing

### Test Health Check
```bash
curl http://localhost:8080/api/health
```

### Test Profile Image Upload
```bash
# 1. Login to get JWT token
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.data.token')

# 2. Upload profile image (max 5MB)
curl -X POST http://localhost:8080/api/employees/{employee-id}/avatar \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/image.jpg"
```

### Test Payslip Generation
```bash
curl -X POST http://localhost:8080/api/payroll/generate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "employee-uuid",
    "month": 1,
    "year": 2024
  }'
```

### Check S3 Files (if using S3)
```bash
# List all uploaded files
aws s3 ls s3://hrms-employee-docs/ --recursive

# Download a file
aws s3 cp s3://hrms-employee-docs/profile-images/uuid.jpg ./downloaded.jpg
```

---

## 🔍 Monitoring

### View Logs
```bash
# All services
docker-compose logs -f

# Backend only
docker-compose logs -f hrm-backend

# Database only
docker-compose logs -f mysql
```

### Check Running Containers
```bash
docker-compose ps
```

### Check Resource Usage
```bash
docker stats
```

---

## 🛑 Stopping Services

```bash
# Stop but keep volumes
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove everything (including data!)
docker-compose down -v
```

---

## 📁 File Size Limits

| File Type | Max Size | Allowed Formats |
|-----------|----------|-----------------|
| Profile Images | 5 MB | JPEG, PNG |
| Documents | 10 MB | PDF, DOC, DOCX |
| Payslips | 20 MB | PDF |

---

## 🔐 Default Credentials (Change in Production!)

```bash
# Database
USERNAME: root
PASSWORD: password

# Application (create via signup)
ROLE: ADMIN
```

**⚠️ Change these immediately in production!**

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# 8080 (backend), 3306 (mysql)
# Free up ports or change in docker-compose.yml
docker-compose down
# Change ports in docker-compose.yml
docker-compose up -d
```

### Database Connection Failed
```bash
# Wait for MySQL to start
sleep 30
docker-compose logs mysql

# Verify MySQL is running
docker-compose exec mysql mysql -uroot -ppassword -e "SELECT 1;"
```

### S3 Upload Failed
```bash
# Check credentials
aws sts get-caller-identity

# Check bucket exists
aws s3 ls s3://hrms-employee-docs/

# Check bucket permissions
aws s3api get-bucket-policy --bucket hrms-employee-docs
```

### File Size Error
Check `application.properties`:
```properties
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

---

## 📚 Documentation

- **[Production Migration Summary](./PRODUCTION_MIGRATION_SUMMARY.md)** - Complete changes made
- **[AWS Deployment Guide](./AWS_DEPLOYMENT_GUIDE.md)** - AWS setup instructions
- **[Spring Boot Docs](https://spring.io/projects/spring-boot)** - Framework documentation
- **[AWS S3 Docs](https://docs.aws.amazon.com/s3/)** - S3 reference

---

## 🚀 Next Steps

1. **Update Frontend URLs** - Point to `api.yourdomain.com`
2. **Configure SSL/TLS** - Use AWS ALB with certificates
3. **Set Up Monitoring** - CloudWatch dashboards
4. **Enable Auto-scaling** - ECS auto-scaling groups
5. **Configure Backups** - RDS automated backups
6. **Implement CI/CD** - GitHub Actions / CodePipeline

---

## 💡 Common Commands

```bash
# Build image
docker build -t hrm-backend:latest .

# Run specific service
docker-compose up -d hrm-backend

# Execute command in container
docker-compose exec hrm-backend bash

# View logs with timestamp
docker-compose logs -f --timestamps hrm-backend

# Restart service
docker-compose restart hrm-backend

# Remove everything
docker system prune -a

# Check environment variables
docker-compose config

# Validate docker-compose syntax
docker-compose config --quiet
```

---

## ✅ Production Checklist

- [ ] Change all default passwords
- [ ] Update JWT_SECRET to a long, random string
- [ ] Configure CORS_ALLOWED_ORIGINS for your domain
- [ ] Set up AWS S3 bucket with proper permissions
- [ ] Configure RDS backup and retention policies
- [ ] Enable CloudWatch logs and monitoring
- [ ] Set up SSL/TLS certificates
- [ ] Configure auto-scaling policies
- [ ] Set up DNS records
- [ ] Test all endpoints with production values
- [ ] Set up monitoring and alerting
- [ ] Document infrastructure architecture
- [ ] Create runbook for common issues

---

**Version**: 1.0
**Last Updated**: February 2024

Need help? Check the [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md) for detailed instructions!
