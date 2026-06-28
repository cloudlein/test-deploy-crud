# Stage 1: Build the native executable
FROM ghcr.io/graalvm/native-image-community:25 AS builder

WORKDIR /app

# Copy gradle files
COPY gradle/ gradle/
COPY gradlew gradlew
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle

# Ensure gradlew is executable
RUN chmod +x gradlew

# Copy source code
COPY src/ src/

# Build native image (tests are excluded to optimize build speed and memory in CI/Render)
RUN ./gradlew nativeCompile -x test

# Stage 2: Create a minimal runtime image
# Using a slim Ubuntu image to ensure compatibility with dynamic libraries compiled by GraalVM
FROM ubuntu:noble

WORKDIR /app

# Copy the compiled native executable from builder stage
COPY --from=builder /app/build/native/nativeCompile/test-deploy-crud /app/test-deploy-crud

# Expose the application port (Render will automatically detect this or bind via PORT env)
EXPOSE 8080

# Execute the native binary
ENTRYPOINT ["/app/test-deploy-crud"]
