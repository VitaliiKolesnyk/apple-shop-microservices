FROM openjdk:17-jdk-slim

EXPOSE 9000

ADD target/api_gateway-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]