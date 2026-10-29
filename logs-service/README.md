# logs-service (puerto 8085)

Microservicio central de logs de SmartCash. Recibe los errores y eventos de los demas
microservicios y los guarda en su propia base de datos PostgreSQL (`logs_db`, tabla `logs_sistema`).

## Como funciona
- Cada microservicio sigue escribiendo sus logs por consola (factor XI de los 12 factores) **y ademas** envia
  los de nivel WARN/ERROR a `POST /api/logs` en segundo plano. Si `logs-service` esta caido, el servicio
  de origen sigue funcionando normalmente.
- Se enmascaran contrasenas, tokens y claves antes de guardar.
- La base `logs_db` se crea sola la primera vez que arranca el servicio.
- Arquitectura hexagonal: `domain/ports` (interfaz) -> `application` (casos de uso) -> `infrastructure/persistence` (adaptador SQL) -> `api`.

## Endpoints (documentacion interactiva en `/docs`)
| Metodo | Ruta | Para que sirve |
|---|---|---|
| POST | `/api/logs` | Registrar un log |
| GET | `/api/logs` | Consultar con filtros: `servicio`, `nivel`, `desde`, `hasta`, `texto`, `limite`, `desplazamiento` |
| GET | `/api/logs/resumen` | Cantidad de logs por servicio y nivel |
| DELETE | `/api/logs/purgar?dias=30` | Borrar logs con mas de N dias |
| GET | `/health` | Estado del servicio |

Si `LOGS_API_KEY` tiene valor, GET y DELETE exigen el encabezado `X-API-Key`.

## Consultar directamente en la base de datos
```sql
SELECT fecha, servicio, nivel, mensaje, ruta, estado_http
FROM logs_sistema ORDER BY fecha DESC LIMIT 50;

SELECT servicio, nivel, COUNT(*) FROM logs_sistema GROUP BY servicio, nivel;
```

## Variables de entorno
| Variable | Por defecto | Descripcion |
|---|---|---|
| `DATABASE_URL` | SQLite local | Conexion a `logs_db` (en Docker: PostgreSQL) |
| `PORT` | 8085 | Puerto |
| `LOG_LEVEL` | info | Nivel de logs de uvicorn |
| `LOGS_API_KEY` | vacio | Si se define, protege consulta y purga |
| `CORS_ALLOWED_ORIGINS` | `*` | Origenes permitidos |

## Pruebas
```
pip install -r requirements.txt
python -m pytest -q tests
```
