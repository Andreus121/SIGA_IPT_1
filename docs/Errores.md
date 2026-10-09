# Reporte de Errores y Hallazgos en el Backend (SIGA IPT)

Este documento detalla todos los errores detectados en el backend hasta la **Épica 4 (Gestión de Docentes)**, clasificados por criticidad, con su ubicación exacta, explicación técnica y recomendación de solución.

> **Nota de Contexto Arquitectónico:**
> La base de datos **NO utiliza llaves foráneas (`FOREIGN KEY`)**, dado que el sistema migrará a microservicios. Todas las validaciones de existencia, restricciones de eliminación e integridad referencial deben ser aseguradas de manera estricta por la capa de servicios (`Service`).

---

## Índice de Contenidos

1. [Error Crítico de Compilación (Bloqueante)](#1-error-crítico-de-compilación-bloqueante)
2. [Errores Críticos de Lógica de Negocio](#2-errores-críticos-de-lógica-de-negocio)
3. [Incumplimientos de Arquitectura y Seguridad](#3-incumplimientos-de-arquitectura-y-seguridad)
4. [Componentes y Funcionalidades Faltantes (Épicas 1 a 4)](#4-componentes-y-funcionalidades-faltantes-épicas-1-a-4)
5. [Inconsistencias en Entidades y Modelo de Datos (Enfoque Sin FKs)](#5-inconsistencias-en-entidades-y-modelo-de-datos-enfoque-sin-fks)
6. [Configuración, Docker y DevOps](#6-configuración-docker-y-devops)


---

## 1. Error Crítico de Compilación (Bloqueante) **ARREGLADO**

### 1.1. Invocación de `existsByTeacherRun()` sin argumentos y bloque vacío
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/service/TeacherService.java`
- **Línea:** 166-168
- **Código actual:**
  ```java
  // Regla: No eliminar si tiene historial académico
  if(academicHistoryRepository.existsByTeacherRun()){

  }
  ```
- **Problema:**
  1. `AcademicHistoryRepository.java` declara `boolean existsByTeacherRun(String teacherRun)`. Llamar al método sin parámetros rompe la compilación (`javac` falla con código 1: *actual and formal argument lists differ in length*).
  2. El bloque `if` está vacío; no lanza ninguna excepción en caso de que existan registros.
  3. El método `deleteTeacher(Long id)` recibe un `id`, pero **no recupera la entidad `TeacherEntity`** de la base de datos para obtener su `run` y pasarlo como parámetro.
- **Solución recomendada:**
  ```java
  TeacherEntity teacher = teacherRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado con el ID: " + id));

  if (sectionRepository.existsByTeacherId(id)) {
      throw new IllegalStateException("No se puede eliminar el docente porque tiene secciones asociadas en el sistema.");
  }

  if (academicHistoryRepository.existsByTeacherRun(teacher.getRun())) {
      throw new IllegalStateException("No se puede eliminar el docente porque registra historial académico en el sistema.");
  }

  teacherRepository.delete(teacher);
  ```

---

## 2. Errores Críticos de Lógica de Negocio

### 2.1. Lógica invertida en la validación de prerrequisitos al actualizar asignatura **ARREGLADO**
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/service/CourseService.java`
- **Líneas:** 96-98 y 106-110
- **Código actual:**
  ```java
  // Línea 96:
  if (prereqCourse != null && prereqCourse.getSemester() < dto.getSemester()) {
      throw new IllegalArgumentException("El semestre de la asignatura debe ser estrictamente posterior al de sus prerrequisitos.");
  }

  // Línea 106:
  if (dependentCourse != null && dto.getSemester() < dependentCourse.getSemester()) {
      throw new IllegalArgumentException("No se puede mover la asignatura al semestre " + dto.getSemester() +
              " porque es prerrequisito de '" + dependentCourse.getCode() + "' en el semestre " + dependentCourse.getSemester() + ".");
  }
  ```
- **Problema:**
  - En la línea 96, la regla exige que el prerrequisito sea de un semestre **estrictamente anterior** (`prereqCourse.getSemester() < dto.getSemester()`). Al verificar con `<`, el código arroja error **precisamente cuando la condición es válida**, y permite el caso inválido cuando el prerrequisito está en un semestre igual o posterior.
  - En la línea 106 ocurre lo mismo: el semestre de la asignatura debe ser menor al de las asignaturas que dependen de ella (`dto.getSemester() < dependentCourse.getSemester()`). Al evaluar con `<`, prohíbe el caso correcto y permite que la asignatura se mueva a semestres iguales o superiores a sus dependientes.
- **Solución recomendada:**
  - Cambiar en línea 96: `prereqCourse.getSemester() >= dto.getSemester()`.
  - Cambiar en línea 106: `dto.getSemester() >= dependentCourse.getSemester()`.

---

### 2.2. `changeTeacherStatus` no actualiza el estado del docente **ARREGLADO**
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/service/TeacherService.java`
- **Líneas:** 134-150
- **Código actual:**
  ```java
  if (newStatus == TeacherStatus.INACTIVE) {
      if (sectionRepository.existsByTeacherIdAndPeriodStatus(id, PeriodStatus.OPEN)) {
          throw new IllegalStateException("No se puede inactivar al docente porque tiene secciones asignadas en un período académico ABIERTO.");
      }
  }
  // Si el nuevo estado es Activo solo se cambia y ya
  teacherRepository.save(teacher);
  return mapTeacherToDto(teacher);
  ```
- **Problema:**
  - Falta la asignación `teacher.setStatus(newStatus);`.
  - El método valida correctamente si puede pasar a `INACTIVE`, pero nunca aplica el nuevo estado al objeto, guardando en base de datos la entidad intacta.
- **Solución recomendada:**
  Agregar `teacher.setStatus(newStatus);` antes de `teacherRepository.save(teacher);`.

---

### 2.3. Validación incorrecta de unicidad de RUN en actualización de docente **ARREGLADO**
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/service/TeacherService.java`
- **Líneas:** 94-101
- **Código actual:**
  ```java
  if(dto.getRun()!=null && !dto.getRun().isBlank()){
      if(teacherRepository.existsByRun(dto.getRun())){
          throw new IllegalArgumentException("El run proporcionado es de otro profesor");
      }
      teacher.setRun(dto.getRun());
  }
  ```
- **Problema:**
  - `teacherRepository.existsByRun(dto.getRun())` comprueba si el RUN existe en toda la tabla. Si en la petición PUT se envía el mismo RUN que el profesor ya posee, la condición devuelve `true` y bloquea la actualización innecesariamente.
  - Ya existe el método `existsByRunAndIdNot(String run, Long id)` en `TeacherRepository`, pero no fue utilizado aquí (a diferencia del email, que sí usó `existsByEmailAndIdNot`).
- **Solución recomendada:**
  Usar `teacherRepository.existsByRunAndIdNot(dto.getRun(), id)`.

---

### 2.4. Asignación errónea de rol `Role.STUDENT` al registrar un docente **ARREGLADO**
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/service/TeacherService.java`
- **Línea:** 56
- **Código actual:**
  ```java
  .role(Role.STUDENT)
  ```
- **Problema:**
  - Al crear un docente, se le asigna el rol `STUDENT` en lugar de `TEACHER`. (Independientemente de que la entidad `User` deba ser retirada por el requerimiento de Keycloak).

---

### 2.5. Actualización de RUN de estudiante sin validación de unicidad ni riesgo de integridad referencial (Sin FK) **ARREGLADO**
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/service/StudentService.java`
- **Líneas:** 92-94
- **Código actual:**
  ```java
  if (dto.getRun()!=null && !dto.getRun().isBlank()){
      student.setRun(dto.getRun());
  }
  ```
- **Problema:**
  1. No valida si el nuevo RUN ya existe en otro estudiante (`existsByRunAndIdNot`).
  2. **Impacto Crítico sin Llaves Foráneas:** Las tablas `registrations`, `academic_histories` y `enrollments` utilizan `studentRun` como identificador de relación. Si se modifica el RUN de un estudiante que ya tiene inscripciones o notas históricas, se produce una desincronización inmediata de datos huérfanos.
- **Solución recomendada:**
  - Prohibir la modificación del RUN si el estudiante ya cuenta con inscripciones o historial académico (al igual que la carrera y el plan de estudios), o deshabilitar la edición del RUN una vez registrado el estudiante.

---

## 3. Incumplimientos de Arquitectura y Seguridad

### 3.1. Gestión propia de usuarios y almacenamiento de contraseñas en base de datos **ARREGLADO**
- **Archivos:**
  - `backend/src/main/java/cl/siga/siga_ipt/entity/UserEntity.java`
  - `backend/src/main/java/cl/siga/siga_ipt/repository/UserRepository.java`
  - `backend/src/main/java/cl/siga/siga_ipt/dto/StudentCreateDto.java` (`password`)
  - `backend/src/main/java/cl/siga/siga_ipt/dto/TeacherCreateDto.java` (`password`)
  - `StudentService.java` y `TeacherService.java`
- **Requerimiento Oficial (PDF pág. 3, sección 3.5):**
  > *"El sistema deberá implementar autenticación y autorización mediante Keycloak, como herramienta de gestión de identidades y accesos (IAM)... No se deberá implementar un mecanismo propio de autenticación ni almacenar las contraseñas de los usuarios en la base de datos de la aplicación."*
- **Problema:**
  - Se implementó una tabla `users` donde se persisten contraseñas en texto plano (`password`).
  - La especificación prohíbe explícitamente almacenar credenciales o tener un sistema de autenticación propio. Keycloak es el único proveedor de identidades autorizado.
- **Solución recomendada:**
  - Eliminar los campos `password` de `StudentCreateDto` y `TeacherCreateDto`.
  - Desacoplar `UserEntity` y `UserRepository` del flujo de negocio de estudiantes y profesores.

---

### 3.2. Falta de configuración de Spring Security (`SecurityFilterChain`) **ARREGLADO**
- **Problema:**
  - En `pom.xml` están agregadas las dependencias:
    - `spring-boot-starter-security`
    - `spring-boot-starter-security-oauth2-resource-server`
  - Sin embargo, **no existe ninguna clase `@Configuration`** para Spring Security.
  - Al iniciar la aplicación, Spring Security activa su protección por defecto, generando una contraseña aleatoria en consola y bloqueando todos los endpoints con respuestas `401 Unauthorized`.
- **Solución recomendada:**
  - Crear una clase de configuración (ej. `cl.siga.siga_ipt.config.SecurityConfig`) configurando el Resource Server para validar tokens JWT emitidos por Keycloak, o permitir temporalmente los endpoints de la API mientras se integra Keycloak.

---

### 3.3. Falta de Manejo Global de Excepciones (`@RestControllerAdvice`) **ARREGLADO**
- **Problema:**
  - Los servicios lanzan `IllegalArgumentException` e `IllegalStateException`.
  - Sin un `@RestControllerAdvice`, Spring retorna respuestas `500 Internal Server Error` con el stack trace por defecto de Tomcat.
  - La evaluación exige el uso apropiado de códigos de estado HTTP (REST):
    - Parámetros inválidos / Reglas de negocio: `400 Bad Request`
    - Recurso no encontrado: `404 Not Found`
    - Conflictos de estado (ej. carrera con estudiantes): `409 Conflict`
    - Validaciones Bean Validation (`@Valid`): `400 Bad Request` estructurado.
- **Solución recomendada:**
  - Implementar una clase `GlobalExceptionHandler` con `@RestControllerAdvice` y una estructura estándar de respuesta de error (`timestamp`, `status`, `error`, `message`, `path`).

---

## 4. Componentes y Funcionalidades Faltantes (Épicas 1 a 4)

### 4.1. Falta el controlador REST de Docentes (`TeacherController.java`)**ARREGLADO**
- **Épica:** Épica 4 (Gestión de Docentes).
- **Problema:**
  - Se crearon `TeacherEntity`, `TeacherRepository`, los DTOs y `TeacherService`, pero **no existe `TeacherController.java`**.
  - Los clientes no tienen cómo consumir ninguna operación de docentes vía HTTP REST.
- **Solución recomendada:**
  - Crear `TeacherController.java` exponiendo:
    - `POST /api/teachers` (Crear docente)
    - `GET /api/teachers` (Listar docentes, con filtro opcional por `status`)
    - `GET /api/teachers/{id}` (Obtener docente por ID)
    - `PUT /api/teachers/{id}` (Actualizar datos del docente)
    - `PATCH` o `PUT /api/teachers/{id}/status` (Cambiar estado ACTIVO/INACTIVO)
    - `DELETE /api/teachers/{id}` (Eliminar docente)

---

### 4.2. Falta la eliminación física de Estudiante (`deleteStudent`) **ARREGLADO**
- **Épica:** Épica 1 (Gestión de Estudiantes).
- **Requerimiento Oficial (PDF pág. 5):**
  > *"Un estudiante que posea inscripciones o historial académico no podrá ser eliminado físicamente del sistema."*
- **Problema:**
  - En `StudentService.java` y `StudentController.java` **no existe el método para eliminar estudiantes**.
  - No se está dando cumplimiento a la regla que permite eliminar a un estudiante siempre y cuando no tenga inscripciones ni historial.
- **Solución recomendada:**
  - Agregar `deleteStudent(Long id)` en `StudentService` validando:
    ```java
    StudentEntity student = studentRepository.findById(id)...;
    if (registrationRepository.existsByStudentRun(student.getRun()) ||
        academicHistoryRepository.existsByStudentRun(student.getRun())) {
        throw new IllegalStateException("No se puede eliminar físicamente un estudiante con inscripciones o historial académico.");
    }
    studentRepository.delete(student);
    ```
  - Exponer `DELETE /api/students/{id}` en `StudentController`.

---

### 4.3. Falta endpoint para consultar Plan de Estudio individual por código **ARREGLADO**
- **Épica:** Épica 2 (Gestión de Carreras y Planes de Estudio).
- **Problema:**
  - `StudyPlanController` solo cuenta con `GET /api/study-plans/career/{careerCode}`.
  - Falta un endpoint `GET /api/study-plans/{code}` para obtener el detalle de un plan específico.

---

## 5. Inconsistencias en Entidades y Modelo de Datos (Enfoque Sin FKs)

### 5.1. `CourseEntity.java`: Restricción de unicidad global incorrecta y sintaxis de `@Table` **ARREGLADO**
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/entity/CourseEntity.java`
- **Líneas:** 7-10 y 21
- **Código actual:**
  ```java
  @Table(name = "courses", uniqueConstraints = {
          @UniqueConstraint(name = "uk_course_code_plan", columnNames = {"code", "studyPlanCode"})
  })
  ...
  @Column(nullable = false, unique = true)
  private String code;
  ```
- **Problema:**
  1. La línea 21 marca `code` como `unique = true` a nivel global. La regla del negocio dice textualmente: *"Su código deberá ser único dentro de dicho plan."* Marcar el código único a nivel global impide que dos planes de estudio contengan asignaturas con el mismo código.
  2. En la restricción de `@Table`, el nombre de la columna para `studyPlanCode` en PostgreSQL según la convención de Hibernate por defecto es `study_plan_code`. Usar `studyPlanCode` genera conflictos en la creación del DDL de la base de datos.
- **Solución recomendada:**
  - Quitar `unique = true` del campo `code`.
  - Usar: `@UniqueConstraint(name = "uk_course_code_plan", columnNames = {"code", "study_plan_code"})`.

---

### 5.2. `StudyPlanEntity.java`: Falta restricción de unicidad en BD **ARREGLADO**
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/entity/StudyPlanEntity.java`
- **Problema:**
  - El campo `code` no tiene `unique = true` en la anotación `@Column`. Si bien el servicio lo valida, la base de datos no está protegida contra inserciones duplicadas concurrentes.

---

### 5.3. `TeacherEntity.java`: Atributos obligatorios permitiendo valores nulos **ARREGLADO**
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/entity/TeacherEntity.java`
- **Líneas:** 28-29
- **Problema:**
  - `professionalTitle` y `academicDegree` no tienen `@Column(nullable = false)`, a pesar de que la regla de negocio y el DTO indican que son campos estrictamente obligatorios.

---

### 5.4. Inconsistencia en identificadores naturales entre tablas (Crítico para Microservicios)
- **Problema:**
  - En un sistema sin FKs que migrará a microservicios, las referencias entre tablas deben usar claves naturales uniformes:
    - **Período Académico:**
      - En `AcademicPeriodEntity`: se llama `academicPeriod`.
      - En `SectionEntity`: se llama `academicPeriodCode`.
      - En `AcademicHistoryEntity`, `EnrollmentEntity`, `RegistrationEntity`, `SectionScheduleEntity`: se llama `periodCode`.
      - *Recomendación:* Estandarizar a `code` o `periodCode` en todas las entidades.
    - **Identificador de Docente:**
      - En `AcademicHistoryEntity`: se almacena `teacherRun` (String).
      - En `SectionEntity`: se almacena `Long teacherId`.
      - *Recomendación:* Usar `teacherRun` (o un UUID/código docente único) para mantener coherencia en las referencias distribuidas.

---

### 5.5. `SectionEntity.java`: Duplicación de estado de período y falta de unicidad
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/entity/SectionEntity.java`
- **Problema:**
  - Incluye `private PeriodStatus periodStatus;`. El estado del período le corresponde a `AcademicPeriodEntity`. Duplicarlo en `SectionEntity` provocará inconsistencias cuando un período se cierre.
  - La regla indica: *"Una asignatura podrá contar con una única sección durante un mismo período académico."* Falta una restricción única compuesta por `(courseCode, academicPeriodCode)`.

---

## 6. Configuración, Docker y DevOps

### 6.1. `docker-compose.yml` sin volumen persistente para PostgreSQL
- **Archivo:** `docker-compose.yml`
- **Requerimiento Oficial (PDF pág. 2, sección 3.3):**
  > *"La base de datos deberá utilizar un volumen Docker para garantizar la persistencia de los datos ante la detención o recreación del contenedor."*
- **Problema:**
  - No se definió ningún volumen persistente. Cada vez que se baje o recree el contenedor `siga_postgresql`, todos los datos y tablas se pierden.
- **Solución recomendada:**
  Agregar un volumen con nombre:
  ```yaml
  services:
    postgres-db:
      image: postgres:16-alpine
      container_name: siga_postgresql
      environment:
        POSTGRES_DB: ${DB_NAME}
        POSTGRES_USER: ${DB_USERNAME}
        POSTGRES_PASSWORD: ${DB_PASSWORD}
      ports:
        - "5090:5432"
      volumes:
        - postgres_data:/var/lib/postgresql/data

  volumes:
    postgres_data:
  ```

---

### 6.2. `application.properties` con credenciales quemadas en código
- **Archivo:** `backend/src/main/resources/application.properties`
- **Requerimiento Oficial (PDF pág. 2, sección 3.3):**
  > *"La configuración de la aplicación deberá realizarse mediante variables de entorno, evitando incorporar credenciales, contraseñas u otros datos sensibles directamente en el código fuente."*
- **Problema:**
  - Valores hardcodeados: `localhost:5090`, `admin`, `password1234`.
- **Solución recomendada:**
  Parametrizar con variables de entorno y valores por defecto:
  ```properties
  spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5090/SigaIPT_DB?sslmode=disable}
  spring.datasource.username=${DB_USERNAME:admin}
  spring.datasource.password=${DB_PASSWORD:password1234}
  ```

---

### 6.3. Falta de plugin JaCoCo y Pruebas Unitarias
- **Requerimiento Oficial (PDF pág. 3, sección 3.4 y 3.5):**
  > *"Pruebas unitarias: La capa de servicios (lógica de negocio) deberá contar con pruebas unitarias y alcanzar una cobertura de líneas de código mayor o igual al 90%."*
- **Problema:**
  - En `pom.xml` no está configurado `jacoco-maven-plugin`.
  - Solo existe una prueba vacía en `SigaIptApplicationTests.java`. Los servicios no tienen pruebas unitarias escritas aún.

---

### 6.4. Typo en mensaje de error de `CareerService.java`
- **Archivo:** `backend/src/main/java/cl/siga/siga_ipt/service/CareerService.java`
- **Línea:** 90
- **Texto:** `"La carrera especificada no existe,"` (termina en coma en vez de punto).

---



## Prioridad de Correcciones

**Prioridad 1 – Funcionalidad crítica**  
- Eliminación física de estudiante (`deleteStudent`) **ARREGLADO**  
- Endpoint GET de Plan de estudio individual por código (`/api/study-plans/{code}`) **ARREGLADO**  

**Prioridad 2 – Correcciones de entidad y modelo**  
- Unicidad del código de asignatura por plan (`CourseEntity`) – pendiente  
- Unicidad del código de plan de estudio a nivel DB (`StudyPlanEntity`) – pendiente  
- Campos obligatorios en `TeacherEntity` (`professionalTitle`, `academicDegree`) – pendiente  
- Consistencia de identificadores entre entidades (periodo, docente) – pendiente  
- Restricción única en `SectionEntity` (curso + período) y eliminación de duplicado de estado – pendiente  

**Prioridad 3 – Seguridad y configuración**  
- Configuración de Spring Security con Keycloak – pendiente  
- GlobalExceptionHandler – pendiente  
- Plugin JaCoCo y pruebas unitarias – pendiente
