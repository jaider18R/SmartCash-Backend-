Smart cash


## Variables de entorno (configuración externalizada)

Toda configuración que cambia entre entornos se lee de variables de entorno
(factor III de los 12 factores). Copiar `.env.example` como `.env` y completar.
Las marcadas como **obligatorias** no tienen valor por defecto: sin ellas el servicio no arranca.

| Variable | Servicios | Obligatoria | Valor por defecto | Para qué sirve |
|---|---|---|---|---|
| `DB_PASSWORD` | usuarios-auth, transacciones | Sí | — | Contraseña de la base de datos |
| `POSTGRES_PASSWORD` | postgres | Sí | — | Contraseña del superusuario de PostgreSQL |
| `JWT_SECRET` | usuarios-auth, transacciones | Sí | — | Clave de firma del JWT (mín. 32 caracteres) |
| `EMAIL_API_KEY` | notificaciones | No | `mock-key` | Clave de la API de Brevo (`mock-key` = no envía correos reales) |
| `EMAIL_REMITENTE` / `EMAIL_REMITENTE_NOMBRE` | notificaciones | No | `soporte@smartcash.com` / `SmartCash` | Remitente de los correos |
| `FRONTEND_URL` | usuarios-auth, notificaciones | No | `http://localhost:5173` | URL pública del frontend (enlaces de los correos) |
| `CORS_ALLOWED_ORIGINS` | servicios Java | No | localhost y redes privadas | Orígenes permitidos por CORS, separados por coma |
| `CORS_ALLOWED_ORIGINS_PY` | servicios Python | No | `*` | Orígenes permitidos por CORS (analítica y categorización) |
| `LOG_LEVEL` | servicios Java | No | `INFO` | Nivel de logs |
| `LOG_LEVEL_PY` | servicios Python | No | `info` | Nivel de logs (minúsculas, para uvicorn) |
| `JPA_DDL_AUTO` | usuarios-auth, transacciones | No | `update` | Estrategia de esquema de Hibernate |
| `PORT` | todos | No | 8081/8082/8083/8000/8084 | Puerto en el que escucha cada servicio |
| `TRANSACCIONES_SERVICE_URL` | analítica | No | `http://localhost:8082` | URL del servicio de transacciones |
| `SPRING_DATASOURCE_URL` / `_USERNAME` | usuarios-auth, transacciones | No | localhost | Conexión a PostgreSQL |
| `DATABASE_URL` | categorización | No | SQLite local | Base de datos de categorización |

## Logs centralizados (logs-service)

Los errores (nivel ERROR) y advertencias (WARN) de los 5 microservicios se envian a `logs-service`
(puerto 8085), que los guarda en la base de datos `logs_db`, tabla `logs_sistema`. Los logs tambien
siguen saliendo por consola. Ver `logs-service/README.md`.

| Variable | Servicios | Por defecto | Descripcion |
|---|---|---|---|
| `LOGS_HABILITADO` | todos | `true` | Si es `false`, el servicio no envia logs al logs-service |
| `SMARTCASH_SERVICIOS_LOGS_URL` | servicios Java | `http://localhost:8085` | URL del logs-service |
| `LOGS_SERVICE_URL` | servicios Python | `http://localhost:8085` | URL del logs-service |
| `LOGS_NIVEL_MINIMO` | servicios Python | `WARNING` | Nivel minimo que se envia |
| `LOGS_API_KEY` | logs-service | vacio | Protege consulta y purga de logs |
