FROM maven:3.8.8-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY . .
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
