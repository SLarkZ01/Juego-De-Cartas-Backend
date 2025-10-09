## Multi-stage Dockerfile for the Spring Boot backend
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

# Copy maven wrapper and pom
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml pom.xml

# Copy sources
COPY src src

# Ensure mvnw is executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy jar from build stage
COPY --from=build /workspace/app/target/juegocartas-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
