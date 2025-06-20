# --- Stage 1: Build the app using Maven ---
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven wrapper, pom.xml, and sources
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src ./src

# Build the project
RUN ./mvnw clean package -DskipTests

# --- Stage 2: Run the app with minimal image ---
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar

# Define the entrypoint
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080
