# ==============================================================================
# Multi-stage Dockerfile para categorizacion-service (Python / FastAPI / ML)
# Buenas practicas aplicadas:
# - Imagen oficial y ligera (python:3.12-slim)
# - No root user
# - Sin herramientas de compilacion en runtime
# ==============================================================================

# Etapa 1: Builder para instalar wheels y compilar dependencias C (scikit-learn, numpy)
FROM python:3.12-slim AS builder

WORKDIR /build

# Instalar dependencias necesarias para compilar paquetes C de Python
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Etapa 2: Runtime limpio y liviano
FROM python:3.12-slim AS runtime

LABEL maintainer="SmartCash Team"
LABEL version="1.0.0"
LABEL description="Microservicio de ML y OCR para categorizacion incremental de transacciones"

RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-spa \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -m -u 1000 appuser


WORKDIR /app

# Copiar paquetes instalados desde builder
COPY --from=builder /install /usr/local

# Copiar el codigo fuente
COPY app ./app

# Directorio de datos con permisos para el usuario no-root
RUN mkdir -p /app/data && chown -R appuser:appuser /app

USER appuser

ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV PORT=8000
ENV LOG_LEVEL=info

EXPOSE 8000

CMD ["sh", "-c", "uvicorn app.main:app --host 0.0.0.0 --port ${PORT} --log-level ${LOG_LEVEL}"]
