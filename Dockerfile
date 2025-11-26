# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first for caching
COPY pom.xml .

# Download dependencies (캐싱 활용)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn package -DskipTests -B

# Production stage
FROM eclipse-temurin:21-jre-alpine AS production

WORKDIR /app

# Install required tools for health check
RUN apk add --no-cache curl wget

# Copy built jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser && \
    chown -R appuser:appgroup /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
