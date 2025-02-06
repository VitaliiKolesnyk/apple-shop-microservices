FROM openjdk:17-jdk-slim

EXPOSE 8085

ADD target/user-services-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]