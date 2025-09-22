FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/cloud-file-storage-0.0.1-SNAPSHOT.jar ./cloud-file-storage.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "cloud-file-storage.jar"]