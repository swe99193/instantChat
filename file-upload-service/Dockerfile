FROM eclipse-temurin:17-jdk
RUN mkdir /app
COPY build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=LocalK8"]