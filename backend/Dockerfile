FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -e -B -DskipTests package -Dquarkus.package.type=uber-jar || true
COPY src ./src
RUN mvn -B -DskipTests package -Dquarkus.package.type=uber-jar

FROM eclipse-temurin:17-jre
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/target/*-runner.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
