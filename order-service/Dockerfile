FROM openjdk:17-jdk-slim

EXPOSE 8081

ADD target/order-sevice-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]