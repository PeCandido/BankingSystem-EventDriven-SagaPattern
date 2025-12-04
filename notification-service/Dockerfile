FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY core-common/pom.xml ./core-common/
COPY core-common/src ./core-common/src
COPY notification-service/pom.xml ./notification-service/
COPY notification-service/src ./notification-service/src
RUN mvn clean package -DskipTests -pl notification-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/notification-service/target/*.jar app.jar
EXPOSE 8083
CMD ["java", "-jar", "app.jar"]
