# Use official Java 17 image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the jar file
CMD ["java", "-jar", "target/texttopdftool-0.0.1-SNAPSHOT.jar"]