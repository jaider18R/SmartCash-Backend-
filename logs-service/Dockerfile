# ==============================================================================
# Dockerfile de logs-service (SmartCash) - Python / FastAPI
# - Imagen oficial ligera, usuario no root, configuracion solo por variables de entorno
# ==============================================================================
FROM python:3.12-slim

LABEL maintainer="SmartCash Team"
LABEL version="1.0.0"
LABEL description="Microservicio central de logs (errores y eventos) de SmartCash"

RUN useradd -m -u 1000 appuser

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app ./app
RUN chown -R appuser:appuser /app

USER appuser

ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV PORT=8085
ENV LOG_LEVEL=info

EXPOSE 8085

CMD ["sh", "-c", "uvicorn app.main:app --host 0.0.0.0 --port ${PORT} --log-level ${LOG_LEVEL}"]
