FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN --mount=type=cache,target=/root/.m2 ./mvnw -pl email-service -am package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/email-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]