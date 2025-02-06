FROM openjdk:17-jdk-slim

EXPOSE 8084

ADD target/payment-service-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]