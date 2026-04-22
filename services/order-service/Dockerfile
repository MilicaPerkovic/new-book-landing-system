# Use pre-built JAR to avoid gRPC protoc compilation issues in Docker
FROM eclipse-temurin:21-jre-alpine

# Copy pre-built jar from local build
# This assumes the jar has been built locally with: mvn clean package -DskipTests -Dquarkus.package.type=uber-jar
COPY target/*runner.jar /deployments/quarkus-run.jar

# Environment variables for Quarkus
ENV LANG='en_US.UTF-8' \
    LANGUAGE='en_US:en' \
    QUARKUS_HTTP_PORT=8080 \
    QUARKUS_LOG_LEVEL=INFO

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost:8080/q/health >/dev/null || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
