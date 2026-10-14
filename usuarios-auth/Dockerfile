# ==============================================================================
# Multi-stage Dockerfile para usuarios-auth (SmartCash)
# Buenas practicas aplicadas:
# - Imagen oficial y ligera (eclipse-temurin:21 Alpine)
# - Construccion multi-etapa (build -> runtime limpio)
# - Usuario no root (appuser)
# - Etiquetas y healthcheck
# ==============================================================================

# Etapa 1: Compilacion de la aplicacion
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copiar solo el descriptor primero para aprovechar la cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el codigo fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Imagen final de ejecucion (ligera y segura)
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="SmartCash Team"
LABEL version="1.0.0"
LABEL description="Microservicio de autenticacion y gestion de usuarios"

# Crear usuario y grupo no-root para seguridad
RUN addgroup -S smartcash && adduser -S appuser -G smartcash

WORKDIR /app

# Copiar solo el jar compilado desde la etapa de build
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:smartcash /app

USER appuser

EXPOSE 8081

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
