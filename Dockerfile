FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar /app/server-0.0.1.jar

EXPOSE 8987

ENTRYPOINT ["java", "-jar", "/app/server-0.0.1.jar"]
