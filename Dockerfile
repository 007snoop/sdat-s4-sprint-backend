# --- Stage 1: Build the app using Maven ---
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven wrapper, pom.xml, and sources
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src ./src

# Build the project (skip tests for faster CI/CD)
RUN ./mvnw clean package -DskipTests

# --- Stage 2: Run the app with minimal image ---
FROM eclipse-temurin:17-jdk-alpine

# Install CA certificates so AWS SDK can make HTTPS calls
RUN apk add --no-cache ca-certificates && update-ca-certificates

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Default environment variables (can be overridden at runtime)
ENV AWS_REGION=us-east-2
ENV useAwsSecrets=true

# Expose the app port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
