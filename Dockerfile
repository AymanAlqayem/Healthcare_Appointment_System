
# ✅ Purpose: Builds an image of your Spring Boot backend
 #ready to run anywhere with Java 17.

# Use official OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy built jar into the container
COPY target/tarifi-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]