# Especificación y Documentación de la API REST - SIGA-IPT

Esta documentación está diseñada para el desarrollo del **Frontend**, detallando la configuración de autenticación, todos los DTOs con su JSON exacto, enumeraciones, manejo de errores y cada uno de los endpoints expuestos por los controladores del backend.

---

## 1. Convenciones Generales y Autenticación

- **Base URL:** `http://localhost:8080` (o el puerto configurado del backend Spring Boot)
- **Content-Type:** `application/json`
- **Autenticación:** OAuth2 / OpenID Connect con Keycloak (Resource Server con JWT).
  - En cada petición protegida debe enviarse el header:
    ```http
    Authorization: Bearer <TOKEN_JWT>
    ```
  - Los roles requeridos (`ADMIN`, `TEACHER`, `STUDENT`) se validan desde los claims del token JWT emitido por Keycloak.

### Formato Estándar de Errores (`ApiError`)
Cuando ocurre un error (400 Bad Request, 404 Not Found, 409 Conflict, etc.), el backend responde con este formato JSON:

```json
{
  "timestamp": "2026-10-26T22:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Mensaje detallado del error o validaciones fallidas",
  "path": "/api/students"
}
```

---

## 2. Enumeraciones (Enums)

Los siguientes valores de texto exactos son los utilizados en los JSON:

### `PeriodStatus`
- `"OPEN"`
- `"CLOSED"`

### `CareerStatus`
- `"ACTIVE"`
- `"INACTIVE"`

### `StudyPlanStatus`
- `"CURRENT"`
- `"NOT_CURRENT"`

### `StudentStatus`
- `"REGULAR"`
- `"POSTPONEMENT"`
- `"TEMPORARY_WITHDRAWAL"`
- `"GRADUATED"`
- `"DROPPED"`

### `TeacherStatus`
- `"ACTIVE"`
- `"INACTIVE"`

### `EnrollmentStatus` (Matrícula Anual/Semestral)
- `"ACTIVE"`
- `"SUSPENDED"`
- `"WITHDRAWN"`
- `"CANCELED"`

### `RegistrationStatus` (Inscripción de Asignatura)
- `"REGISTERED"`
- `"CANCELED"`

### `AcademicStatus` (Historial Académico)
- `"PASSED"`
- `"FAILED"`
- `"IN_PROGRESS"`

---

## 3. Catálogo de DTOs y Formato JSON Exacto

### 3.1 Período Académico (`AcademicPeriod`)

#### `AcademicPeriodCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `periodCode` | `string` | Sí | No blanco. Ej: `"2026-1"` |
| `status` | `string` | Sí | `PeriodStatus` (`"OPEN"`, `"CLOSED"`) |

```json
{
  "periodCode": "2026-1",
  "status": "OPEN"
}
```

#### `AcademicPeriodResponseDto` (Response)
```json
{
  "periodCode": "2026-1",
  "status": "OPEN"
}
```

---

### 3.2 Carreras (`Career`)

#### `CareerCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `code` | `string` | Sí | Código único de carrera (ej: `"INF"`) |
| `name` | `string` | Sí | Nombre de la carrera |
| `description` | `string` | Sí | Descripción de la carrera |

```json
{
  "code": "INF",
  "name": "Ingeniería en Informática",
  "description": "Carrera enfocada en desarrollo de software y sistemas"
}
```

#### `CareerUpdateDto` (Request)
| Campo | Tipo | Obligatorio | Descripción |
| :--- | :--- | :--- | :--- |
| `name` | `string` | No | Nuevo nombre |
| `description` | `string` | No | Nueva descripción |
| `status` | `string` | No | `CareerStatus` (`"ACTIVE"`, `"INACTIVE"`) |

```json
{
  "name": "Ingeniería en Computación e Informática",
  "description": "Nueva descripción actualizada",
  "status": "ACTIVE"
}
```

#### `CareerResponseDto` (Response)
```json
{
  "id": 1,
  "code": "INF",
  "name": "Ingeniería en Informática",
  "description": "Carrera enfocada en desarrollo de software y sistemas",
  "status": "ACTIVE"
}
```

---

### 3.3 Planes de Estudio (`StudyPlan`)

#### `StudyPlanCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `code` | `string` | Sí | Código del plan (ej: `"INF-2026"`) |
| `careerCode` | `string` | Sí | Código de la carrera asociada |
| `name` | `string` | Sí | Nombre o versión del plan |
| `setCurrent` | `boolean` | No | Si es `true`, pasa a ser el plan vigente |

```json
{
  "code": "INF-2026",
  "careerCode": "INF",
  "name": "Plan Malla 2026",
  "setCurrent": true
}
```

#### `StudyPlanResponseDto` (Response)
```json
{
  "id": 1,
  "code": "INF-2026",
  "careerCode": "INF",
  "name": "Plan Malla 2026",
  "status": "CURRENT"
}
```

---

### 3.4 Asignaturas (`Course`)

#### `CourseCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `code` | `string` | Sí | Código de la asignatura (ej: `"INF101"`) |
| `studyPlanCode` | `string` | Sí | Código del plan al que pertenece |
| `name` | `string` | Sí | Nombre de la asignatura |
| `semester` | `number (int)` | Sí | Semestre en la malla (`1` a `4`) |
| `theoryHours` | `number (int)` | Sí | Horas de teoría (`>= 0`) |
| `exerciseHours` | `number (int)` | Sí | Horas de ejercicios (`>= 0`) |
| `labHours` | `number (int)` | Sí | Horas de laboratorio (`>= 0`) |
| `sct` | `number (int)` | Sí | Créditos SCT (`1` a `7`) |
| `prerequisiteCodes` | `string[]` | No | Conjunto de códigos de ramos prerrequisitos |

```json
{
  "code": "INF102",
  "studyPlanCode": "INF-2026",
  "name": "Estructuras de Datos",
  "semester": 2,
  "theoryHours": 4,
  "exerciseHours": 2,
  "labHours": 2,
  "sct": 5,
  "prerequisiteCodes": [
    "INF101"
  ]
}
```

#### `CourseUpdateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `name` | `string` | No | Nombre |
| `semester` | `number (int)` | No | Semestre (`1` a `4`) |
| `theoryHours` | `number (int)` | No | Horas teoría (`>= 0`) |
| `exerciseHours` | `number (int)` | No | Horas ejercicios (`>= 0`) |
| `labHours` | `number (int)` | No | Horas lab (`>= 0`) |
| `sct` | `number (int)` | No | Créditos SCT (`1` a `7`) |
| `prerequisiteCodes` | `string[]` | No | Códigos prerrequisitos a actualizar |

```json
{
  "name": "Estructuras de Datos Avanzadas",
  "semester": 2,
  "theoryHours": 4,
  "exerciseHours": 2,
  "labHours": 2,
  "sct": 6,
  "prerequisiteCodes": [
    "INF101"
  ]
}
```

#### `CourseResponseDto` (Response)
```json
{
  "id": 10,
  "code": "INF102",
  "name": "Estructuras de Datos",
  "semester": 2,
  "theoryHours": 4,
  "exerciseHours": 2,
  "labHours": 2,
  "sct": 5,
  "studyPlanCode": "INF-2026",
  "prerequisites": [
    "INF101"
  ]
}
```

---

### 3.5 Estudiantes (`Student`)

#### `StudentCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `run` | `string` | Sí | RUN/RUT del estudiante (ej: `"12345678-9"`) |
| `fullName` | `string` | Sí | Nombre y apellidos |
| `email` | `string` | Sí | Email institucional válido |
| `careerCode` | `string` | Sí | Código de la carrera |

```json
{
  "run": "12345678-9",
  "fullName": "Juan Pérez González",
  "email": "juan.perez@alumnos.instituto.cl",
  "careerCode": "INF"
}
```

#### `StudentUpdateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `fullName` | `string` | No | Nombre completo |
| `email` | `string` | No | Email válido |
| `careerCode` | `string` | No | Código carrera |
| `status` | `string` | No | `StudentStatus` (`"REGULAR"`, `"POSTPONEMENT"`, etc.) |

```json
{
  "fullName": "Juan Carlos Pérez González",
  "email": "juan.perez@alumnos.instituto.cl",
  "careerCode": "INF",
  "status": "REGULAR"
}
```

#### `StudentResponseDto` (Response)
```json
{
  "id": 1,
  "run": "12345678-9",
  "fullName": "Juan Pérez González",
  "email": "juan.perez@alumnos.instituto.cl",
  "careerCode": "INF",
  "studyPlanCode": "INF-2026",
  "status": "REGULAR"
}
```

---

### 3.6 Docentes (`Teacher`)

#### `TeacherCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `run` | `string` | Sí | RUN/RUT del docente |
| `fullName` | `string` | Sí | Nombre completo |
| `email` | `string` | Sí | Email válido |
| `professionalTitle` | `string` | Sí | Título profesional (ej: `"Ingeniero de Software"`) |
| `academicDegree` | `string` | Sí | Grado académico (ej: `"Magíster en Ciencias"`) |

```json
{
  "run": "11222333-4",
  "fullName": "María Soto Riquelme",
  "email": "maria.soto@instituto.cl",
  "professionalTitle": "Ingeniera Civil en Informática",
  "academicDegree": "Magíster en Ciencias de la Computación"
}
```

#### `TeacherUpdateDto` (Request)
| Campo | Tipo | Obligatorio | Descripción |
| :--- | :--- | :--- | :--- |
| `run` | `string` | No | RUN |
| `fullName` | `string` | No | Nombre |
| `email` | `string` | No | Email válido |
| `professionalTitle` | `string` | No | Título |
| `academicDegree` | `string` | No | Grado |

```json
{
  "run": "11222333-4",
  "fullName": "María Angélica Soto Riquelme",
  "email": "maria.soto@instituto.cl",
  "professionalTitle": "Ingeniera Civil en Computación",
  "academicDegree": "Doctora en Computación"
}
```

#### `TeacherResponseDto` (Response)
```json
{
  "id": 5,
  "run": "11222333-4",
  "fullName": "María Soto Riquelme",
  "email": "maria.soto@instituto.cl",
  "professionalTitle": "Ingeniera Civil en Informática",
  "academicDegree": "Magíster en Ciencias de la Computación",
  "status": "ACTIVE"
}
```

---

### 3.7 Horarios y Secciones (`SectionSchedule` y `Section`)

#### `SectionScheduleDto` (Estructura interna de horarios)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `day` | `string` | Sí | Expresión regular `^[LMWJV]$` (`"L"`: Lunes, `"M"`: Martes, `"W"`: Miércoles, `"J"`: Jueves, `"V"`: Viernes) |
| `moduleNumber` | `number (int)` | Sí | Módulo de clase del `1` al `6` (M1 a M6) |

```json
{
  "day": "L",
  "moduleNumber": 1
}
```

#### `SectionCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `code` | `string` | Sí | Código de sección (ej: `"SEC-01"`) |
| `courseCode` | `string` | Sí | Código de la asignatura (ej: `"INF102"`) |
| `periodCode` | `string` | Sí | Código del período (ej: `"2026-1"`) |
| `teacherId` | `number (long)` | Sí | ID del docente a cargo |
| `capacity` | `number (int)` | Sí | Capacidad de cupos (`1` a `60`) |
| `schedules` | `SectionScheduleDto[]`| Sí | Mínimo 1 bloque horario no vacío |

```json
{
  "code": "INF102-SEC01",
  "courseCode": "INF102",
  "periodCode": "2026-1",
  "teacherId": 5,
  "capacity": 40,
  "schedules": [
    {
      "day": "L",
      "moduleNumber": 1
    },
    {
      "day": "W",
      "moduleNumber": 2
    }
  ]
}
```

#### `SectionUpdateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `teacherId` | `number (long)` | No | Nuevo ID del docente |
| `capacity` | `number (int)` | No | Capacidad (`1` a `60`) |
| `schedules` | `SectionScheduleDto[]`| No | Nueva lista de horarios |

```json
{
  "teacherId": 5,
  "capacity": 45,
  "schedules": [
    {
      "day": "L",
      "moduleNumber": 2
    }
  ]
}
```

#### `SectionResponseDto` (Response)
```json
{
  "id": 12,
  "code": "INF102-SEC01",
  "courseCode": "INF102",
  "periodCode": "2026-1",
  "teacherId": 5,
  "capacity": 40,
  "schedules": [
    {
      "day": "L",
      "moduleNumber": 1
    },
    {
      "day": "W",
      "moduleNumber": 2
    }
  ]
}
```

---

### 3.8 Matrícula Anual / Semestral (`Enrollment`)

#### `EnrollmentCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `studentRun` | `string` | Sí | RUN del estudiante |
| `periodCode` | `string` | Sí | Código del período académico abierto |

```json
{
  "studentRun": "12345678-9",
  "periodCode": "2026-1"
}
```

#### `EnrollmentResponseDto` (Response)
| Campo | Tipo | Formato / Valores |
| :--- | :--- | :--- |
| `id` | `number (long)` | Identificador de matrícula |
| `studentRun` | `string` | RUN del alumno |
| `periodCode` | `string` | Período académico |
| `enrollmentDate` | `string (date)` | Formato ISO `YYYY-MM-DD` |
| `status` | `string` | `EnrollmentStatus` (`"ACTIVE"`, `"SUSPENDED"`, etc.) |

```json
{
  "id": 20,
  "studentRun": "12345678-9",
  "periodCode": "2026-1",
  "enrollmentDate": "2026-03-01",
  "status": "ACTIVE"
}
```

---

### 3.9 Inscripción de Asignaturas / Toma de Ramos (`Registration`)

#### `RegistrationCreateDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `studentRun` | `string` | Sí | RUN del alumno |
| `sectionCode` | `string` | Sí | Código de la sección |
| `periodCode` | `string` | Sí | Código del período académico abierto |

```json
{
  "studentRun": "12345678-9",
  "sectionCode": "INF102-SEC01",
  "periodCode": "2026-1"
}
```

#### `RegistrationResponseDto` (Response)
| Campo | Tipo | Formato / Valores |
| :--- | :--- | :--- |
| `id` | `number (long)` | ID de inscripción |
| `studentRun` | `string` | RUN del estudiante |
| `courseCode` | `string` | Código de asignatura |
| `sectionCode` | `string` | Código de sección |
| `periodCode` | `string` | Período académico |
| `registrationDate` | `string (date-time)`| Formato ISO `YYYY-MM-DDTHH:mm:ss` |
| `finalGrade` | `number (double)` o `null` | Nota final (1.0 a 7.0), null si aún no se cierra/califica |
| `status` | `string` | `RegistrationStatus` (`"REGISTERED"`, `"CANCELED"`) |

```json
{
  "id": 55,
  "studentRun": "12345678-9",
  "courseCode": "INF102",
  "sectionCode": "INF102-SEC01",
  "periodCode": "2026-1",
  "registrationDate": "2026-03-05T10:15:30",
  "finalGrade": null,
  "status": "REGISTERED"
}
```

---

### 3.10 Calificaciones (`Grade`)

#### `GradeEntryDto` (Request)
| Campo | Tipo | Obligatorio | Validaciones / Descripción |
| :--- | :--- | :--- | :--- |
| `registrationId`| `number (long)` | Sí | ID de la inscripción (`registration.id`) |
| `finalGrade` | `number (double)`| Sí | Rango decimal permitido: `1.0` a `7.0` |

```json
{
  "registrationId": 55,
  "finalGrade": 5.8
}
```

---

### 3.11 Cierre Semestral (`SemesterClosing`)

#### `SemesterClosingResponseDto` (Response)
```json
{
  "periodCode": "2026-1",
  "totalRegistrationsProcessed": 120,
  "studentsGraduatedCount": 5,
  "studentsDroppedCount": 2,
  "message": "Cierre de semestre ejecutado con éxito"
}
```

---

## 4. Documentación Completa de Endpoints por Controlador

---

### 4.1 Períodos Académicos (`/api/periods`)
**Controlador:** `AcademicPeriodController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/periods` | `ADMIN` | `AcademicPeriodCreateDto` | Ninguno | `201 CREATED` -> `AcademicPeriodResponseDto` |
| `GET` | `/api/periods/{code}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `code`: `string` | `200 OK` -> `AcademicPeriodResponseDto` |
| `GET` | `/api/periods` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | Ninguno | `200 OK` -> `AcademicPeriodResponseDto[]` |

---

### 4.2 Carreras (`/api/careers`)
**Controlador:** `CareerController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/careers` | `ADMIN` | `CareerCreateDto` | Ninguno | `201 CREATED` -> `CareerResponseDto` |
| `GET` | `/api/careers` | `ADMIN` | Ninguno | Ninguno | `200 OK` -> `CareerResponseDto[]` |
| `GET` | `/api/careers/{code}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `code`: `string` | `200 OK` -> `CareerResponseDto` |
| `PUT` | `/api/careers/{code}` | `ADMIN` | `CareerUpdateDto` | `code`: `string` | `200 OK` -> `CareerResponseDto` |
| `DELETE` | `/api/careers/{code}` | `ADMIN` | Ninguno | `code`: `string` | `204 NO CONTENT` |

---

### 4.3 Planes de Estudio (`/api/study-plans`)
**Controlador:** `StudyPlanController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/study-plans` | `ADMIN` | `StudyPlanCreateDto` | Ninguno | `201 CREATED` -> `StudyPlanResponseDto` |
| `GET` | `/api/study-plans/career/{careerCode}` | `ADMIN` | Ninguno | `careerCode`: `string` | `200 OK` -> `StudyPlanResponseDto[]` |
| `GET` | `/api/study-plans/{code}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `code`: `string` | `200 OK` -> `StudyPlanResponseDto` |
| `PUT` | `/api/study-plans/{planCode}/set-current` | `ADMIN` | Ninguno | `planCode`: `string` | `200 OK` -> `StudyPlanResponseDto` |
| `DELETE` | `/api/study-plans/{planCode}` | `ADMIN` | Ninguno | `planCode`: `string` | `204 NO CONTENT` |

---

### 4.4 Asignaturas (`/api/courses`)
**Controlador:** `CourseController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/courses` | `ADMIN` | `CourseCreateDto` | Ninguno | `201 CREATED` -> `CourseResponseDto` |
| `PUT` | `/api/courses/{studyPlanCode}/{code}` | `ADMIN` | `CourseUpdateDto` | `studyPlanCode`, `code` | `200 OK` -> `CourseResponseDto` |
| `GET` | `/api/courses/{studyPlanCode}/{code}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `studyPlanCode`, `code` | `200 OK` -> `CourseResponseDto` |
| `GET` | `/api/courses/plan/{studyPlanCode}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `studyPlanCode` | `200 OK` -> `CourseResponseDto[]` |
| `DELETE` | `/api/courses/{studyPlanCode}/{code}` | `ADMIN` | Ninguno | `studyPlanCode`, `code` | `204 NO CONTENT` |

---

### 4.5 Estudiantes (`/api/students`)
**Controlador:** `StudentController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/students` | `ADMIN` | `StudentCreateDto` | Ninguno | `201 CREATED` -> `StudentResponseDto` |
| `PUT` | `/api/students/{id}` | `ADMIN` | `StudentUpdateDto` | `id`: `number (long)` | `200 OK` -> `StudentResponseDto` |
| `GET` | `/api/students/{id}` | `ADMIN`, `TEACHER` | Ninguno | `id`: `number (long)` | `200 OK` -> `StudentResponseDto` |
| `GET` | `/api/students/run/{run}` | `ADMIN`, `TEACHER` | Ninguno | `run`: `string` | `200 OK` -> `StudentResponseDto` |
| `GET` | `/api/students/email/{email}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `email`: `string` | `200 OK` -> `StudentResponseDto` |
| `GET` | `/api/students` | `ADMIN` | Ninguno | Ninguno | `200 OK` -> `StudentResponseDto[]` |
| `DELETE` | `/api/students/{id}` | `ADMIN` | Ninguno | `id`: `number (long)` | `204 NO CONTENT` |

---

### 4.6 Docentes (`/api/teachers`)
**Controlador:** `TeacherController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/teachers` | `ADMIN` | `TeacherCreateDto` | Ninguno | `201 CREATED` -> `TeacherResponseDto` |
| `GET` | `/api/teachers` | `ADMIN` | Ninguno | `status` (Query, Opcional): `TeacherStatus` | `200 OK` -> `TeacherResponseDto[]` |
| `GET` | `/api/teachers/{id}` | `ADMIN`, `TEACHER` | Ninguno | `id`: `number (long)` | `200 OK` -> `TeacherResponseDto` |
| `PUT` | `/api/teachers/{id}` | `ADMIN` | `TeacherUpdateDto` | `id`: `number (long)` | `200 OK` -> `TeacherResponseDto` |
| `PATCH`| `/api/teachers/{id}/status` | `ADMIN` | Ninguno | `id` (Path), `status` (Query): `TeacherStatus` | `200 OK` -> `TeacherResponseDto` |
| `DELETE`| `/api/teachers/{id}` | `ADMIN` | Ninguno | `id`: `number (long)` | `204 NO CONTENT` |

---

### 4.7 Secciones y Horarios (`/api/sections`)
**Controlador:** `SectionController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/sections` | `ADMIN` | `SectionCreateDto` | Ninguno | `201 CREATED` -> `SectionResponseDto` |
| `PUT` | `/api/sections/{id}` | `ADMIN` | `SectionUpdateDto` | `id`: `number (long)` | `200 OK` -> `SectionResponseDto` |
| `GET` | `/api/sections/{id}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `id`: `number (long)` | `200 OK` -> `SectionResponseDto` |
| `GET` | `/api/sections/code/{code}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `code`: `string` | `200 OK` -> `SectionResponseDto` |
| `GET` | `/api/sections/period/{periodCode}` | `ADMIN`, `TEACHER`, `STUDENT` | Ninguno | `periodCode`: `string` | `200 OK` -> `SectionResponseDto[]` |
| `DELETE` | `/api/sections/{id}` | `ADMIN` | Ninguno | `id`: `number (long)` | `204 NO CONTENT` |

---

### 4.8 Matrícula (`/api/enrollments`)
**Controlador:** `EnrollmentController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/enrollments` | `ADMIN`, `STUDENT` | `EnrollmentCreateDto` | Ninguno | `201 CREATED` -> `EnrollmentResponseDto` |
| `GET` | `/api/enrollments/period/{periodCode}` | `ADMIN` | Ninguno | `periodCode`: `string` | `200 OK` -> `EnrollmentResponseDto[]` |
| `GET` | `/api/enrollments/student/{run}` | `ADMIN`, `STUDENT` | Ninguno | `run`: `string` | `200 OK` -> `EnrollmentResponseDto[]` |

---

### 4.9 Inscripción de Asignaturas / Toma de Ramos (`/api/registrations`)
**Controlador:** `RegistrationController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/registrations` | `ADMIN`, `STUDENT` | `RegistrationCreateDto` | Ninguno | `201 CREATED` -> `RegistrationResponseDto` |
| `DELETE` | `/api/registrations/{id}` | `ADMIN`, `STUDENT` | Ninguno | `id`: `number (long)` | `204 NO CONTENT` |
| `GET` | `/api/registrations/student/{run}` | `ADMIN`, `STUDENT`, `TEACHER` | Ninguno | `run` (Path), `periodCode` (Query obligatorio): `string` | `200 OK` -> `RegistrationResponseDto[]` |
| `GET` | `/api/registrations/section/{sectionCode}` | `ADMIN`, `TEACHER` | Ninguno | `sectionCode` (Path), `periodCode` (Query obligatorio): `string` | `200 OK` -> `RegistrationResponseDto[]` |

---

### 4.10 Calificaciones (`/api/grades`)
**Controlador:** `GradeController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `PUT` | `/api/grades` | `ADMIN`, `TEACHER` | `GradeEntryDto` | Ninguno | `200 OK` -> `RegistrationResponseDto` |
| `GET` | `/api/grades/section/{sectionCode}` | `ADMIN`, `TEACHER` | Ninguno | `sectionCode` (Path), `periodCode` (Query obligatorio): `string` | `200 OK` -> `RegistrationResponseDto[]` |

---

### 4.11 Cierre Semestral (`/api/semester-closing`)
**Controlador:** `SemesterClosingController`

| Método | Endpoint | Roles Permitidos | Request Body | Query / Path Params | Response (Código HTTP) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/semester-closing/{periodCode}` | `ADMIN` | Ninguno | `periodCode`: `string` | `200 OK` -> `SemesterClosingResponseDto` |
