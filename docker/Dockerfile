FROM eclipse-temurin:25-jre

WORKDIR /

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]