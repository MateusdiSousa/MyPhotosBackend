# INSTALANDO DEPENDÊNCIAS COM MAVEN 
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

COPY src ./src

# RUN mvn dependency:go-offline -B

RUN mvn clean package -DskipTests

# RODANDO A APLICAÇÃO

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]

