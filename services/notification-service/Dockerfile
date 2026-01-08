FROM eclipse-temurin:21-jdk-jammy
VOLUME /tmp
ARG JAR_FILE=target/notification-service-1.0.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]