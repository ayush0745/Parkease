FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY analytics-service analytics-service
RUN mvn clean package -pl analytics-service -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/analytics-service/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
