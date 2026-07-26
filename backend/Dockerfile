# FlagForge backend — multi-stage build (Java 21)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Cache dependencies
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S flagforge && adduser -S flagforge -G flagforge
USER flagforge

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
