FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B clean verify

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build --chown=10001:10001 /build/target/unit-converter.jar /app/app.jar
USER 10001:10001
ENV APP_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-jar", "/app/app.jar"]
