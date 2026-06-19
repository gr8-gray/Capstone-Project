# --- build stage: compile Java + Kotlin, package the boot jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Cache dependencies in a separate layer (best-effort; build still works if it can't go fully offline)
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
# Tests need Docker (Testcontainers) which isn't available in the image build — skip them here;
# they run in CI / locally. The boot jar bundles the static frontend under resources/static.
RUN mvn -B -q clean package -DskipTests

# --- run stage: slim JRE ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -XX:MaxRAMPercentage=75 -jar app.jar"]
