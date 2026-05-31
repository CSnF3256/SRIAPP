# Sistema de Verificación SRI + ANT

Solución completa para verificar contribuyentes del SRI y puntos de licencia ANT.

## Arquitectura

```
Usuario → React (Wizard) → Spring Boot API → SRI APIs
                                           → ANT (caché Redis + circuit breaker)
```

## Patrones aplicados

| Patrón | Dónde | Por qué |
|--------|-------|---------|
| **Cache Aside** | `AntService.java` | ANT tiene baja disponibilidad; se lee caché primero, se escribe solo al tener respuesta válida |
| **Circuit Breaker** | `AntClient.java` + `application.yml` | Si ANT falla >50% de llamadas, se abre el circuito 30s y no se desperdician conexiones |
| **Retry** | Resilience4j | 2 reintentos automáticos con espera de 3s |
| **TimeLimiter** | Resilience4j | Timeout de 20s por llamada a la ANT |

## Requisitos

- Java 21+
- Node 20+
- Docker + Docker Compose (opcional)
- Redis (local o Redis Cloud)

---

## Inicio rápido con Docker

```bash
# Levantar todo (Redis + Backend + Frontend)    
docker-compose up --build

# Frontend: http://localhost:5173
# Backend:  http://localhost:8080
# Redis:    localhost:6379
```

---

## Desarrollo local (sin Docker)

### 1. Redis local
```bash
# macOS
brew install redis && redis-server

# Ubuntu
sudo apt install redis-server && redis-server
```

### 2. Backend Spring Boot
```bash
cd backend
./mvnw spring-boot:run
# Corre en http://localhost:8080
```

### 3. Frontend React
```bash
cd frontend
npm install
npm run dev
# Corre en http://localhost:5173
```

---

## Configuración Redis Cloud (producción)

En `backend/src/main/resources/application.yml` o variables de entorno:

```bash
export REDIS_HOST=redis-xxxxx.c1.us-east-1-2.ec2.cloud.redislabs.com
export REDIS_PORT=17xxx
export REDIS_PASSWORD=TuPasswordRedisCloud
export REDIS_SSL=true
```

---

## Endpoints del backend

### SRI
| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/sri/contribuyente/verificar?ruc=1712345678001` | Verifica si existe el RUC |
| GET | `/api/sri/persona?ruc=1712345678001` | Datos de persona natural |
| GET | `/api/sri/vehiculo?placa=ABC1234` | Datos del vehículo |

### ANT (con caché)
| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/ant/licencia?cedula=1712345678&placa=ABC1234` | Puntos de licencia (cache aside) |
| DELETE | `/api/ant/licencia/cache?cedula=...&placa=...` | Invalidar caché |

La respuesta de `/api/ant/licencia` incluye `"fromCache": true/false` para que el frontend informe al usuario.

---

## Flujo del wizard (pasos)

```
1. Email       → captura correo del usuario
2. RUC/SRI     → verifica contribuyente → obtiene persona natural
3. Vehículo    → consulta matrícula en SRI
4. ANT         → consulta puntos de licencia (cache aside)
5. Resumen     → muestra toda la información consolidada
```

---

## Caché Redis — estrategia

```
┌─────────────────────────────────────────────────────┐
│  AntService.consultarConCache(cedula, placa)         │
│                                                     │
│  1. GET redis:"ant:licencia:{cedula}:{placa}"        │
│     ├─ HIT  → devolver con fromCache=true ──────────┤
│     └─ MISS →                                       │
│         2. AntClient.consultarLicencia(...)          │
│            ├─ OK  → SET redis key TTL 24h            │
│            │         devolver con fromCache=false    │
│            └─ KO  → lanzar excepción (503)           │
└─────────────────────────────────────────────────────┘
```

TTL configurado: 24 horas. La clave expira automáticamente.
