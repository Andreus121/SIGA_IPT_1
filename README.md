# SIGA-IPT: Sistema de Gestión Académica

Proyecto integral que incluye arquitectura de microservicios, seguridad OAuth2 con Keycloak, backend con Spring Boot (Java 21), balanceador Nginx, base de datos PostgreSQL y frontend en React (Vite).

---

## 1. Requisitos Previos

- **Docker Desktop** (en Windows con WSL2 activado).
- Archivo `.env` configurado en la raíz del proyecto (puedes basarte en `.env.example`).

---

## 2. Cómo Levantar el Proyecto con Docker

### Paso 1: Configurar variables de entorno
Si no tienes el archivo `.env`, créalo copiando el ejemplo:
```powershell
Copy-Item .env.example .env
```

### Paso 2: Levantar todos los contenedores
Para levantar todos los servicios (PostgreSQL, Keycloak, Backend, Nginx y Frontend):
```powershell
docker compose up -d
```

Si deseas levantar únicamente la infraestructura base (Base de datos, Keycloak y Frontend):
```powershell
docker compose up -d postgres keycloak frontend
```

### Paso 3: Verificación de estado
Para revisar que todos los contenedores estén corriendo (`Up`):
```powershell
docker compose ps
```

Para ver los logs en tiempo real de cualquier servicio (por ejemplo Keycloak o Frontend):
```powershell
docker compose logs -f keycloak
docker compose logs -f frontend
```

### Para apagar los servicios:
```powershell
docker compose down
```

---

## 3. Direcciones y Puertos de los Servicios

| Servicio | URL Local | Puerto Contenedor / Host | Descripción |
| :--- | :--- | :--- | :--- |
| **Frontend** | [http://localhost:5173](http://localhost:5173) | `80:5173` | Aplicación web React (Vite + Nginx) |
| **Keycloak** | [http://localhost:8090](http://localhost:8090) | `8080:8090` | Servidor de identidad y login OAuth2 |
| **Nginx (API Gateway)** | [http://localhost:80](http://localhost:80) | `80:80` | Balanceador de carga hacia las 3 réplicas del Backend |
| **PostgreSQL** | `localhost:5435` | `5432:5435` | Base de datos relacional del sistema |

---

## 4. Cuentas de Prueba Configuradas en Keycloak

El Realm configurado es **`siga-realm`** con el cliente **`siga-frontend`**.

Se crearon 3 cuentas de prueba listas para iniciar sesión en el frontend:

| Rol | Usuario | Contraseña | Redirección en Frontend | Permisos |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` | `/admin` | Acceso total: gestión de carreras, mallas, asignaturas, docentes, estudiantes, cierre semestral. |
| **Profesor** | `teacher1` | `teacher123` | `/teacher` | Registro y consulta de calificaciones de sus secciones. |
| **Estudiante** | `student1` | `student123` | `/student` | Matrícula, toma de asignaturas y revisión de su avance académico. |

### Cuenta de Administración de Keycloak:
Para entrar a la consola administrativa de Keycloak (`http://localhost:8090`):
- **Usuario:** `keycloak_username`
- **Contraseña:** `keycloak_pass`

---

## 5. Desarrollo Frontend Local (Opcional sin Docker)

Si prefieres correr el frontend en caliente desde tu máquina local mientras los servicios de apoyo corren en Docker:
```powershell
cd frontend
npm install
npm run dev
```
La aplicación correrá en [http://localhost:5173](http://localhost:5173).
