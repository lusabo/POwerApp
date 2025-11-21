FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests

COPY src ./src

# Build com o Quarkus plugin explicitamente
RUN mvn clean package -DskipTests quarkus:build

# Imagem de runtime
FROM eclipse-temurin:17-jre-jammy

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0"
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

WORKDIR /deployments

# Copia todo o target
COPY --from=build /workspace/target /deployments/target

# Script que tenta todas as possibilidades
RUN echo '#!/bin/sh\n\
if [ -f /deployments/target/quarkus-app/quarkus-run.jar ]; then\n\
  echo "Executando fast-jar..."\n\
  exec java $JAVA_OPTS -jar /deployments/target/quarkus-app/quarkus-run.jar\n\
elif [ -f /deployments/target/*-runner.jar ]; then\n\
  echo "Executando uber-jar..."\n\
  exec java $JAVA_OPTS -jar /deployments/target/*-runner.jar\n\
else\n\
  echo "Tentando JAR padrão..."\n\
  JAR=$(find /deployments/target -name "*.jar" ! -name "*-tests.jar" ! -name "*-javadoc.jar" ! -name "*-sources.jar" | head -1)\n\
  if [ -n "$JAR" ]; then\n\
    exec java $JAVA_OPTS -jar "$JAR"\n\
  else\n\
    echo "ERRO: Nenhum JAR encontrado!"\n\
    ls -lah /deployments/target/\n\
    exit 1\n\
  fi\n\
fi' > /deployments/start.sh && chmod +x /deployments/start.sh

EXPOSE 8080

CMD ["/deployments/start.sh"]