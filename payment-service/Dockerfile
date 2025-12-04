FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY core-common/pom.xml ./core-common/
COPY core-common/src ./core-common/src
COPY payment-service/pom.xml ./payment-service/
COPY payment-service/src ./payment-service/src
RUN mvn clean package -DskipTests -pl payment-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/payment-service/target/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
