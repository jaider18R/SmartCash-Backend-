# SmartCash — Backend (usuarios-auth + transacciones-service)

## Que hay implementado
- **usuarios-auth** (puerto 8081): `POST /auth/registro`, `POST /auth/login` (devuelve JWT)
- **transacciones-service** (puerto 8082): `POST /transacciones` (protegido con JWT, categoriza con un mock)

Ambos siguen **arquitectura hexagonal**: `domain/` (entidades + puertos, sin dependencias externas),
`application/` (casos de uso), `infrastructure/` (adaptadores: JPA, seguridad, clientes),
`api/` (controllers, DTOs).

## Principios SOLID aplicados (resumen para tu documento de tesis)

| Principio | Donde se ve |
|---|---|
| **S**RP | Cada clase tiene una sola razon de cambio: el Controller solo traduce HTTP, el Service solo orquesta la regla de negocio, el Adapter solo traduce a JPA |
| **O**CP | `ClasificadorTransaccionPort` permite reemplazar `CategorizacionMockAdapter` por un adaptador HTTP real sin tocar `RegistrarTransaccionService` |
| **L**SP | Cualquier clase que implemente `RepositorioUsuarioPort` (JPA, en memoria, otra BD) puede sustituir a `UsuarioRepositoryAdapter` sin romper nada |
| **I**SP | Puertos pequeños y especificos (`PasswordEncoderPort`, `TokenGeneratorPort`) en vez de una sola interfaz gigante |
| **D**IP | Los servicios de aplicacion dependen de interfaces (`domain/ports`), nunca de clases concretas de infraestructura |

## Como correr cada servicio

1. Crea las bases de datos: `psql -U postgres -f setup-bases-de-datos.sql`
2. Exporta las variables de entorno (o ajusta application.yml):
   ```
   export DB_PASSWORD=tu_password_segura
   export JWT_SECRET=una-clave-secreta-muy-larga-cambiar-en-produccion-1234567890
   ```
3. Desde cada carpeta de servicio: `mvn spring-boot:run`

## Probar el flujo completo (curl)

```bash
# 1. Registro
curl -X POST http://localhost:8081/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Alejandro","correo":"alejandro@test.com","password":"clave12345"}'

# 2. Login (guarda el token que devuelve)
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"alejandro@test.com","password":"clave12345"}'

# 3. Registrar transaccion (usa el token del paso anterior)
curl -X POST http://localhost:8082/transacciones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -d '{"idUsuario":"UUID_DEL_USUARIO","monto":45000,"fecha":"2026-09-20","comercio":"Rappi Colombia","tipoMovimiento":"gasto"}'
```

## Pendiente / siguiente paso
- Reemplazar `CategorizacionMockAdapter` por un adaptador HTTP real cuando exista `categorizacion-service` (Python/FastAPI)
- Agregar Flyway/Liquibase en vez de `ddl-auto: update` antes de pasar a produccion
