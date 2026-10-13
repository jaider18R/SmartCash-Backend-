# ==============================================================================
# Multi-stage Dockerfile para transacciones-service (SmartCash)
# ==============================================================================

# Etapa 1: Compilacion de la aplicacion
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Runtime ligero y seguro
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="SmartCash Team"
LABEL version="1.0.0"
LABEL description="Microservicio de gestion de transacciones y categorizacion"

RUN addgroup -S smartcash && adduser -S appuser -G smartcash

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:smartcash /app

USER appuser

EXPOSE 8082

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
