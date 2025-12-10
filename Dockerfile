# Step 1: Base image
FROM eclipse-temurin:17-jdk-jammy

# Step 2: Set working directory
WORKDIR /app

# Step 3: Copy jar and external config
COPY target/*.jar app.jar
COPY application.properties application.properties

# Step 4: Expose Spring Boot port
EXPOSE 8080

# Step 5: Run Spring Boot with external config file
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.config.location=file:/app/application.properties"]