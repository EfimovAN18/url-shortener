# Этап сборки: зависимости скачиваются отдельным слоем, чтобы не тянуть их заново при каждом изменении кода
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Этап запуска: только JRE и готовый jar
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home appuser
USER appuser
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
