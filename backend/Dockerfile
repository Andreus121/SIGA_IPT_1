### Builder stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar wrapper y pom desde el contexto del backend
COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./

# Descargar dependencias en caché
RUN ./mvnw -B dependency:go-offline || true

# Copiar código fuente
COPY src ./src

# Compilar omitiendo tests
RUN ./mvnw -B clean package -DskipTests

### Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
