
## Basde de datos: PostgreSQL

Razones: 
1. Tomé la decisión de evitar las relaciones con FK para después facilitar los microservicios, con esto en PostgreSQL permite Arrays de manera nativa en las columnas, facilitando conexiones (como guardar bloques de horarios de una sección)
2. Validaciones a nivel columna, permite añadir restricciones nativas en la base de datos (por ejemplo los cupos de una seccion no pueden superar 60 y las notas deben ser entre 1,0 y 7,0)
3. Control de concurrencia superior MVCC, PostgreSQL tiene mayor solidez para prevenir sobrecupos y facilitará la toma de ramos

## Añadir dependecia "Valid"

Razones:
1. permite evitar bloques if/else en el codigo simplificando reglas de negocio
2. permite verificar peticiones directamentecon los DTOs usando @Valid

## Añadir dependecia "Spring Boot DevTools"

Razones:
1. Cuando modifico un archivo y el servidor está ejecutandose, se actualiza solo con los cambios sin tener que reiniciar

## Patrones de diseño
1. Separation of Concerns (separación de intereses) que es dividir un sistema en varias secciones, en este caso como entity, service, repository, etc.
2. 