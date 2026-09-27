# Gestión de estudiantes

**El sistema deberá permitir consultar y actualizar la información de los estudiantes, así como acceder a su historial académico (asignaturas cursadas, calificaciones, etc.)**

## Datos de un alumno

- Código
- Run
- Nombre completo
- Correo electrónico
- Carrera (código de la carrera)
- Plan de estudios (código del plan de estudio)
- Estado
    -REGULAR: habilitado para hacer sus estudios
    -POSTERGACIÓN: estudiante que detuvó/canceló sus estudios
    -RETIRO TEMPORAL: estudiante que ha interrumpido sus estudios
    -EGRESADO: estidoamte que aprobó todo  el plan de estudio
    -ELIMINADO: reprobó una asignatura dos veces

## Reglas de negocio

- Registrar un estudiante: proporciona su RUN, nombre completo, correo electrónico y carrera. Con esto se asocia el plan de estudio de esa carrera. Todo registro nuevo comienza con estado **REGULAR**
- RUN y correo deben ser **ÚNICOS** dentro del sistema
- Todo estudiante tiene carrera y un plan de estudio de la misma
- Un alumno con historial académico no puede ser eliminado del sistema
- estados **EGRESADO** y **ELIMINADO** no podrán ser asignados manualmente
- La carrera y el plan de estudios de un estudiante solo se pueden modificar mientras no tenga inscripciones ni historial académico
- los estados **REGULAR**, **POSTERGACIÓN**, **RETIRO TEMPORAL** pueden ser asignados por el **ADMIN**.

# Gestión de carreras y planes de estudio

Toda carrera tendrá una duración de dos años (4 semestres)

## Datos de una carrera

- Código de carrera
- Nombre
- Descripción
- Estado
    - ACTIIVA
    - INACTIVA
- Código de plan te estudio **Agregada por mi**

## Datos plan de estudio

- Código
- Código de carrera

## Reglas de negocio

- Código de carrera debe ser único en el sistema
- Solo puede haber **UN** plan de estudio vigente a la vez por carrera
- Una carrera **INACTIVA** **NO** puede recibir nuevos estudiantes
- Crear nuevos planes de estudio no modifica el plan de estudio de los alumnos que ingresaron con otro plan
- Si un plan de estudio está asociado a un estudiante no puede ser eliminado
- Una carrera que tenga estudiantes o planes de estudios asociados no puede ser eliminado, solo modificar su estado
- Al crear un plan de estudio nuevo y ponerlo como vigente, el otro automaticamente debe pasar a **NO VIGENTE**

# Gestión de asignaturas y prerequisitos

## Datos de una asignatura

- Código
- Nombre
- Semestre (valor del 1 al 4)
- TEL (teoría, Ejercicios y Laboratorio)
- SCT (créditos academicos)
- Prerequisitos

## Reglas de negocio

- el SCT de cada asignatura debe ser **mayor que** cero y **menor o igual** a 7
- Las horas totales (TEL) de una asignatura debe ser **menor que** 8
- Una asignatura puede tener entre 0 y 3 prerrequisitos y solo pueden ser del mismo plan de estudios
- Los prerrequisitos deben ser de semestres anteriores de la asignatura del mismo plan de estudio
- Una asignatura que forme parte del historial académico de un estudiante no puede ser eliminada del sistema
- Las modificaciones a una asignatura no modifican la información registrade en el historial académico de un estudiante

**AGREGADO POR MI (suposiciones)**
- El contexto nunca dice que una asignatura es de un plan de estudio, así que las asignaturas se pueden compartir entre planes de estudio e incluso carreras

# Gestión de docentes

## Datos de un profesor

- Código
- RUN
- Nombre completo
- Correo electrónico
- Título profesional
- Grado académico
- Estado
    - ACTIVO
    - INACTIVO


## Reglas de nogocio

- RUN y correo deben ser **ÚNICOS**
- Todo registro nuevo inicia con estado **ACTIVO**
- Solo docentes con estado **ACTIVO** se les puede asignar una seccion
- Un docente que tenga secciones asociadas no puede ser eliminado del sistema
- Un docente **NO** puede cambiar su estado a **INACTIVO** en un período académico **ABIERTO O EN CURSO**
- Un cambio de estado de un docente no cambia su información histórica
- Un profesor puede tener más de una sección por periodo académico

# Gestión de oferta académica y secciones

**Modulos**
- Lunes (L1,L2,L3,L4,L5,L6)
- Martes (M1,M2,M3,M4,M5,M6)
- Miércoles (W1,W2,W3,W4,W5,W6)
- Jueves (J1,J2,J3,J4,J5,J6)
- Viernes (V1,V2,V,V4,V5,V6)

**Horarios**
- 1 - 08:15-9:35
- 2 - 09:50-11:10
- 3 - 11:25-12:45
- 4 - 13:45-15:05
- 5 - 15:20-16:40
- 6 - 16:55-18:15

## Datos de una sección
- Código
- Código de la asignatura
- Periodo académico
- Docente
- Capacidad de estudiantes
- Horario

## Datos de periodo academico
- Periodo académico (2027-2)
- Estado
    - ABIERTO
    - CERRADO
debe registrar las matriculas de los alumnos (info utilizada en la toma de ramos)
no sé que más

## Reglas de negocio
- Al crear un perído académico tiene estado inicial **ABIERTO**
- Toda sección debe tener un docente con estado ACITVO al momento de hacer la asignación
- La capacidad de una sección debe ser **mayor que** cero y no podrá superar los 60 estudiantes
- La cantidad de estudiantes inscritos no puede superar la capacidad maxima de la sección
- Toda sección debe tener al menos un bloque de horario
- Cada sección debe tener la cantidad de bloques de horarios correspondiente a las horas TEL de la asignatura, un bloque corresponde a 2 horas pedagógicas
- Una sección no puede tener horarios duplicados
- Un docente no puede tenet asignadas dos secciones con horarios que coincidan en el mismo bloque, el sistema debe verificar estos conflictos **antes** de realizar la asignación
- Una sección con estudiantes inscritos no puede ser eliminada del sistema

# Inscripción académica

## Reglas de negocio
- El estudiante debe estár matriculado en el periodo académico correspondiente y con estado **REGULAR** para hacer toma de ramos
- El sistema debe verificar automaticamente que el estudiante cumple las condiciones para inscribirse a un ramo
- El estudiante solo puede inscribir asignaturas de su plan de estudio ofrecidas durante el período académico
- Las inscripciones y anulaciones solo podrán realizarse mientras el período académico se encuentre ABIERTO
- Una asignatura aprobada no puede ser inscrita nuevamente por el estudiante
- Un ramo reprobado **UNA VEZ** puede ser inscrito por el estudiante
- El estudiante debe inscribir al menos 3 asignaturas y máximo 6 por periodo académico. Si le quedan menos 3 asignaturas pendientes para terminar su plan de estudio, podrá inscribirlos
- Un estudiante no podrá inscribir asignaturas de un semestre superior mientras tenga asignaturas pendientes de semestres inferiores que estén disponibles para la inscripción
- Una asignatura solo puede ser inscrita si tiene cupos en su sección, una inscripción usa un cupo y anular libera un cupo
- Un estudiante no puede inscribirse a secciones que generen tope de horario
- Toda sección queda asociada al estudiante, asignatura, sección y periodo académico, registrando las fechas en que fue realizada 


# Registro de calificaciones finales y cierre de semestre

## Reglas de negocio
- Al finalizar un período académico el sistema permite a los profesores registrar las calificaciones finales de cada estudiante de sus secciones
- El sistema no administra las evaluaciones parciales durante el semestre
- El proceso con el cual se determina la calificación final es fuera del sistema (no hay que preocuparse de ponderaciones ni calculos aquí) Solo registra las notas finales
- Con la nota final el sistema automaticamente determina si un estudiante aprueba o reprueba, si la nota es **igual o superior** a **4,0** el estudiante se considera **APROBADO**, en caso contrario es **REPROBADO**
- Las calificaciones deben ser entre el rango 1,0 a 7,0 y pueden ser modificadas mientras el período académico se encuentre **ABIERTO**
- Solo se puede hacer cierre de un período académico si este se encuentra en estado **ABIERTO** y todos los alumnos inscritos en secciones del periodo poseen **TODAS** sus calificaciones finales. Si hay calificaciones pendientes el sistema debe indicar en que secciones faltan estas mismas
- En el cierre las calificaiones y resultados deben guardarse en el historial académico de los estudiantes
- Si un estudiante reprueba una misma asignatura por segunda vez pasa automaticamente a ELIMINADO, conserva sus inscripciones, calificaciones e historial académico
- Un estudiante que aprueba todas las asignaturas de su plan de estudio pasa automaticamente a **EGRESADO**
- Una vez procesado todo del periodo academico cambia a estado **CERRADO**
- Un período académico **CERRADO** no permite modificar calificaciones, nuevas inscripciones o anulaciones ni ejecutar el proceso normal de cierre

# ROLES EN EL SISTEMA

## Administrador Académico
Responsable de la gestión académica del sistema. Puede
administrar estudiantes, docentes, carreras, planes de estudio, asignaturas, períodos
académicos, oferta académica y secciones. También puede consultar las inscripciones y
realizar el cierre de los períodos académicos.


## Docente
Puede consultar las secciones que tiene asignadas y los estudiantes inscritos en
ellas, así como registrar y modificar sus calificaciones finales mientras el período académico se
encuentre abierto.

## Estudiante
Puede consultar su información académica, plan de estudios, oferta académica e
historial académico. También puede realizar y consultar sus inscripciones durante los períodos
habilitados para ello.

# COSAS AÑADIDAS POR MI

## Datos de Historial Académico
- Código
- Código alumno
- Código sección
- Código profesor
- Período académico
- Estado
    - Aprobado
    - Reprobado
    - Cursando
