# Build stage
FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /workspace/app

COPY pom.xml .
RUN mvn dependency:resolve

COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl

ARG SERVICE_NAME=banco-digital-identity
ARG JAR_FILE=target/banco-digital-identity-0.0.1-SNAPSHOT.jar

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR from builder
COPY --from=builder /workspace/app/${JAR_FILE} app.jar

RUN chown appuser:appgroup app.jar

EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

USER appuser

ENTRYPOINT [ "sh", "-c", "java -XX:+UnlockExperimentalVMOptions -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dspring.profiles.active=${APP_PROFILE} -jar app.jar" ]
