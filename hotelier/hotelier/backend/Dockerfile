# ---- Stage 1: build the JAR with Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests clean package

# ---- Stage 2: small runtime image ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/hotelier-backend.jar app.jar
EXPOSE 8080
# Render sets $PORT; the app reads it through server.port=${PORT:8080}
# Memory flags keep the JVM inside the 512 MB limit of Render's free instance.
ENTRYPOINT ["java", "-Xmx300m", "-XX:MaxMetaspaceSize=128m", "-XX:+UseSerialGC", "-Xss512k", "-jar", "app.jar"]
