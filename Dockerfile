# ============================================================================
# STAGE 1: Build stage
# ============================================================================
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY .mvn .mvn
COPY pom.xml pom.xml

# Download dependencies (creates layer for caching)
RUN ./mvnw dependency:resolve

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# ============================================================================
# STAGE 2: Runtime stage
# ============================================================================
FROM eclipse-temurin:17-jre-jammy

# Create app user for security (non-root execution)
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Create directories for file uploads (fallback for local storage)
RUN mkdir -p /app/uploads/payslips && \
    mkdir -p /app/uploads/profile-images && \
    mkdir -p /app/uploads/documents && \
    chown -R appuser:appuser /app

# Set environment variables for production
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0" \
    FILE_STORAGE=s3 \
    TZ=Asia/Kolkata

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp app.jar com.hrms.hrm.HrmApplication --health || exit 1

# Switch to non-root user
USER appuser

# Run the application with environment variables support
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
