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

RUN apk add --no-cache ca-certificates curl && update-ca-certificates
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

# Keep sane JVM defaults
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Defaults (override at runtime in CI/CD)
ENV AWS_REGION=us-east-2
ENV useAwsSecrets=true

# IMPORTANT: context path is /api → update health URL
HEALTHCHECK --interval=10s --timeout=3s --retries=10 --start-period=60s \
  CMD curl -fsS http://localhost:8080/api/actuator/health/liveness || exit 1

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
