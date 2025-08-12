# =========================
# Stage 1: Build with Maven
# =========================
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only what's needed to resolve dependencies first (better cache)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

# Now copy sources and build
COPY src ./src
RUN ./mvnw -q clean package -DskipTests

# =========================
# Stage 2: Runtime (slim JRE)
# =========================
FROM eclipse-temurin:17-jre-alpine

# Install CA certs + curl for health checks
RUN apk add --no-cache ca-certificates curl && update-ca-certificates

# Create non-root user
RUN addgroup -S app && adduser -S app -G app
USER app

WORKDIR /app

# Copy the fat jar from the build stage
# (Assumes there's only one jar in target/)
COPY --from=build /workspace/target/*.jar /app/app.jar

# Sensible JVM defaults for containers
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Default envs (override at runtime)
ENV AWS_REGION=us-east-2
ENV useAwsSecrets=true

# Healthcheck (requires spring-boot-starter-actuator exposing /actuator/health)
HEALTHCHECK --interval=10s --timeout=3s --retries=6 --start-period=10s \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
