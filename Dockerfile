#Compilación
FROM eclipse-temurin:21-jdk AS buildstage 

RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY src /app/src

RUN mvn clean package
# Ejecución
FROM eclipse-temurin:21-jdk

COPY --from=buildstage /app/target/productor-kafka-0.0.1-SNAPSHOT.jar /app/app.jar


EXPOSE 8080

ENTRYPOINT [ "java", "-jar","/app/app.jar" ]