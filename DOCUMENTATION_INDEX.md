# 📚 HRM Backend Documentation Index

## 🎯 Getting Started

### 👉 **Start Here** 
1. **[README_PRODUCTION.md](./README_PRODUCTION.md)** - Overview of all production features
2. **[QUICKSTART.md](./QUICKSTART.md)** - Get up and running in 5 minutes

---

## 📖 Comprehensive Guides

### 🚀 Deployment & Configuration
| Document | Purpose | When to Use |
|----------|---------|-------------|
| [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md) | Complete AWS setup (S3, RDS, ECS) | Setting up for AWS production |
| [ENVIRONMENT_CONFIGURATION.md](./ENVIRONMENT_CONFIGURATION.md) | Environment-specific configs | Configuring for dev/staging/prod |
| [PRODUCTION_MIGRATION_SUMMARY.md](./PRODUCTION_MIGRATION_SUMMARY.md) | Detailed technical changes made | Understanding what changed |

### 🔧 Technical Details
| Document | Purpose | When to Use |
|----------|---------|-------------|
| `.env.example` | Environment variable template | Setting up new environment |
| `Dockerfile` | Container image definition | Understanding containerization |
| `docker-compose.yml` | Multi-container orchestration | Running locally or in swarm |

---

## 📋 Feature Documentation

### File Upload & Storage
- **Profile Images**: max 5MB (JPEG/PNG)
  - Endpoint: `POST /api/employees/{id}/avatar`
  - Storage: AWS S3 or local
  - Response: Profile with avatar URL

- **Payslips**: auto-generated PDFs
  - Max 20MB
  - S3 storage with presigned URLs
  - Automatic on payroll generation

- **Documents**: max 10MB (PDF/Word)
  - Endpoint: `POST /api/files/upload`
  - S3 or local storage

### Database Support
```
Development: Local MySQL (localhost:3306)
Production: AWS RDS MySQL (regional endpoint)
```

### Authentication
```
Method: JWT (JSON Web Tokens)
Expiration: Configurable (default: 24 hours)
Secure: CORS-protected, HTTPS ready
```

---

## 🔐 Security Features

✅ File type validation
✅ File size limits enforced
✅ JWT authentication required
✅ Role-based access control
✅ CORS properly configured
✅ Environment-based secrets
✅ IAM role support for AWS
✅ Non-root Docker execution
✅ Comprehensive error handling

---

## 🗂️ Project Structure

```
hrm_backend/
├── 📄 README.md (original project)
├── 📄 README_PRODUCTION.md (overview)
├── 📄 QUICKSTART.md (5-min setup)
├── 📄 AWS_DEPLOYMENT_GUIDE.md (AWS setup)
├── 📄 ENVIRONMENT_CONFIGURATION.md (config templates)
├── 📄 PRODUCTION_MIGRATION_SUMMARY.md (technical changes)
│
├── src/main/
│   ├── java/com/hrms/hrm/
│   │   ├── config/
│   │   │   ├── AwsS3Config.java ✨ NEW
│   │   │   ├── WebConfig.java ✏️ UPDATED
│   │   │   └── MonthlyPayslipPdfGenerator.java ✏️ UPDATED
│   │   ├── security/
│   │   │   └── SecurityConfig.java ✏️ UPDATED
│   │   ├── service/impl/
│   │   │   ├── S3FileStorageService.java ✨ NEW
│   │   │   └── EmployeeServiceImpl.java ✏️ UPDATED
│   │   └── payroll/
│   │       ├── model/PaySlip.java ✏️ UPDATED
│   │       └── config/... ✏️ UPDATED
│   └── resources/
│       └── application.properties ✏️ UPDATED
│
├── Dockerfile ✏️ UPDATED (multi-stage)
├── docker-compose.yml ✨ NEW
├── .dockerignore ✨ NEW
├── .env.example ✨ NEW
├── pom.xml ✏️ UPDATED
└── uploads/ (local storage)
```

---

## 🚀 Quick Reference

### Local Development
```bash
cp .env.example .env          # Use local values
docker-compose up -d          # Start all services
docker-compose logs -f        # View logs
```

### AWS Production
```bash
# 1. Create S3 bucket
aws s3 mb s3://hrms-employee-docs

# 2. Create RDS instance
# See AWS_DEPLOYMENT_GUIDE.md

# 3. Build & push image
docker build -t hrm-backend:latest .
# Push to ECR...

# 4. Deploy
docker-compose up -d  # or ECS
```

---

## 📊 Configuration Options

### File Storage
```properties
# Development (Local)
file.storage=local

# Production (AWS S3)
file.storage=s3
aws.region=ap-south-1
aws.s3.bucket=hrms-employee-docs
```

### Database
```properties
# Development
spring.datasource.url=jdbc:mysql://localhost:3306/hrm

# Production (RDS)
spring.datasource.url=jdbc:mysql://rds-endpoint:3306/hrm
```

---

## ✅ Production Checklist

- [ ] Read [README_PRODUCTION.md](./README_PRODUCTION.md)
- [ ] Follow [QUICKSTART.md](./QUICKSTART.md) to test locally
- [ ] Use [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md) for AWS setup
- [ ] Review [ENVIRONMENT_CONFIGURATION.md](./ENVIRONMENT_CONFIGURATION.md)
- [ ] Create `.env` with production values
- [ ] Test file uploads locally
- [ ] Deploy to AWS following guide
- [ ] Monitor with CloudWatch
- [ ] Set up backups and scaling

---

## 🧪 Testing

### Profile Image Upload
```bash
curl -X POST http://localhost:8080/api/employees/{id}/avatar \
  -H "Authorization: Bearer {token}" \
  -F "file=@image.jpg"
```

### Payslip Generation
```bash
curl -X POST http://localhost:8080/api/payroll/generate \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"employeeId":"uuid","month":1,"year":2024}'
```

---

## 📞 Support Resources

### Official Documentation
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose documentation](https://docs.docker.com/compose/)

### Troubleshooting
- Check [QUICKSTART.md](./QUICKSTART.md) troubleshooting section
- See [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md) troubleshooting section
- Review application logs: `docker-compose logs -f`

---

## 🎓 Key Learning Points

### Architecture
```
Frontend (React) → Backend (Spring Boot) → Database (MySQL)
                         ↓
                   AWS S3 Storage
```

### File Flow
```
1. User uploads file
2. Validation (size, type)
3. Unique name generation
4. S3 upload (or local save)
5. Database record update
6. Presigned URL generation
7. Response to client
```

### Security Flow
```
1. JWT authentication required
2. File type validation
3. Size limit enforcement
4. Secure URL generation
5. Presigned URL expiry (15 min)
6. Audit logging
```

---

## 📈 Performance Tips

1. **Database**
   - Use connection pooling (configured)
   - Enable query caching
   - Index frequently accessed columns

2. **S3 Storage**
   - Use presigned URLs (15 min valid)
   - Enable S3 versioning
   - Configure lifecycle policies

3. **Container**
   - Use G1GC for memory management
   - Set proper memory limits
   - Health checks configured

4. **Frontend**
   - Use CloudFront for CDN
   - Compress assets
   - Cache static files

---

## 🔄 Deployment Workflow

```
Development
    ↓
  [Local Docker Compose]
    ↓
Staging
    ↓
  [AWS staging environment]
    ↓
Production
    ↓
  [AWS production environment]
```

---

## 📝 Document Legend

| Icon | Meaning |
|------|---------|
| ✨ NEW | Newly created file |
| ✏️ UPDATED | Modified file |
| 📄 | Documentation |
| 🐳 | Docker-related |
| ☁️ | AWS-related |
| 🔒 | Security-related |

---

## 🎯 Next Steps

1. **Read** [README_PRODUCTION.md](./README_PRODUCTION.md) for overview
2. **Follow** [QUICKSTART.md](./QUICKSTART.md) to test locally
3. **Setup** AWS resources using [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md)
4. **Configure** environment using [ENVIRONMENT_CONFIGURATION.md](./ENVIRONMENT_CONFIGURATION.md)
5. **Deploy** following the deployment guide
6. **Monitor** with CloudWatch and logs
7. **Scale** as needed using auto-scaling

---

## 📞 Support

For issues or questions:
1. Check relevant documentation
2. Review application logs
3. Check AWS CloudWatch logs
4. Verify configuration files
5. Test with simple curl commands

---

**Status**: ✅ Production Ready
**Version**: 1.0
**Last Updated**: February 2024

**Quick Links**:
- [Production Overview](./README_PRODUCTION.md)
- [Quick Start](./QUICKSTART.md)
- [AWS Deployment](./AWS_DEPLOYMENT_GUIDE.md)
- [Environment Config](./ENVIRONMENT_CONFIGURATION.md)
