FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests package -pl app -am

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/app/target/*-exec.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
