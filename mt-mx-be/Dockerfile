# Multi-stage build for Spring Boot application
FROM maven:3.8-openjdk-11-slim as builder

# Set working directory
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy the jar file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
