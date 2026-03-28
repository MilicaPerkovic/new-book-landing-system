# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

# Copy pom.xml and download dependencies
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build Quarkus uber-jar (skipping tests as they'll run in CI)
RUN mvn clean package -DskipTests -Dquarkus.package.type=uber-jar

# Stage 2: Runtime with Distroless
FROM quay.io/quarkus/quarkus-distroless-image:2.0

# Copy compiled jar from builder
COPY --from=builder /workspace/target/order-service-*-runner.jar /deployments/quarkus-run.jar

# Environment variables for Quarkus
ENV LANG='en_US.UTF-8' \
    LANGUAGE='en_US:en' \
    QUARKUS_HTTP_PORT=8080 \
    QUARKUS_LOG_LEVEL=INFO

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/q/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
