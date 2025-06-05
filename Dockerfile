FROM openjdk:17-jdk-slim

WORKDIR /app

EXPOSE 8080
EXPOSE 8081

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
