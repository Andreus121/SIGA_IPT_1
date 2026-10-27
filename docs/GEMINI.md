# SIGA IPT - Sistema Integrado de Gestión Académica

Documento de memoria técnica y contexto para continuidad de desarrollo en cualquier dispositivo o entorno.

---

## 1. Contexto General del Proyecto

- **Institución:** Instituto Profesional de Tecnología (IPT).
- **Carreras técnicas impartidas:** Duración de 2 años (4 semestres académicos).
  1. *Técnico Analista Programador* (TAP, iniciada el 2000, 50 vacantes/año, ~108 estudiantes).
  2. *Técnico en Ciberseguridad* (TCS, iniciada el 2012, 50 vacantes/año, ~105 estudiantes).
  3. *Técnico en Análisis de Datos* (TAD, iniciada el 2018, 50 vacantes/año, ~102 estudiantes).
- **Total inicial:** 315 estudiantes, 25 docentes.
- **Objetivo:** Reemplazar planillas y documentos no integrados con el sistema web monolítico **SIGA IPT** para centralizar la gestión académica.

---

## 2. Decisiones Arquitectónicas y Restricciones Obligatorias

### 2.1. Base de Datos Sin Llaves Foráneas (No FKs)
> **REGLA CRUCIAL DE ARQUITECTURA:**
> La base de datos relacional (PostgreSQL) **NO utiliza llaves foráneas (`FOREIGN KEY`)**.
> **Razón:** El sistema está diseñado para migrarse en el futuro a una arquitectura basada en **microservicios**, donde cada dominio tendrá su propia base de datos independiente.
> **Consecuencia directa:** La integridad referencial, validaciones de existencia, restricciones de eliminación y coherencia entre entidades **deben ser estrictamente validadas y gestionadas en la capa de Servicios (`Service`)** de Spring Boot.

### 2.2. Seguridad e Identidad (IAM)
- **Keycloak** como gestor de identidades y accesos (IAM).
- **Prohibido:** Implementar autenticación propia o almacenar contraseñas de usuarios dentro de la base de datos de la aplicación.
- **Backend:** Spring Security + `spring-boot-starter-security-oauth2-resource-server` para validar tokens JWT emitidos por Keycloak.
- **Roles:** `ADMIN` (Administrador Académico), `TEACHER` (Docente), `STUDENT` (Estudiante).

> **Nota:** Se eliminó la entidad `UserEntity` y el enum `Role`; la autenticación y autorización se gestionan exclusivamente mediante Keycloak, extrayendo los roles del claim `realm_access.roles` del JWT.

### 2.3. Estándares de Código
- **Idioma del código fuente:** Estrictamente en **inglés** (nombres de clases, métodos, variables, atributos y DTOs). Mensajes de validación o excepciones dirigidos a los usuarios pueden estar en español.
- **Arquitectura:** Monolito por capas clásico:
  - `entity` (Entidades JPA)
  - `repository` (Interfaces Spring Data JPA)
  - `service` (Lógica de negocio y validación de reglas)
  - `controller` (Controladores REST)
  - `dto` (Data Transfer Objects con validaciones Bean Validation `@Valid`)

### 2.4. Calidad y Pruebas
- **Pruebas Unitarias:** Cobertura de líneas de código en la capa `Service` mayor o igual al **90%** (medida con JaCoCo).
- **Pruebas de Integración:** Mínimo 3 procesos relevantes del sistema, cada uno con 3 escenarios exitosos y 3 escenarios de rechazo (mínimo 18 casos de prueba en total).

### 2.5. Frontend y Despliegue
- **Frontend:** ReactJS con Vite, componentes funcionales y Hooks, Axios, React Router, librería de componentes (Bootstrap, Tailwind, MUI u otra).
- **Despliegue Producción:** Docker Compose con:
  - PostgreSQL (con **volumen persistente** obligatorio).
  - 3 réplicas del backend.
  - Balanceador de carga Nginx para las réplicas del backend.
  - 1 instancia del frontend.
  - Variables de entorno obligatorias (sin credenciales hardcodeadas).
  - Pipeline de Continuous Delivery (CD) en GitHub Actions hacia Docker Hub.

---

## 3. Resumen de las 7 Épicas y Reglas de Negocio

### Épica 1: Gestión de Estudiantes
- **Datos:** RUN, nombre completo, correo electrónico, carrera (`careerCode`), plan de estudios (`studyPlanCode`), estado (`status`).
- **Estados:** `REGULAR`, `POSTPONEMENT`, `TEMPORARY_WITHDRAWAL`, `GRADUATED`, `DROPPED`.
- **Reglas:**
  - RUN y Email únicos en el sistema.
  - Al crearse, queda automáticamente asociado al plan de estudio vigente de la carrera y en estado `REGULAR`.
  - La carrera y el plan solo pueden cambiarse si el estudiante **no posee inscripciones ni historial académico**.
  - No se puede cambiar a una carrera `INACTIVA`.
  - Estados `GRADUATED` y `DROPPED` **no pueden asignarse manualmente** (se determinan en el cierre de semestre).
  - Estados `REGULAR`, `POSTPONEMENT` y `TEMPORARY_WITHDRAWAL` pueden ser asignados por el `ADMIN`.
  - Un estudiante con inscripciones o historial académico **no puede ser eliminado físicamente**.

### Épica 2: Gestión de Carreras y Planes de Estudio
- **Carreras:** Código único, nombre, descripción, estado (`ACTIVE`, `INACTIVE`), duración 4 semestres.
  - No puede eliminarse físicamente si tiene estudiantes o planes de estudio asociados.
  - Una carrera inactiva no puede recibir nuevos estudiantes.
- **Planes de Estudio:** Código, código de carrera, nombre/versión, estado (`CURRENT`, `NOT_CURRENT`).
  - Solo puede existir **un plan vigente (`CURRENT`) por carrera a la vez**.
  - Al marcar uno como vigente, el anterior pasa automáticamente a `NOT_CURRENT`.
  - No puede eliminarse físicamente si tiene estudiantes asociados (ni asignaturas).

### Épica 3: Gestión de Asignaturas y Prerrequisitos
- **Datos Asignatura:** Código (único dentro de su plan de estudios), plan de estudio, nombre, semestre (1 al 4), TEL (horas Teoría, Ejercicios, Laboratorio), SCT (créditos).
- **Reglas:**
  - SCT debe ser mayor a 0 y menor o igual a 7 ($1 \le SCT \le 7$).
  - Horas TEL no negativas y la suma total debe ser estrictamente menor a 8 ($TEL < 8$, máx 7 horas).
  - Entre 0 y 3 prerrequisitos, deben ser asignaturas del **mismo plan** y de **semestres estrictamente anteriores**.
  - No puede eliminarse si forma parte del historial académico o si es prerrequisito de otra asignatura activa.
  - Modificar una asignatura no debe alterar registros históricos.

### Épica 4: Gestión de Docentes *(ESTADO ACTUAL)*
- **Datos:** RUN, nombre completo, correo electrónico, título profesional, grado académico, estado (`ACTIVE`, `INACTIVE`).
- **Reglas:**
  - RUN y correo únicos en el sistema.
  - Todos los campos son obligatorios al registrar.
  - Inicia automáticamente en estado `ACTIVE`.
  - Solo docentes `ACTIVE` pueden asignarse a secciones.
  - Un docente con secciones o historial académico no puede eliminarse físicamente.
  - Un docente **no puede pasar a `INACTIVE` si tiene secciones asignadas en un período académico `ABIERTO`**.
  - Cambiar su estado no altera la información histórica ni asignaciones pasadas.

### Épica 5: Gestión de Oferta Académica y Secciones *(Pendiente)*
- Período académico (ej: 2027-1, 2027-2) con estado `OPEN` o `CLOSED`. Inicia `OPEN`.
- Cada asignatura ofrecida tiene una única sección por período académico.
- Docente asignado debe estar `ACTIVE`.
- Capacidad: mayor a 0 y máximo 60 estudiantes.
- Horarios: Módulos M1 a M6 en días L, M, W, J, V. La cantidad de bloques debe coincidir con las horas TEL ($1 \text{ bloque} = 2 \text{ horas pedagógicas}$).
- Sin bloques duplicados y sin conflicto de horario para el docente.
- No eliminable si tiene estudiantes inscritos.

### Épica 6: Inscripción Académica *(Pendiente)*
- Estudiante debe estar `REGULAR` y matriculado en el período académico.
- Solo asignaturas de su plan de estudios ofrecidas en el período.
- Período académico debe estar `OPEN`.
- Asignatura aprobada no puede reinscribirse. Reprobada una vez sí.
- Prerrequisitos deben estar aprobados.
- Mínimo 3 y máximo 6 asignaturas por período (excepto si restan menos de 3 para egresar).
- Prioridad de semestres inferiores (no saltar niveles si hay pendientes ofrecidas).
- Control de cupos y control de tope de horarios del estudiante.

### Épica 7: Registro de Calificaciones Finales y Cierre de Semestre *(Pendiente)*
- Notas finales entre 1.0 y 7.0 ingresadas por el docente de la sección. $\ge 4.0$ Aprobado, $< 4.0$ Reprobado.
- Período debe estar `OPEN` para registrar/modificar notas.
- Cierre de semestre: Solo si todas las inscripciones tienen nota final.
- Durante el cierre se consolida en historial académico.
- Si reprueba por 2da vez una misma asignatura $\rightarrow$ pasa a `DROPPED` (Eliminado).
- Si aprueba todo el plan $\rightarrow$ pasa a `GRADUATED` (Egresado).
- Al finalizar el cierre, el período pasa a `CLOSED`.

---

## 4. Estructura Actual del Backend

```
backend/
├── pom.xml
├── src/main/
│   ├── java/cl/siga/siga_ipt/
│   │   ├── SigaIptApplication.java
│   │   ├── controller/
│   │   │   ├── CareerController.java
│   │   │   ├── CourseController.java
│   │   │   ├── StudentController.java
│   │   │   └── StudyPlanController.java
│   │   │   <!-- Falta TeacherController.java -->
│   │   ├── dto/
│   │   │   ├── CareerCreateDto.java / CareerResponseDto.java / CareerUpdateDto.java
│   │   │   ├── CourseCreateDto.java / CourseResponseDto.java / CourseUpdateDto.java
│   │   │   ├── StudentCreateDto.java / StudentResponseDto.java / StudentUpdateDto.java
│   │   │   ├── StudyPlanCreateDto.java / StudyPlanResponseDto.java
│   │   │   └── TeacherCreateDto.java / TeacherResponseDto.java / TeacherUpdateDto.java
│   │   ├── entity/
│   │   │   ├── AcademicHistoryEntity.java
│   │   │   ├── AcademicPeriodEntity.java
│   │   │   ├── CareerEntity.java
│   │   │   ├── CourseEntity.java
│   │   │   ├── EnrollmentEntity.java
│   │   │   ├── PrerequisiteEntity.java
│   │   │   ├── RegistrationEntity.java
│   │   │   ├── SectionEntity.java
│   │   │   ├── SectionScheduleEntity.java
│   │   │   ├── StudentEntity.java
│   │   │   ├── StudyPlanEntity.java
│   │   │   ├── TeacherEntity.java
│   │   │   └── UserEntity.java
│   │   ├── enums/
│   │   │   ├── AcademicStatus.java
│   │   │   ├── CareerStatus.java
│   │   │   ├── EnrollmentStatus.java
│   │   │   ├── PeriodStatus.java
│   │   │   ├── RegistrationStatus.java
│   │   │   ├── Role.java
│   │   │   ├── StudentStatus.java
│   │   │   ├── StudyPlanStatus.java
│   │   │   └── TeacherStatus.java
│   │   ├── repository/
│   │   │   └── (Interfaces JpaRepository para cada entidad)
│   │   └── service/
│   │       ├── CareerService.java
│   │       ├── CourseService.java
│   │       ├── StudentService.java
│   │       ├── StudyPlanService.java
│   │       └── TeacherService.java
│   └── resources/
│       └── application.properties
└── src/test/
    └── java/cl/siga/siga_ipt/
        └── SigaIptApplicationTests.java
```

---

## 5. Instrucciones para Continuar en Otro Dispositivo

1. **Requisitos de Sistema:**
   - JDK 21 (ej: Microsoft OpenJDK 21 o Eclipse Temurin 21).
   - Docker Desktop operativo.
   - Maven (se incluye `mvnw` y `mvnw.cmd`).
2. **Levantar Base de Datos:**
   ```bash
   docker-compose up -d
   ```
3. **Compilar y Ejecutar Backend:**
   ```bash
   # En Windows PowerShell
   $env:JAVA_HOME="<ruta_al_jdk_21>"
   cd backend
   .\mvnw.cmd clean compile
   .\mvnw.cmd spring-boot:run
   ```
4. **Documentos de Referencia:**
   - Consultar `docs/Analisis.md` para reglas de negocio detalladas.
   - Consultar `docs/Evaluacion1_v1.pdf` para la rúbrica oficial de la evaluación.
   - Consultar `docs/Errores.md` para el listado de correcciones pendientes antes de pasar a la Épica 5.

## Estado Actual y Próximos Pasos

### Cambios y Hitos Completados
- `SectionEntity.java`: restricción única compuesta (`course_code`, `period_code`), eliminado `periodStatus` y su import.
- Arquitectura sin Foreign Keys, `GlobalExceptionHandler` y `SecurityConfig` (Keycloak OAuth2 Resource Server) totalmente operativos.
- **Épica 5 (Gestión de Oferta Académica y Secciones) COMPLETADA:**
  - `SectionScheduleEntity` y `SectionScheduleRepository` para gestión de bloques horarios.
  - DTOs implementados: `SectionScheduleDto`, `SectionCreateDto`, `SectionUpdateDto` y `SectionResponseDto`.
  - `SectionService` con validaciones de negocio:
    - Período académico abierto (`PeriodStatus.OPEN`).
    - Docente asignado activo (`TeacherStatus.ACTIVE`) y sin conflicto de horario en el período.
    - Cupos ($1 \le \text{capacidad} \le 60$).
    - Bloques de horario (módulos M1 a M6 en L-V) y correspondencia con horas TEL ($1 \text{ bloque} = 2 \text{ horas pedagógicas}$).
    - Restricción de eliminación si existen estudiantes inscritos.
  - `SectionController` con endpoints REST protegidos por Spring Security.
  - **Pruebas Unitarias de Services (COMPLETADO):**
    - 130 pruebas unitarias implementadas con JUnit 5, Mockito y AssertJ sin errores.
    - Cobertura del paquete `service` en JaCoCo: **92% de instrucciones** y **97% de líneas** cubiertas (supera el requisito de $\ge 90\%$).
- **Épica 6 (Inscripción Académica) COMPLETADA:**
  - `EnrollmentEntity` y `EnrollmentRepository` para matrícula por período.
  - `RegistrationEntity` y `RegistrationRepository` para toma de asignaturas.
  - DTOs: `EnrollmentCreateDto`, `EnrollmentResponseDto`, `RegistrationCreateDto`, `RegistrationResponseDto`.
  - `EnrollmentService` y `RegistrationService` con todas las validaciones de negocio.
  - `EnrollmentController` y `RegistrationController` con endpoints REST protegidos.
  - Pruebas unitarias: `EnrollmentServiceTest` (8 tests) y `RegistrationServiceTest` (14 tests).
- **Épica 7 (Registro de Calificaciones Finales y Cierre de Semestre) COMPLETADA:**
  - `GradeEntryDto` y `SemesterClosingResponseDto`.
  - `GradeService` con validaciones de notas ($1.0 \le \text{nota} \le 7.0$), estado de inscripción y período `OPEN`.
  - `SemesterClosingService` con proceso transaccional de cierre:
    - Validación de notas completas en inscripciones del período.
    - Consolidación en `AcademicHistoryEntity` con estados `PASSED` / `FAILED`.
    - Detección de doble reprobación para pase a estado `DROPPED`.
    - Detección de aprobación curricular completa para pase a estado `GRADUATED`.
    - Transición del período académico a estado `CLOSED`.
  - `GradeController` y `SemesterClosingController` con endpoints REST protegidos con `@PreAuthorize`.
  - Pruebas unitarias: `GradeServiceTest` (7 tests) y `SemesterClosingServiceTest` (5 tests).

- **Pruebas de Integración (COMPLETADO):**
  - Configuración con base de datos H2 en memoria (`mode=PostgreSQL`) en perfil `test` y `@AutoConfigureMockMvc` con Spring Security (`@WithMockUser`).
  - Total de 3 procesos y 18 casos de prueba (9 exitosos, 9 de rechazo) implementados y aprobados:
    1. **Proceso 1: Oferta de Secciones y Horarios** ([`SectionOfferingIntegrationTest.java`](file:///c:/Users/jcand/Documents/SIGA_IPT_1/backend/src/test/java/cl/siga/siga_ipt/integration/SectionOfferingIntegrationTest.java)):
       - 3 exitosos: Creación de sección con docente activo y horarios, modificación de capacidad, consulta de secciones por período.
       - 3 rechazos: Período cerrado, docente inactivo, tope de horario docente.
    2. **Proceso 2: Matrícula e Inscripción de Asignaturas** ([`EnrollmentRegistrationIntegrationTest.java`](file:///c:/Users/jcand/Documents/SIGA_IPT_1/backend/src/test/java/cl/siga/siga_ipt/integration/EnrollmentRegistrationIntegrationTest.java)):
       - 3 exitosos: Matrícula de estudiante regular en período abierto, inscripción de asignatura con prerrequisitos cumplidos, anulación de ramo (`dropCourse`) en período abierto.
       - 3 rechazos: Estudiante sin matrícula activa, prerrequisito no aprobado, tope de horario entre asignaturas del estudiante.
    3. **Proceso 3: Calificaciones y Cierre de Semestre** ([`GradeClosingIntegrationTest.java`](file:///c:/Users/jcand/Documents/SIGA_IPT_1/backend/src/test/java/cl/siga/siga_ipt/integration/GradeClosingIntegrationTest.java)):
       - 3 exitosos: Ingreso de calificación final válida (5.5), cierre de semestre con graduación de estudiante (`GRADUATED`), transición del período a `CLOSED`.
       - 3 rechazos: Calificación fuera de rango (> 7.0), ingreso/modificación de notas en período cerrado, cierre de semestre con asignaturas sin calificación registrada.
  - Total general de pruebas ejecutadas: **149 pruebas (130 unitarias + 18 de integración + 1 de contexto)** todas pasando sin fallos.
  - Cobertura global de JaCoCo en `service`: **92% de instrucciones** y **97% de líneas** cubiertas.

- **Despliegue y Contenedorización (EN CURSO):**
  - Creado `nginx.conf` con upstream balanceado hacia el backend (`http://backend:8080`) en puerto 80.
  - Actualizado `docker-compose.yml`:
    - PostgreSQL persistente (puerto 5090).
    - Keycloak 24 (puerto 8090).
    - Backend Spring Boot configurado para 3 réplicas (`deploy.replicas: 3`) sin nombre de contenedor fijo para evitar conflictos de Compose v2.
    - Nginx como reverse proxy / balanceador de carga.
  - Creado [`backend/Dockerfile`](file:///c:/Users/jcand/Documents/SIGA_IPT_1/backend/Dockerfile) multi-stage (eclipse-temurin:21-jdk-alpine builder + jre-alpine runtime) con paths relativos al contexto del backend corregidos.

### Próximo Paso Inmediato
- Probar el levantamiento del clúster con `docker compose up -d --build` para validar el empaquetado del backend y la orquestación.
- Continuar con la capa Frontend en React + Vite.
