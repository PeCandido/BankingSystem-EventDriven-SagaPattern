FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY core-common/pom.xml ./core-common/
COPY core-common/src ./core-common/src
COPY merchant-service/pom.xml ./merchant-service/
COPY merchant-service/src ./merchant-service/src
RUN mvn clean package -DskipTests -pl merchant-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/merchant-service/target/*.jar app.jar
EXPOSE 8082
CMD ["java", "-jar", "app.jar"]
