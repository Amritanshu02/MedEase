# Use official OpenJDK 21 slim image as base
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the packaged jar (adjust if your build outputs elsewhere)
COPY target/medlocal-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on (default 8080, but can be overridden)
EXPOSE 8080

# Set environment variables for Spring profiles (can be overridden at runtime)
ENV SPRING_PROFILES_ACTIVE=prod

# Run the jar
ENTRYPOINT ["java","-jar","/app/app.jar"]