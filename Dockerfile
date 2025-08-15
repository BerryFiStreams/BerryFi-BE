# Multi-stage build for Spring Boot application
# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build, you can remove -DskipTests if needed)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks, ca-certificates for SSL/TLS, and openssl for certificate management
RUN apk add --no-cache \
    curl \
    ca-certificates \
    openssl \
    tzdata \
    && update-ca-certificates

# Update certificate store and add common SMTP certificates
RUN wget -O /tmp/ca-certificates.crt https://curl.se/ca/cacert.pem \
    && cp /tmp/ca-certificates.crt /etc/ssl/certs/ \
    && update-ca-certificates \
    && rm /tmp/ca-certificates.crt

# Set timezone (optional but recommended for email timestamps)
ENV TZ=UTC

# Set working directory
WORKDIR /app

# Import updated certificates into Java keystore for SMTP connections
RUN keytool -importcert -alias cacert -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file /etc/ssl/certs/ca-certificates.crt -noprompt || true

# Create a non-root user for security
RUN addgroup -g 1000 spring && adduser -u 1000 -G spring -s /bin/sh -D spring

# Copy the built jar from the builder stage
COPY --from=builder /app/target/portal-0.0.1-SNAPSHOT.jar portal.jar

# Create logs directory and set permissions
RUN mkdir -p /app/logs && chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose the port the app runs on
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment and SSL/TLS with memory optimization
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0 -XX:InitialRAMPercentage=50.0 -Xms256m -Xmx512m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom -Djavax.net.ssl.trustStore=/opt/java/openjdk/lib/security/cacerts -Djavax.net.ssl.trustStorePassword=changeit -Dmail.smtp.ssl.protocols=TLSv1.2"

# Set Spring profile to prod for production deployment
ENV SPRING_PROFILES_ACTIVE=prod
ENV RAILWAY_ENVIRONMENT=prod

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar portal.jar"]
