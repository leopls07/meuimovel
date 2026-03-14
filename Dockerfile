# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia o pom primeiro para aproveitar o cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código e compila (sem rodar testes — já foram no CI)
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Debian slim em vez de Alpine — Alpine tem incompatibilidade de TLS (musl libc)
# que causa SSLException ao conectar no MongoDB Atlas
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Cria usuário não-root
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]