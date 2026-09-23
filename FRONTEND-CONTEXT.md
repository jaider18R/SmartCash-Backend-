# Contexto del Proyecto: SmartCash — Frontend (React)

Este documento contiene el contexto arquitectónico, las reglas de negocio y los contratos de API del backend de **SmartCash** para guiar el desarrollo de la aplicación web Frontend en **React**.

---

## 1. Visión general del producto

**SmartCash** es una plataforma web de finanzas personales diseñada para el contexto económico colombiano (manejo de comercios locales como Rappi, Nequi, D1, Éxito, Carulla, etc.).

### Flujo central:
1. El usuario se registra e inicia sesión (recibe un token JWT).
2. El usuario registra manualmente sus ingresos y gastos.
3. El sistema **categoriza automáticamente** la transacción al registrarla y le asigna un porcentaje de confianza (`confianzaCategorizacion`).
4. En fases posteriores, el usuario podrá corregir categorías erróneas para alimentar un modelo de Machine Learning incremental (aporte central de tesis).
5. Módulos complementarios planeados: **Presupuestos y Alertas**, **Metas de ahorro** e **Inversiones**.

---

## 2. Stack tecnológico y arquitectura Frontend recomendada

* **Librería / Framework:** React (Vite + TypeScript recomendado para coherencia estricta de contratos con el backend).
* **Estilos:** Tailwind CSS (o la librería de componentes preferida: Shadcn UI, Material UI o Chakra UI).
* **Manejo de estado y llamadas HTTP:** Axios con interceptores para JWT, React Query (TanStack Query) o Context API/Zustand.
* **Moneda / Formato:** Moneda colombiana (COP) con formato de miles y decimales (`Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' })`).
* **Enrutamiento:** React Router DOM (con protección de rutas privadas para usuarios autenticados).

---

## 3. Estado actual del Backend y Puertos de red

El backend está desarrollado en **microservicios con arquitectura hexagonal** (Java 21 + Spring Boot 3 + PostgreSQL). Actualmente no hay API Gateway, por lo que el Frontend se comunica directamente con cada microservicio en sus respectivos puertos locales:

| Microservicio | Puerto local | Responsabilidad actual |
|---|---|---|
| `usuarios-auth` | `http://localhost:8081` | Registro de usuarios, Login y emisión de JWT. |
| `transacciones-service` | `http://localhost:8082` | Creación de transacciones y categorización automática. |
| `presupuestos-service` | *Puerto 8083 (Planeado)* | Gestión de presupuestos y alertas. |
| `inversiones-service` | *Planeado* | Portafolio e histórico de inversión. |
| `metas-service` | *Planeado* | Metas de ahorro y aportes. |
| `categorizacion-service` | *Planeado (FastAPI)* | Servicio de ML incremental. |

---

## 4. Seguridad y manejo de sesión (JWT Stateless)

* La autenticación usa **JWT (JSON Web Token)** sin cookies de sesión en el servidor.
* Duración del token: 1 hora (`3600000 ms`).
* Para todas las peticiones a endpoints protegidos (como transacciones), el Frontend **debe** enviar el header HTTP:
  ```http
  Authorization: Bearer <TOKEN_JWT_AQUI>
  ```
* Se debe implementar un interceptor en Axios/Fetch que inserte automáticamente este header y capture respuestas `401 Unauthorized` para redirigir a `/login`.

---

## 5. Contratos de API (Endpoints listos para consumir)

### A. Microservicio de Usuarios y Autenticación (`http://localhost:8081`)

#### 1. Registro de usuario
* **Endpoint:** `POST http://localhost:8081/auth/registro`
* **Público:** Sí (no requiere token)
* **Headers:** `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "nombre": "Alejandro Cristancho",
    "correo": "usuario@ejemplo.com",
    "password": "PasswordSeguro123"
  }
  ```
  *(Validaciones: nombre obligatorio, correo con formato válido, password mínimo 8 caracteres).*
* **Response Exitosa (`201 Created`):**
  ```json
  {
    "idUsuario": "d0d6fb3c-e44e-4ab3-b556-883452cf679c",
    "nombre": "Alejandro Cristancho",
    "correo": "usuario@ejemplo.com",
    "fechaRegistro": "2026-09-22T13:34:58.682"
  }
  ```
* **Posibles Errores:**
  * `409 Conflict`: `{"error": "El correo usuario@ejemplo.com ya esta registrado"}`
  * `400 Bad Request`: Error de validación en campos.

#### 2. Inicio de sesión (Login)
* **Endpoint:** `POST http://localhost:8081/auth/login`
* **Público:** Sí (no requiere token)
* **Headers:** `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "correo": "usuario@ejemplo.com",
    "password": "PasswordSeguro123"
  }
  ```
* **Response Exitosa (`200 OK`):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkMGQ2ZmIzYy1lNDRlLTRhYjMtYjU1Ni04ODM0NTJjZjY3OWMiLCJjb3JyZW8iOiJ1c3VhcmlvQGVqZW1wbG8uY29tIiwiZXhwIjoxNzkwMTA2ODAwfQ..."
  }
  ```
  *(El token contiene como claim `sub` el `idUsuario` y como claim `correo` el email del usuario).*
* **Posibles Errores:**
  * `401 Unauthorized`: `{"error": "Credenciales invalidas"}`

---

### B. Microservicio de Transacciones (`http://localhost:8082`)

#### 1. Registrar Transacción (con auto-categorización)
* **Endpoint:** `POST http://localhost:8082/transacciones`
* **Público:** No. Requiere `Authorization: Bearer <TOKEN>`
* **Headers:** 
  * `Content-Type: application/json`
  * `Authorization: Bearer <TOKEN>`
* **Request Body:**
  ```json
  {
    "idUsuario": "d0d6fb3c-e44e-4ab3-b556-883452cf679c",
    "monto": 45000.00,
    "fecha": "2026-09-22",
    "comercio": "Rappi Colombia",
    "tipoMovimiento": "gasto"
  }
  ```
  *(Validaciones: `monto > 0`, `comercio` no vacío, `tipoMovimiento` únicamente `"ingreso"` o `"gasto"`, `fecha` en formato ISO `YYYY-MM-DD`).*
* **Response Exitosa (`201 Created`):**
  ```json
  {
    "idTransaccion": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "idUsuario": "d0d6fb3c-e44e-4ab3-b556-883452cf679c",
    "idCategoria": "e8f1a2b3-c4d5-6e7f-8a9b-0c1d2e3f4a5b",
    "monto": 45000.00,
    "fecha": "2026-09-22",
    "comercio": "Rappi Colombia",
    "tipoMovimiento": "gasto",
    "confianzaCategorizacion": 0.90
  }
  ```
  *Nota sobre categorización:* El backend infiere la categoría automáticamente según el nombre del comercio (ej. Rappi -> Domicilios, Éxito/D1 -> Mercado, Netflix -> Suscripciones) y devuelve el nivel de confianza de la predicción.
* **Posibles Errores:**
  * `401 Unauthorized`: Token ausente o expirado.
  * `400 Bad Request`: Datos inválidos o incompletos.

---

## 6. Vistas mínimas iniciales a construir en el Frontend

1. **Pantalla de Autenticación (`/login` y `/registro`):**
   * Formulario con validación visual.
   * Manejo de alertas en caso de credenciales inválidas o correo duplicado.
   * Al iniciar sesión, almacenar el token en `localStorage` o memoria y redirigir al Dashboard.

2. **Dashboard Principal (`/dashboard`):**
   * Barra de navegación con datos del usuario y botón de cerrar sesión.
   * Resumen rápido (Balance total, total de ingresos, total de gastos).
   * Botón de acción rápida: *“Nueva Transacción”*.

3. **Módulo de Transacciones (`/transacciones`):**
   * Modal o formulario para registrar transacción (Monto en COP, Fecha, Nombre del comercio, Tipo: Ingreso/Gasto).
   * Visualización del resultado con su categoría asignada y badge de confianza (ej. `90% confianza`).

4. **Preparación de módulos futuros (menú de navegación):**
   * *Presupuestos* (Límites de gasto por categoría con barra de progreso).
   * *Metas de ahorro* (Progreso de aportes hacia un objetivo).
   * *Inversiones* (Seguimiento de portafolio y valor actual).

---

## 7. Instrucciones para la IA que genere el código Frontend

* **Estructura limpia de carpetas:**
  ```text
  src/
  ├── assets/
  ├── components/      # Componentes UI reutilizables (Botones, Modales, Inputs)
  ├── context/         # AuthContext para sesión y token
  ├── hooks/           # Custom hooks
  ├── pages/           # Vistas (Login, Register, Dashboard, Transacciones)
  ├── services/        # Clientes HTTP (apiAuth.ts, apiTransacciones.ts)
  ├── types/           # Interfaces TypeScript de DTOs y modelos
  └── utils/           # Formateadores de fecha y moneda (COP)
  ```
* **Variables de entorno (`.env`):**
  ```env
  VITE_AUTH_API_URL=http://localhost:8081
  VITE_TRANSACCIONES_API_URL=http://localhost:8082
  ```
* Aplica un diseño moderno, limpio, responsivo y orientado a fintech.
