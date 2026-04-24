# Multi-stage build for Identity Service
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY src/ ./src
COPY .mvn/ ./.mvn
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_FILE=target/banco-digital-identity-0.0.1-SNAPSHOT.jar
COPY --from=builder /build/${JAR_FILE} application.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "application.jar"]
