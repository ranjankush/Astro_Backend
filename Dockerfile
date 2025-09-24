#FROM maven:3.9.6-eclipse-temurin-21 AS build
#WORKDIR /app
#COPY . .
#RUN mvn clean package -DskipTests
#
## Stage 2: Run the Spring Boot app with JDK 21
#FROM eclipse-temurin:21-jdk-jammy
#WORKDIR /app
#COPY --from=build /app/target/AiAstrologer-0.0.1-SNAPSHOT.jar app.jar
#EXPOSE 8088
#ENTRYPOINT ["java", "-jar", "app.jar"]


# ==============================
# Stage 1: Build the Spring Boot app
# ==============================
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy only pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .

# Download dependencies only (offline mode)
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the JAR (skip tests to speed up)
RUN mvn clean package -DskipTests

# ==============================
# Stage 2: Run the Spring Boot app
# ==============================
FROM eclipse-temurin:21-jre-jammy

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/AiAstrologer-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Render will provide PORT env)
EXPOSE 8080

# Run the app using dynamic PORT for Render
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8088}"]
