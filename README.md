# Sistema de Verificación Integrada SRI - ANT

## 1. Descripción general

Este proyecto implementa un sistema web para verificar información de una persona natural, su vehículo y sus puntos de licencia, integrando servicios del SRI y ANT.

La solución está compuesta por:

- **Frontend:** React
- **Backend:** Java Spring Boot
- **Caché Cloud:** Redis Cloud
- **APIs externas:**
  - SRI - Verificación de contribuyente
  - SRI - Consulta de persona natural por RUC
  - SRI - Consulta de matrícula vehicular por placa
  - ANT - Consulta de puntos/licencia mediante web con baja disponibilidad

El sistema aplica un patrón de caché para la consulta ANT. Cuando la ANT responde correctamente, el backend guarda la información en Redis Cloud. En consultas posteriores, el sistema puede devolver la información desde caché para evitar fallos por baja disponibilidad.

---

## 2. Estructura general del ZIP

Al descomprimir el proyecto, se debe tener una estructura similar a:

```text
sri-ant-system/
├── backend/
│   ├── pom.xml
│   ├── src/
│   └── target/                  # Se genera automáticamente
│
├── frontend/
│   ├── package.json
│   ├── src/
│   └── node_modules/            # Se genera con npm install
│
└── README.md
```

> Importante: si no existe `node_modules`, es normal. Se crea ejecutando `npm install`.

---

## 3. Requisitos previos

Antes de ejecutar el sistema, verificar que estén instalados:

### 3.1 Java JDK 21

Comprobar:

```powershell
java -version
javac -version
```

Debe mostrarse una versión 21, por ejemplo:

```text
openjdk version "21.0.x"
javac 21.0.x
```

### 3.2 Maven

Comprobar:

```powershell
mvn -version
```

Debe indicar que Maven usa Java 21. En la salida debe aparecer algo similar a:

```text
Java version: 21
```

Si Maven muestra Java 17, Java 11 u otra versión, configurar `JAVA_HOME`.

Ejemplo:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

java -version
javac -version
mvn -version
```

### 3.3 Node.js y npm

Comprobar:

```powershell
node -v
npm -v
```

---

## 4. Configuración de Redis Cloud

El sistema usa Redis Cloud como caché para la consulta ANT.

Antes de levantar el backend, configurar las variables de entorno en PowerShell.

> Por seguridad, no se incluye la contraseña real en este README. Reemplazar `COLOCAR_PASSWORD_REDIS_CLOUD` por la contraseña de Redis Cloud entregada para la prueba.

```powershell
$env:REDIS_HOST="harbor-laudable-drink-62605.db.redis.io"
$env:REDIS_PORT="18145"
$env:REDIS_USERNAME="default"
$env:REDIS_PASSWORD="COLOCAR_PASSWORD_REDIS_CLOUD"
$env:REDIS_SSL="false"
$env:CORS_ORIGINS="http://localhost:3000,http://localhost:5173"
```

### 4.1 Verificar variables

Ejecutar:

```powershell
echo $env:REDIS_HOST
echo $env:REDIS_PORT
echo $env:REDIS_USERNAME
echo $env:REDIS_SSL
[string]::IsNullOrWhiteSpace($env:REDIS_PASSWORD)
```

El último comando debe devolver:

```text
False
```

Si devuelve `True`, significa que la contraseña no está cargada.

### 4.2 Verificar conexión al puerto de Redis Cloud

```powershell
Test-NetConnection harbor-laudable-drink-62605.db.redis.io -Port 18145
```

Debe aparecer:

```text
TcpTestSucceeded : True
```

Si aparece `False`, revisar conexión a internet, firewall o datos del endpoint Redis Cloud.

---

## 5. Configuración importante en `application.yml`

El archivo se encuentra en:

```text
backend/src/main/resources/application.yml
```

Debe tener esta configuración:

```yaml
server:
  port: 8080

spring:
  application:
    name: verificacion-sri-ant

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      username: ${REDIS_USERNAME:}
      password: ${REDIS_PASSWORD:}
      ssl:
        enabled: ${REDIS_SSL:false}
      timeout: 3000ms

  cache:
    type: redis
    redis:
      time-to-live: 86400000

cors:
  allowed-origins: ${CORS_ORIGINS:http://localhost:3000,http://localhost:5173}
```

> Importante: no editar archivos dentro de `target/classes`, porque Maven los regenera. Editar siempre `src/main/resources/application.yml`.

---

## 6. Ejecución del backend

Abrir PowerShell y entrar a la carpeta del backend:

```powershell
cd "D:\JavaWorks\APPSRI\sri-ant-system\backend"
```

Configurar Java si es necesario:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Configurar Redis Cloud:

```powershell
$env:REDIS_HOST="harbor-laudable-drink-62605.db.redis.io"
$env:REDIS_PORT="18145"
$env:REDIS_USERNAME="default"
$env:REDIS_PASSWORD="COLOCAR_PASSWORD_REDIS_CLOUD"
$env:REDIS_SSL="false"
$env:CORS_ORIGINS="http://localhost:3000,http://localhost:5173"
```

Ejecutar:

```powershell
mvn clean spring-boot:run
```

Si el backend levanta correctamente, debe verse algo similar a:

```text
Tomcat started on port 8080
Started VerificacionSriAntApplication
```

No cerrar esta terminal mientras se usa el sistema.

---

## 7. Pruebas rápidas del backend

Abrir otra terminal PowerShell y probar:

### 7.1 Persona SRI

```powershell
curl.exe "http://localhost:8080/api/sri/persona?ruc=1001787603001"
```

Debe devolver datos como:

```json
{
  "numeroRuc": "1001787603001",
  "razonSocial": "...",
  "tipoContribuyente": "PERSONA NATURAL"
}
```

### 7.2 Vehículo SRI

```powershell
curl.exe "http://localhost:8080/api/sri/vehiculo?placa=PBD7436"
```

Debe devolver datos como:

```json
{
  "numeroPlaca": "PBD7436",
  "descripcionMarca": "RENAULT",
  "descripcionModelo": "SANDERO 1.6 MT DYNAMIQUE",
  "anioAuto": 2009
}
```

### 7.3 ANT y Redis Cloud

Ejecutar dos veces:

```powershell
curl.exe "http://localhost:8080/api/ant/licencia?cedula=1001787603&placa=PBD7436"
```

Primera vez puede devolver:

```json
"fromCache": false
```

Segunda vez debería devolver:

```json
"fromCache": true
```

Esto confirma que la respuesta fue guardada y recuperada desde Redis Cloud.

### 7.4 CORS

```powershell
curl.exe -i -H "Origin: http://localhost:3000" "http://localhost:8080/api/ant/licencia?cedula=1001787603&placa=PBD7436"
```

Debe aparecer:

```text
Access-Control-Allow-Origin: http://localhost:3000
```

---

## 8. Ejecución del frontend

Abrir una nueva terminal PowerShell y entrar a la carpeta del frontend:

```powershell
cd "D:\JavaWorks\APPSRI\sri-ant-system\frontend"
```

Instalar dependencias:

```powershell
npm install
```

Ejecutar:

```powershell
npm start
```

Si el proyecto usa Vite en lugar de React Scripts, usar:

```powershell
npm run dev
```

Abrir en el navegador:

```text
http://localhost:3000
```

o si usa Vite:

```text
http://localhost:5173
```

---

## 9. Flujo de uso del sistema

1. Ingresar correo electrónico.
2. Ingresar RUC de persona natural.
3. El sistema verifica si existe como contribuyente del SRI.
4. El sistema obtiene los datos del contribuyente desde el SRI.
5. Ingresar la placa del vehículo.
6. El sistema consulta la información vehicular en el SRI.
7. El sistema consulta los puntos de licencia en la ANT.
8. Si la ANT responde correctamente, los datos se guardan en Redis Cloud.
9. Si se vuelve a consultar el mismo caso, la respuesta puede devolverse desde caché.
10. Se muestra un resumen final con datos SRI, vehículo y puntos ANT.

---

## 10. Errores comunes y soluciones

### Error 1: `ERR_CONNECTION_REFUSED`

Significa que el frontend o backend no está corriendo.

Solución:

- Verificar backend:

```powershell
curl.exe "http://localhost:8080/api/sri/vehiculo/raw?placa=PBD7436"
```

- Verificar frontend:
  - Entrar a `http://localhost:3000` o `http://localhost:5173`
  - Revisar que `npm start` o `npm run dev` esté ejecutándose.

---

### Error 2: `release version 21 not supported`

Maven no está usando Java 21.

Solución:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

java -version
javac -version
mvn -version
```

Luego:

```powershell
mvn clean spring-boot:run
```

---

### Error 3: `Unable to connect to Redis`

El backend no puede conectarse a Redis Cloud.

Soluciones:

1. Verificar variables:

```powershell
echo $env:REDIS_HOST
echo $env:REDIS_PORT
echo $env:REDIS_USERNAME
echo $env:REDIS_SSL
[string]::IsNullOrWhiteSpace($env:REDIS_PASSWORD)
```

2. Verificar puerto:

```powershell
Test-NetConnection harbor-laudable-drink-62605.db.redis.io -Port 18145
```

3. Confirmar que `REDIS_SSL` esté en:

```powershell
$env:REDIS_SSL="false"
```

En este proyecto, la conexión funcionó usando `REDIS_SSL=false`.

4. Reiniciar el backend después de configurar variables:

```powershell
mvn clean spring-boot:run
```

---

### Error 4: `Invalid CORS request`

El backend no permite el origen del frontend.

Solución:

Configurar:

```powershell
$env:CORS_ORIGINS="http://localhost:3000,http://localhost:5173"
```

Y en `application.yml`:

```yaml
cors:
  allowed-origins: ${CORS_ORIGINS:http://localhost:3000,http://localhost:5173}
```

Luego reiniciar backend.

---

### Error 5: `mvnw no se reconoce`

El proyecto no tiene Maven Wrapper o no se está usando correctamente.

Solución:

Usar Maven instalado:

```powershell
mvn clean spring-boot:run
```

Si existiera wrapper, en Windows sería:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## 11. Notas de seguridad

No subir contraseñas reales de Redis Cloud a GitHub ni colocarlas directamente en `application.yml`.

La contraseña debe configurarse mediante variable de entorno:

```powershell
$env:REDIS_PASSWORD="..."
```

---

## 12. Tecnologías utilizadas

- React
- Java Spring Boot
- Maven
- Redis Cloud
- WebClient
- Resilience4j
- APIs públicas del SRI
- Consulta web ANT con caché
