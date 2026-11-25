FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
COPY backend ./backend
RUN chmod +x gradlew \
    && ./gradlew --no-daemon -p backend bootJar \
    && find backend/build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -exec cp {} /workspace/app.jar \; \
    && test -s /workspace/app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/app.jar ./app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
