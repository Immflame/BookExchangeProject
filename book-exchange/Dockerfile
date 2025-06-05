FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle clean build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/book-exchange-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_LIQUIBASE_ENABLED=true
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/book_exchange
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=aidar2006

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]