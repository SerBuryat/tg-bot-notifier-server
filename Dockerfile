# Stage 1: Build the application
FROM gradle:7.4.2-jdk17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle project files
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

# Build the application (outputs a JAR file to /app/build/libs)
RUN gradle clean shadowJar -x test

# Stage 2: Create a lightweight runtime image
FROM openjdk:17-jdk-slim

# Set working directory for the app
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/*.jar tg-bot-notifier-server.jar

# Expose the port (optional if dynamic ports are used)
# EXPOSE 8080

# Set the default command to run the JAR file
CMD ["java", "-jar", "/app/tg-bot-notifier-server.jar"]